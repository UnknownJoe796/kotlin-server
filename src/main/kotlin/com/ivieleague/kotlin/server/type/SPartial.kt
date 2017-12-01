package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SPartial<T>(val ofType: SType<T>) : SType<Exists<T>> {
    override val kclass = Exists::class
    override val name: String = "Partial<${ofType.name}>"
    override val description: String = "A value of type ${ofType.name} which may or may not exist."

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)

    override fun parse(node: JsonNode?): Exists<T> {
        if (node == null) return Exists()
        return Exists(ofType.parse(node))
    }

    override fun serialize(generator: JsonGenerator, value: Exists<T>) {
        val subvalue = value.value
        if (subvalue != null)
            ofType.serialize(generator, subvalue)
    }

    override fun serialize(factory: JsonNodeFactory, value: Exists<T>): JsonNode? {
        val subvalue = value.value
        if (subvalue == null) return null
        else return ofType.serialize(factory, subvalue)
    }
    override val default: Exists<T> = Exists()

    companion object {
        private val cache = HashMap<SType<*>, SPartial<*>>()
        operator fun <T> get(type: SType<T>) = cache.getOrPut(type) { SPartial(type) } as SPartial<T>
    }
}

class Exists<T>(
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
}