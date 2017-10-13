package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KClass

interface Condition : (TypedObject) -> Boolean {
    object Always : Condition {
        override fun invoke(p1: TypedObject): Boolean = true
    }

    object Never : Condition {
        override fun invoke(p1: TypedObject): Boolean = false
    }

    class All(val others: List<Condition>) : Condition {
        override fun invoke(obj: TypedObject): Boolean = others.all { it.invoke(obj) }
    }

    class Any(val others: List<Condition>) : Condition {
        override fun invoke(obj: TypedObject): Boolean = others.any { it.invoke(obj) }
    }

    class Equal(val key: String, val value: Any?) : Condition {
        override fun invoke(obj: TypedObject): Boolean = obj[key] == value
    }

    class NotEqual(val key: String, val value: Any?) : Condition {
        override fun invoke(obj: TypedObject): Boolean = obj[key] != value
    }

    class GreaterThanOrEqualTo(val key: String, valuea: Comparable<*>) : Condition {
        @Suppress("UNCHECKED_CAST")
        val value: Comparable<Any> = valuea as Comparable<Any>

        override fun invoke(obj: TypedObject): Boolean {
            @Suppress("UNCHECKED_CAST")
            val actual: Comparable<Any> = obj[key] as? Comparable<Any> ?: return false
            return actual.compareTo(value) >= 1
        }
    }
}

object SCondition : SType<Condition> {
    override val kclass: KClass<*>
        get() = Condition::class

    override fun parse(node: JsonNode): Condition? {
        if (node.isNull) return null
        val type = node.get("type").asText()
        when (type) {
            "Always"
        }
    }

    override fun serialize(generator: JsonGenerator, value: Condition?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}