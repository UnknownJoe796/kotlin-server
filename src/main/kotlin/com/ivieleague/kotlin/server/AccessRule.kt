package com.ivieleague.kotlin.server

sealed class AccessRule {
    class AccessLevel(val level: Int) : AccessRule()
    class Condition(val condition: Condition)
}