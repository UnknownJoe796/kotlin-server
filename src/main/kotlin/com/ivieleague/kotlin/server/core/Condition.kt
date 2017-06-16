package com.ivieleague.kotlin.server.core

sealed class Condition {
    object Always : Condition()
    object Never : Condition()
    data class AllCondition(val conditions: List<Condition>) : Condition()
    data class AnyCondition(val conditions: List<Condition>) : Condition()
    data class ScalarEqual(val scalar: Scalar, val equals: Any?) : Condition()
    data class ScalarNotEqual(val scalar: Scalar, val doesNotEqual: Any?) : Condition()
}