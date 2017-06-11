package com.ivieleague.kotlin.server

sealed class Condition {
    data class All(val conditions: List<Condition>) : Condition()
    data class Any(val conditions: List<Condition>) : Condition()

    data class Equal(val value: Value, val equals: Any?) : Condition()
    data class NotEqual(val value: Value, val doesNotEqual: Any?) : Condition()
    data class Comparison(val value: Value, val comparedTo: Any?, val lessThan: Boolean = true, val equal: Boolean = false) : Condition()
    data class Between(val value: Value, val range: ClosedRange<*>) : Condition()
}