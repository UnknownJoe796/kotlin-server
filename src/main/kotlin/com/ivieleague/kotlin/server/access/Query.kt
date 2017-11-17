package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SimpleTypedObject

interface Query {
    val condition: Condition?
    val sort: List<Sort>
    val startAfter: SimpleTypedObject?
    val count: Int
}