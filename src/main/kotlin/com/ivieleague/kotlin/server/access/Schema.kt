package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass

interface Schema {
    val classes: Map<String, SClass>
    val daos: Map<String, DAO>
}