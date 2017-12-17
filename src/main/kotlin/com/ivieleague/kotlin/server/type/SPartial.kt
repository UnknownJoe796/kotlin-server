package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SPartial<T>(val ofType: SType<T>) : SType<Partial<T>> {
    override val kclass = Partial::class
    override val name: String = "Partial<${ofType.name}>"
    override val description: String = "A value of type ${ofType.name} which may or may not exist."

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)

    override fun parse(node: JsonNode?): Partial<T> {
        if (node == null) return Partial()
        else if (node.isTextual && node.textValue() == Partial.NO_VALUE) return Partial()
        return Partial(ofType.parse(node))
    }

    override fun serialize(generator: JsonGenerator, value: Partial<T>) {
        if (!value.exists) generator.writeString(Partial.NO_VALUE)
        else ofType.serialize(generator, value.value as T)
    }

    override fun serialize(factory: JsonNodeFactory, value: Partial<T>): JsonNode? {
        return if (!value.exists) factory.textNode(Partial.NO_VALUE)
        else ofType.serialize(factory, value.value as T)
    }

    override val default: Partial<T> = Partial()

    companion object {
        private val cache = HashMap<SType<*>, SPartial<*>>()
        operator fun <T> get(type: SType<T>) = cache.getOrPut(type) { SPartial(type) } as SPartial<T>
    }
}

class Partial<T>(
        var value: T? = null,
        var exists:Boolean = value != null
){
    inline fun letNotNull( ifNotNull:(T)->Unit){
        if(exists){
            ifNotNull.invoke(value as T)
        }
    }
    inline fun <A> letNotNull( ifNotNull:(T)->A, otherwise:()->A):A{
        return if(exists){
            ifNotNull.invoke(value as T)
        } else otherwise.invoke()
    }

    companion object {
        const val NO_VALUE = "\u0000"
    }
}