package com.ivieleague.kotlin.server

sealed class Condition {
    data class Equal(val property: Property, val value: Any?) : Condition()
    data class NotEqual(val property: Property, val value: Any?) : Condition()
    data class Comparison(val property: Property, val value: Any?, val lessThan: Boolean = true, val equal: Boolean = false) : Condition()
    data class Between(val property: Property, val range: ClosedRange<*>) : Condition()
}