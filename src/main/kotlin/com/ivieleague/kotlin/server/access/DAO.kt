package com.ivieleague.kotlin.server.access

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.Condition
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.TypedObject

typealias Read = TypedObject
typealias Write = TypedObject
typealias WriteResult = TypedObject

interface DAO {
    val type: SClass
    fun query(transaction: Transaction, query: Query, read: Read): List<TypedObject>
    fun update(transaction: Transaction, write: Write): WriteResult
}

class Sort(
        val ascending: Boolean,
        val field: SClass.Field<*>
)

class Query(
        val condition: Condition,
        val sort: Sort,
        val startAfter: TypedObject,
        val count: Int
)

class SQuery(val type: SClass) : SClass {
    override val name: String
        get() = "Query<$type>"
    override val description: String
        get() = "Query information for type ${type.name}"

    val condition = SClass.Field(
            key = "condition",
            description = "The condition to query for",
            type = SString,
            default = "HAI"
    )

    override val fields: Map<String, SClass.Field<*>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

class SRead(val type: SClass) : SClass {
    override val name: String
        get() = type.name + "_Read"
    override val description: String
        get() = "A description of what to read for ${type.name}"
    override val fields: Map<String, SClass.Field<*>> = run {
        val it = HashMap<String, SClass.Field<*>>()
        it
    }

    override fun parse(node: JsonNode): TypedObject {
        return super.parse(node)
    }

    override fun serialize(generator: JsonGenerator, value: TypedObject?) {
        super.serialize(generator, value)
    }
}