package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.ivieleague.kotlin.server.model.*
import java.io.StringWriter
import java.math.BigDecimal
import java.util.*

object KotlinServerModelsModule : SimpleModule("KotlinServerModelsModule", Version(1, 0, 0, "", "com.ivieleague", "kotlin-server")) {
    init {
        addSerializer(object : StdSerializer<Instance>(Instance::class.java) {
            override fun serialize(value: Instance?, gen: JsonGenerator, provider: SerializerProvider) {
                if (value != null) gen.writeInstance(value)
                else gen.writeNull()
            }
        })
        addSerializer(object : StdSerializer<WriteResult>(WriteResult::class.java) {
            override fun serialize(value: WriteResult?, gen: JsonGenerator, provider: SerializerProvider) {
                if (value != null) gen.writeWriteResult(value)
                else gen.writeNull()
            }
        })
    }
}

fun JsonGenerator.writeInstance(instance: Instance) {
    writeStartObject()
    writeStringField("_id", instance.id)
    for ((scalar, value) in instance.scalars) {
        when (value) {
            is Byte -> writeNumberField(scalar.key, value.toInt())
            is Short -> writeNumberField(scalar.key, value.toInt())
            is Int -> writeNumberField(scalar.key, value)
            is Long -> writeNumberField(scalar.key, value)
            is Float -> writeNumberField(scalar.key, value)
            is Double -> writeNumberField(scalar.key, value)
            is BigDecimal -> writeNumberField(scalar.key, value)
            is String -> writeStringField(scalar.key, value)
            is Boolean -> writeBooleanField(scalar.key, value)
        }
    }
    for ((link, value) in instance.links) {
        writeFieldName(link.key)
        if (value == null) writeNull()
        else writeInstance(value)
    }
    for ((multilink, values) in instance.multilinks) {
        writeFieldName(multilink.key)
        writeStartArray()
        for (value in values) {
            writeInstance(value)
        }
        writeEndArray()
    }
    writeEndObject()
}

fun JsonGenerator.writeWriteResult(result: WriteResult) {
    writeStartObject()
    writeStringField("_id", result.id)
    for ((link, value) in result.links) {
        writeFieldName(link.key)
        if (value == null) writeNull()
        else writeWriteResult(value)
    }
    for ((multilink, values) in result.multilinks) {
        writeFieldName(multilink.key)
        writeStartObject()

        val additions = values.additions
        if (additions != null) {
            writeFieldName("additions")
            writeStartArray()
            for (value in additions) {
                writeWriteResult(value)
            }
            writeEndArray()
        }

        val removals = values.removals
        if (removals != null) {
            writeFieldName("additions")
            writeStartArray()
            for (value in removals) {
                writeWriteResult(value)
            }
            writeEndArray()
        }

        val replacements = values.replacements
        if (replacements != null) {
            writeFieldName("additions")
            writeStartArray()
            for (value in replacements) {
                writeWriteResult(value)
            }
            writeEndArray()
        }

        writeEndObject()
    }
    writeEndObject()
}


fun Table.toInfoMap(user: Instance?): Map<String, Any?> = HashMap<String, Any?>().also {
    it["table_name"] = tableName
    it["table_description"] = tableDescription
    for (property in scalars) {
        it[property.key] = property.toInfoMap(user)
    }
    for (property in links) {
        it[property.key] = property.toInfoMap(user)
    }
    for (property in multilinks) {
        it[property.key] = property.toInfoMap(user)
    }
}

fun Scalar.toInfoMap(user: Instance?): Map<String, Any?> = mapOf(
        "key" to key,
        "description" to description,
        "start_version" to startVersion,
        "end_version" to endVersion,
        "read_permission" to readPermission.invoke(user).toInfoMap(),
        "edit_permission" to editPermission.invoke(user).toInfoMap(),
        "write_permission" to writePermission.invoke(user).toInfoMap(),
        "type" to this.type.toString()
)

fun Link.toInfoMap(user: Instance?): Map<String, Any?> = mapOf(
        "key" to key,
        "description" to description,
        "start_version" to startVersion,
        "end_version" to endVersion,
        "read_permission" to readPermission.invoke(user).toInfoMap(),
        "edit_permission" to editPermission.invoke(user).toInfoMap(),
        "write_permission" to writePermission.invoke(user).toInfoMap(),
        "type" to this.table.tableName
)

fun Multilink.toInfoMap(user: Instance?): Map<String, Any?> = mapOf(
        "key" to key,
        "description" to description,
        "start_version" to startVersion,
        "end_version" to endVersion,
        "read_permission" to readPermission.invoke(user).toInfoMap(),
        "edit_permission" to editPermission.invoke(user).toInfoMap(),
        "write_permission" to writePermission.invoke(user).toInfoMap(),
        "type" to this.table.tableName
)

