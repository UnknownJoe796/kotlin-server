package com.ivieleague.kotlin.server.old.type

fun Condition.simplify(): Condition = when (this) {
    is Condition.AllConditions -> {
        if (conditions.any { it is Condition.Never }) Condition.Never
        else Condition.AllConditions(conditions.filter { it is Condition.Always })
    }
    is Condition.AnyConditions -> {
        if (conditions.any { it is Condition.Always }) Condition.Always
        else Condition.AllConditions(conditions.filter { it is Condition.Never })
    }
    else -> this
}