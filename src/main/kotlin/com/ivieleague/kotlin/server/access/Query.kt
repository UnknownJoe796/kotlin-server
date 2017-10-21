package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.TypedObject

interface Query {
    val condition: Condition?
    val sort: List<Sort>
    val startAfter: TypedObject?
    val count: Int
}