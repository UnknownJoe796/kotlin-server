package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SPartial<T : Any>(val ofType: SType<T>) : SType<Exists<T>> {
    override val kclass = Exists::class
    override val name: String = "Partial<${ofType.name}>"
    override val description: String = "A value of type ${ofType.name} which may or may not exist on top of possibly being null."

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)

    override fun parse(node: JsonNode?): Exists<T>? {
        if (node == null) return null
        return Exists(value = ofType.parse(node))
    }

    override fun serialize(generator: JsonGenerator, value: Exists<T>?) {
        if (value == null) return
        ofType.serialize(generator, value.value)
    }

    override fun serialize(factory: JsonNodeFactory, value: Exists<T>?): JsonNode? {
        if (value == null) return null
        else return ofType.serialize(factory, value.value)
    }

    companion object {
        private val cache = HashMap<SType<*>, SPartial<*>>()
        operator fun <T : Any> get(type: SType<T>) = cache.getOrPut(type) { SPartial(type) } as SPartial<T>
    }
}

class Exists<T>(
        var value: T? = null
)