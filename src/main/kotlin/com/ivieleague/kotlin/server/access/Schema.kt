package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SType

interface Schema {
    val types: Map<String, SType<*>>
    val daos: Map<String, DAO<*>>
}