fun Condition.toInfoMap(): Map<String, Any?> = when (this) {
    Condition.Always -> mapOf("type" to "always")
    Condition.Never -> mapOf("type" to "never")
    is Condition.AllConditions -> mapOf("type" to "all", "conditions" to conditions.map { it.toInfoMap() })
    is Condition.AnyConditions -> mapOf("type" to "any", "conditions" to conditions.map { it.toInfoMap() })
    is Condition.ScalarEqual -> mapOf("type" to "scalarEqual", "path" to path.map { it.key }, "scalar" to scalar.key, "value" to value)
    is Condition.ScalarNotEqual -> mapOf("type" to "scalarNotEqual", "path" to path.map { it.key }, "scalar" to scalar.key, "value" to value)
    is Condition.ScalarBetween<*> -> mapOf("type" to "scalarBetween", "path" to path.map { it.key }, "scalar" to scalar.key, "lower" to lower, "upper" to upper)
    is Condition.ScalarLessThanOrEqual<*> -> mapOf("type" to "scalarLessThanOrEqual", "path" to path.map { it.key }, "scalar" to scalar.key, "upper" to upper)
    is Condition.ScalarGreaterThanOrEqual<*> -> mapOf("type" to "scalarGreaterThanOrEqual", "path" to path.map { it.key }, "scalar" to scalar.key, "lower" to lower)
    is Condition.ScalarLessThan<*> -> mapOf("type" to "scalarLessThan", "path" to path.map { it.key }, "scalar" to scalar.key, "upper" to upper)
    is Condition.ScalarGreaterThan<*> -> mapOf("type" to "scalarGreaterThan", "path" to path.map { it.key }, "scalar" to scalar.key, "lower" to lower)
    is Condition.IdEquals -> mapOf("type" to "idEquals", "path" to path.map { it.key }, "id" to id)
    is Condition.MultilinkContains -> mapOf("type" to "multilinkContains", "path" to path.map { it.key }, "multilink" to multilink.key, "id" to id)
    is Condition.MultilinkDoesNotContain -> mapOf("type" to "multilinkDoesNotContain", "path" to path.map { it.key }, "multilink" to multilink.key, "id" to id)
}


@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toCondition(table: Table): Condition = when ((this["type"] as? String)?.toLowerCase()) {
    "always" -> Condition.Always
    "never" -> Condition.Never
    "all" -> Condition.AllConditions((this["conditions"] as List<*>).map { (it as Map<String, Any?>).toCondition(table) })
    "any" -> Condition.AnyConditions((this["conditions"] as List<*>).map { (it as Map<String, Any?>).toCondition(table) })
    "scalarequal" -> {
        var currentTable = table
        Condition.ScalarEqual(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                value = this["value"]
        )
    }
    "scalarnotequal" -> {
        var currentTable = table
        Condition.ScalarNotEqual(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                value = this["value"]
        )
    }
    "scalarbetween" -> {
        var currentTable = table
        Condition.ScalarBetween<Comparable<Any>>(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                lower = this["lower"] as Comparable<Any>,
                upper = this["upper"] as Comparable<Any>
        )
    }
    "scalarlessthanorequal" -> {
        var currentTable = table
        Condition.ScalarLessThanOrEqual<Comparable<Any>>(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                upper = this["upper"] as Comparable<Any>
        )
    }
    "scalargreaterthanorequal" -> {
        var currentTable = table
        Condition.ScalarGreaterThanOrEqual<Comparable<Any>>(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                lower = this["lower"] as Comparable<Any>
        )
    }
    "scalarlessthan" -> {
        var currentTable = table
        Condition.ScalarLessThan<Comparable<Any>>(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                upper = this["upper"] as Comparable<Any>
        )
    }
    "scalargreaterthan" -> {
        var currentTable = table
        Condition.ScalarGreaterThan<Comparable<Any>>(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                lower = this["lower"] as Comparable<Any>
        )
    }
    "idequals" -> {
        var currentTable = table
        Condition.IdEquals(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                id = this["id"] as String
        )
    }
    "multilinkcontains" -> {
        var currentTable = table
        Condition.MultilinkContains(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                multilink = currentTable.properties[this["scalar"] as String] as Multilink,
                id = this["id"] as String
        )
    }
    "multilinkdoesnotcontain" -> {
        var currentTable = table
        Condition.MultilinkDoesNotContain(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                multilink = currentTable.properties[this["scalar"] as String] as Multilink,
                id = this["id"] as String
        )
    }
    else -> throw IllegalArgumentException("Condition type '${this["type"] as? String}' is not defined.")
}


fun Map<String, Any?>.toRequest(tableMap: Map<String, Table>) = when (this["_request"]) {
    "get" -> toGetRequest(tableMap)
    "update" -> toUpdateRequest(tableMap)
    "query" -> toQueryRequest(tableMap)
    else -> throw IllegalArgumentException()
}

fun Map<String, Any?>.toGetRequest(tableMap: Map<String, Table>): Request.Get = (this["_table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Get(
            table = table,
            id = this["_id"] as String,
            read = this.toMutableMap().apply {
                remove("_id")
                remove("_table")
                remove("_request")
            }.toRead(table)
    )
}

