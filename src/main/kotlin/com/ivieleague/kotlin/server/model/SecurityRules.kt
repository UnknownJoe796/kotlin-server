package com.ivieleague.kotlin.server.model

typealias SecurityRule = (user: Instance?) -> Condition

object SecurityRules {
    val always: SecurityRule = { Condition.Always }
    val never: SecurityRule = { Condition.Never }
}