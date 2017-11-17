package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.access.Condition
import com.ivieleague.kotlin.server.type.SimpleTypedObject

typealias SecurityRule = (user: SimpleTypedObject?) -> Condition

object SecurityRules {
    val always: SecurityRule = { Condition.Always }
    val never: SecurityRule = { Condition.Never }
}