fun Map<String, Any?>.toQueryRequest(tableMap: Map<String, Table>): Request.Query = (this["_table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Query(
            table = table,
            read = this.toMutableMap().apply {
                remove("_table")
                remove("_request")
            }.toRead(table)
    )
}

fun Map<String, Any?>.toUpdateRequest(tableMap: Map<String, Table>): Request.Update = (this["_table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Update(
            table = table,
            write = this.toMutableMap().apply {
                remove("_table")
                remove("_request")
            }.toWrite(table)
    )
}


fun JsonFactory.generateString(action: JsonGenerator.() -> Unit): String {
    val stringWriter = StringWriter()
    val gen = createGenerator(stringWriter)
    gen.action()
    gen.close()
    return stringWriter.toString()
}


@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toInstance(table: Table): Instance {
    val instance = Instance(table = table, id = "")

    for ((key, value) in this) {
        val property = table.properties[key]
        when (property) {
            is Scalar -> instance.scalars[property] = value
            is Link -> instance.links[property] = (value as Map<String, Any?>).toInstance(property.table)
            is Multilink -> instance.multilinks[property] = (value as List<Map<String, Any?>>).map { it.toInstance(property.table) }
        }
    }

    return instance
}

fun Map<String, Any?>.toSort(table: Table): Sort = Sort(
        scalar = table.properties[this["scalar"] as String] as Scalar,
        ascending = this["ascending"] as? Boolean ?: true,
        nullsLast = this["nulls_last"] as? Boolean ?: true
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toRead(table: Table): Read {
    val read = Read()

    for ((key, value) in this) {
        val property = table.properties[key]
        when (property) {
            is Scalar -> if (value !is Boolean || value) read.scalars += property
            is Link -> read.links[property] = (value as Map<String, Any?>).toRead(property.table)
            is Multilink -> read.multilinks[property] = (value as Map<String, Any?>).toRead(property.table)
            null -> {
                when (key) {
                    "_condition" -> read.condition = (value as? Map<String, Any?>)?.toCondition(table) ?: Condition.Always
                    "_after" -> read.startAfter = (value as? Map<String, Any?>)?.toInstance(table)
                    "_sort" -> read.sort = (value as? List<Map<String, Any?>>)?.map { it.toSort(table) } ?: listOf()
                    "_count" -> read.count = (value as? Number)?.toInt() ?: 100
                    else -> throw IllegalArgumentException("Key '$key' not found in table $table.")
                }
            }
        }
    }

    return read
}


@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toWrite(table: Table): Write {

    val scalars = HashMap<Scalar, Any?>()
    val links = HashMap<Link, Write?>()
    val multilinks = HashMap<Multilink, MultilinkModifications>()

    for ((key, value) in this) {
        if (key.startsWith('+')) {
            val property = table.properties[key.substring(1)] as Multilink
            multilinks.getOrPut(property) { MultilinkModifications() }.additions = (value as List<Map<String, Any?>>).map { item ->
                item.toWrite(property.table)
            }
        } else if (key.startsWith('-')) {
            val property = table.properties[key.substring(1)] as Multilink
            multilinks.getOrPut(property) { MultilinkModifications() }.removals = (value as List<Map<String, Any?>>).map { item ->
                item.toWrite(property.table)
            }
        } else {
            val property = table.properties[key]
            when (property) {
                is Scalar -> {
                    val type = property.type
                    scalars[property] = when (type) {
                        ScalarType.Boolean -> value as Boolean
                        ScalarType.Byte -> (value as Number).toByte()
                        ScalarType.Short -> (value as Number).toShort()
                        ScalarType.Int -> (value as Number).toInt()
                        ScalarType.Long -> (value as Number).toLong()
                        ScalarType.Float -> (value as Number).toFloat()
                        ScalarType.Double -> (value as Number).toDouble()
                        ScalarType.ShortString -> value as String
                        ScalarType.LongString -> value as String
                        ScalarType.Date -> Date(value as Long)
                        ScalarType.JSON -> value
                        is ScalarType.Enum -> type.enum.indexedByName[value as String]!!.value
                    }
                }
                is Link -> links[property] = (value as Map<String, Any?>).toWrite(property.table)
                is Multilink -> multilinks.getOrPut(property) { MultilinkModifications() }.replacements = (value as List<Map<String, Any?>>).map { item ->
                    item.toWrite(property.table)
                }
                null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
            }
        }
    }
    return Write(this["_id"]?.toString(), delete = this["_delete"].toBooleanDefaultFalse(), scalars = scalars, links = links, multilinks = multilinks)
}

private fun Any?.toBooleanDefaultTrue(): Boolean = this !is Boolean || this
private fun Any?.toBooleanDefaultFalse(): Boolean = this is Boolean && this

fun Map<String, Any?>.toWrite(id: String, table: Table) = toWrite(table).apply { this.id = id }