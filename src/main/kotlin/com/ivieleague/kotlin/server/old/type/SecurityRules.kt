package com.ivieleague.kotlin.server.old.type

typealias SecurityRule = (user: Instance?) -> Condition

object SecurityRules {
    val always: SecurityRule = { Condition.Always }
    val never: SecurityRule = { Condition.Never }
}