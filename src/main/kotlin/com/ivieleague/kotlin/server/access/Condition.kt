package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SimpleTypedObject

interface Condition : (SimpleTypedObject) -> Boolean {
    object Always : Condition {
        override fun invoke(p1: SimpleTypedObject): Boolean = true
    }

    object Never : Condition {
        override fun invoke(p1: SimpleTypedObject): Boolean = false
    }

    class AllConditions(val others: List<Condition>) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = others.all { it.invoke(obj) }
    }

    class AnyCondition(val others: List<Condition>) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = others.any { it.invoke(obj) }
    }

    class Equal(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) -> obj[key] == value }
    }

    class NotEqual(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) -> obj[key] != value }
    }

    class GreaterThanOrEqualTo(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) ->
            val reference = value as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[key] as? Comparable<Comparable<Any>> ?: return@all false
            actual >= reference
        }
    }

    class LessThanOrEqualTo(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) ->
            val reference = value as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[key] as? Comparable<Comparable<Any>> ?: return@all false
            actual <= reference
        }
    }

    class GreaterThan(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) ->
            val reference = value as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[key] as? Comparable<Comparable<Any>> ?: return@all false
            actual > reference
        }
    }

    class LessThan(val value: SimpleTypedObject) : Condition {
        override fun invoke(obj: SimpleTypedObject): Boolean = value.all { (key, value) ->
            val reference = value as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[key] as? Comparable<Comparable<Any>> ?: return@all false
            actual < reference
        }
    }

}
