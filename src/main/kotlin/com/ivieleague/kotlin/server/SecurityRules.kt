package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.type.Condition
import com.ivieleague.kotlin.server.type.TypedObject

typealias SecurityRule = (user: TypedObject?) -> Condition

object SecurityRules {
    val always: SecurityRule = { Condition.Always }
    val never: SecurityRule = { Condition.Never }
}