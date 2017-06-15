package com.ivieleague.kotlin.server

sealed class Condition {

    abstract fun invoke(row: Map<Property, Any?>): Boolean

    object Always : Condition() {
        override fun invoke(row: Map<Property, Any?>) = true
    }

    object Never : Condition() {
        override fun invoke(row: Map<Property, Any?>) = false
    }

    data class AllCondition(val conditions: List<Condition>) : Condition() {
        override fun invoke(row: Map<Property, Any?>) = conditions.all { it.invoke(row) }
    }

    data class AnyCondition(val conditions: List<Condition>) : Condition() {
        override fun invoke(row: Map<Property, Any?>) = conditions.any { it.invoke(row) }
    }

    data class Equal(val scalar: Scalar, val equals: Any?) : Condition() {
        override fun invoke(row: Map<Property, Any?>) = row[scalar] == equals
    }

    data class NotEqual(val scalar: Scalar, val doesNotEqual: Any?) : Condition() {
        override fun invoke(row: Map<Property, Any?>) = row[scalar] != doesNotEqual
    }
}