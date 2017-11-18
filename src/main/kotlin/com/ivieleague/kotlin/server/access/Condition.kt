package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.TypedObject

interface Condition : (TypedObject) -> Boolean {
    object Always : Condition {
        override fun invoke(p1: TypedObject): Boolean = true
    }

    object Never : Condition {
        override fun invoke(p1: TypedObject): Boolean = false
    }

    class AllConditions(val others: List<Condition>) : Condition {
        override fun invoke(obj: TypedObject): Boolean = others.all { it.invoke(obj) }
    }

    class AnyCondition(val others: List<Condition>) : Condition {
        override fun invoke(obj: TypedObject): Boolean = others.any { it.invoke(obj) }
    }

    class Equal(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            obj[field] == subvalue
        }
    }

    class NotEqual(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            obj[field] != subvalue
        }
    }

    class GreaterThanOrEqualTo(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            val reference = subvalue as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[field] as? Comparable<Comparable<Any>> ?: return@all false
            actual >= reference
        }
    }

    class LessThanOrEqualTo(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            val reference = subvalue as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[field] as? Comparable<Comparable<Any>> ?: return@all false
            actual <= reference
        }
    }

    class GreaterThan(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            val reference = subvalue as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[field] as? Comparable<Comparable<Any>> ?: return@all false
            actual > reference
        }
    }

    class LessThan(val value: TypedObject) : Condition {
        override fun invoke(obj: TypedObject): Boolean = value.type.fields.all { (key, field) ->
            val subvalue = value[field] ?: return@all true
            val reference = subvalue as Comparable<Any>
            val actual: Comparable<Comparable<Any>> = obj[field] as? Comparable<Comparable<Any>> ?: return@all false
            actual < reference
        }
    }

}
