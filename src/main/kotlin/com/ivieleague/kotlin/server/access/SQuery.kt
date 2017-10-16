package com.ivieleague.kotlin.server.access

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.*
import kotlin.reflect.KClass

class SQuery private constructor(val type: SClass) : SClass {
    override val name: String
        get() = "Query<$type>"
    override val description: String
        get() = "Query information for type ${type.name}"

    val ssort = object : SType<Sort> {
        override val kclass: KClass<*> = Sort::class
        override fun parse(node: JsonNode): Sort? {
            if (node.isNull) return null
            val field = type.fields[node.get("field").asText()]!!
            return Sort(node.get("ascending")?.asBoolean() ?: true, field)
        }

        override fun serialize(generator: JsonGenerator, value: Sort?) = generator.writeNullOr(value) {
            writeStartObject()
            writeFieldName("ascending")
            writeBoolean(it.ascending)
            writeFieldName("field")
            writeString(it.field.key)
            writeEndObject()
        }
    }

    override val fields: Map<String, SClass.Field<*>> = listOf(
            SClass.Field(
                    key = "condition",
                    description = "The condition to query for",
                    type = STypedCondition[type],
                    default = null
            ),
            SClass.Field(
                    key = "sort",
                    description = "The things to sort on",
                    type = SList(ssort),
                    default = listOf()
            ),
            SClass.Field(
                    key = "startAfter",
                    description = "The object to start after",
                    type = type,
                    default = null
            ),
            SClass.Field(
                    key = "count",
                    description = "The maximum number of instances to return",
                    type = SInt,
                    default = 100
            )
    ).associate { it.key to it }

    companion object {
        private val cache = HashMap<SClass, SQuery>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SQuery(type) }

        fun wrap(typedObject: TypedObject): Query = object : Query {
            override val condition: Condition?
                get() = (typedObject["condition"] as? TypedObject)?.let { STypedCondition.wrap(it) }
            override val sort: List<Sort>
                get() = (typedObject["sort"] as? List<Sort>) ?: listOf()
            override val startAfter: TypedObject?
                get() = typedObject["startAfter"] as? TypedObject
            override val count: Int
                get() = typedObject["count"] as? Int ?: typedObject.type.fields["count"]!!.default as Int
        }
    }
}