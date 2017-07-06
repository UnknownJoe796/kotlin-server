package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.ivieleague.kotlin.server.model.*
import java.io.StringWriter
import java.math.BigDecimal
import java.util.*

val jsonFactory = JsonFactory()
val JsonObjectMapper = ObjectMapper()

class InstanceSerializer() : StdSerializer<Instance>(Instance::class.java) {
    override fun serialize(value: Instance?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value != null) gen.writeInstance(value)
        else gen.writeNull()
    }

}

fun JsonGenerator.writeInstance(instance: Instance) {
    writeStartObject()
    writeStringField("id", instance.id)
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


fun Map<String, Any?>.toCondition(table: Table): Condition = when ((this["type"] as? String)?.toLowerCase()) {
    "always" -> Condition.Always
    "never" -> Condition.Never
    "all" -> Condition.AllConditions((this["conditions"] as List<*>).map { (it as Map<String, Any?>).toCondition(table) })
    "any" -> Condition.AnyConditions((this["conditions"] as List<*>).map { (it as Map<String, Any?>).toCondition(table) })
    "scalarEqual" -> {
        var currentTable = table
        Condition.ScalarEqual(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                equals = this["value"]
        )
    }
    "scalarNotEqual" -> {
        var currentTable = table
        Condition.ScalarNotEqual(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                scalar = currentTable.properties[this["scalar"] as String] as Scalar,
                doesNotEqual = this["value"]
        )
    }
    "scalarBetween" -> {
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
    "idEquals" -> {
        var currentTable = table
        Condition.IdEquals(
                path = (this["path"] as? List<String>)?.asSequence()?.map {
                    val link = currentTable.properties[it] as Link
                    currentTable = link.table
                    link
                }?.toList() ?: listOf(),
                equals = this["id"] as String
        )
    }
    "multilinkContains" -> {
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
    "multilinkDoesNotContains" -> {
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


fun Map<String, Any?>.toRequest(tableMap: Map<String, Table>) = when (this["request"]) {
    "get" -> toGetRequest(tableMap)
    "update" -> toUpdateRequest(tableMap)
    "delete" -> toDeleteRequest(tableMap)
    "query" -> toQueryRequest(tableMap)
    else -> throw IllegalArgumentException()
}

fun Map<String, Any?>.toGetRequest(tableMap: Map<String, Table>): Request.Get = (this["table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Get(
            table = table,
            id = this["id"] as String,
            read = (this["read"] as Map<String, Any?>).toRead(table)
    )
}

fun Map<String, Any?>.toQueryRequest(tableMap: Map<String, Table>): Request.Query = (this["table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Query(
            table = table,
            condition = (this["read"] as Map<String, Any?>).toCondition(table),
            read = (this["read"] as Map<String, Any?>).toRead(table)
    )
}

fun Map<String, Any?>.toUpdateRequest(tableMap: Map<String, Table>): Request.Update = (this["table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Update(
            table = table,
            write = (this["write"] as Map<String, Any?>).toWrite(table)
    )
}

fun Map<String, Any?>.toDeleteRequest(tableMap: Map<String, Table>): Request.Delete = (this["table"] as String).let {
    val table = tableMap[it] ?: throw IllegalArgumentException("Table '$it' not found")
    Request.Delete(
            table = table,
            id = this["id"] as String
    )
}


fun JsonFactory.generateString(action: JsonGenerator.() -> Unit): String {
    val stringWriter = StringWriter()
    val gen = createGenerator(stringWriter)
    gen.action()
    gen.close()
    return stringWriter.toString()
}

fun Instance.toJsonString() = jsonFactory.generateString { writeInstance(this@toJsonString) }
fun String.toRead(table: Table) = (JsonObjectMapper.readValue(this, Map::class.java) as Map<String, Any?>).toRead(table)
fun String.toWrite(table: Table) = (JsonObjectMapper.readValue(this, Map::class.java) as Map<String, Any?>).toWrite(table)
fun String.toWrite(id: String, table: Table) = (JsonObjectMapper.readValue(this, Map::class.java) as Map<String, Any?>).toWrite(id, table)


fun Map<String, Any?>.toRead(table: Table): Read {
    val read = Read()

    for ((key, value) in this) {
        val property = table.properties[key]
        when (property) {
            is Scalar -> if (value !is Boolean || value) read.scalars += property
            is Link -> read.links[property] = (value as Map<String, Any?>).toRead(property.table)
            is Multilink -> read.multilinks[property] = (value as Map<String, Any?>).toRead(property.table)
            null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
        }
    }

    return read
}


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
    return Write(this["id"]?.toString(), scalars, links, multilinks)
}

fun Map<String, Any?>.toWrite(id: String, table: Table) = toWrite(table).apply { this.id = id }