package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.type.meta.SClassClass

interface SClass : SHasFields<TypedObject> {
    override val name: String
    override val description: String
    val primaryKey: List<TypeField<*>> get() = listOf()
    override val default: TypedObject get() = SimpleTypedObject(this)

    override val dependencies: Collection<SType<*>>
        get() = fields.map { it.value.type }

    override val kclass get() = Map::class

    override fun parse(node: JsonNode?): TypedObject = if (node == null || node.isNull) throw IllegalArgumentException("Node '$node' cannot be parsed into an instance of $name.")
    else parseDirect(JsonGlobals.jsonNodeFactory, node)

    fun parseSimple(node: JsonNode): SimpleTypedObject {
        val result = SimpleTypedObject(this)
        for ((key, field) in fields) {
            val value: JsonNode? = node.get(key)
            @Suppress("UNCHECKED_CAST")
            result[key] = (field.type as SType<Any?>).parse(value, field.default)
        }
        return result
    }

    fun parseDirect(factory: JsonNodeFactory, node: JsonNode): JsonTypedObject {
        return JsonTypedObject(this, factory, node as ObjectNode)
    }

    override fun parse(parser: JsonParser): TypedObject {
        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = SimpleTypedObject(this)
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            val key = parser.currentName
            val field = fields[key]
            if (field == null) {
                result[key] = SPrimitives.getDefault(token).parse(parser)
            } else {
                result[key] = field.type.parse(parser)
            }
            token = parser.nextValue()
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(generator: JsonGenerator, value: TypedObject) {
        generator.apply {
            writeStartObject()
            for ((key, field) in fields) {
                writeFieldName(key)

                val item: Any? = value[field]
                if (item == null)
                    writeNull()
                else {
                    (field.type as SType<Any>).serialize(generator, item)
                }
            }
            writeEndObject()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: TypedObject): JsonNode {
        return if (value is JsonTypedObject) value.source
        else factory.objectNode().apply {
            for ((key, field) in fields) {
                val item = value[field]
                if (item == null)
                    set(key, nullNode())
                else {
                    set(key, (field.type as SType<Any>).serialize(factory, item))
                }
            }

        }
    }


    override fun reflect(): TypedObject = SClassClass.make(this)
}