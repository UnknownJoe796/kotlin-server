package com.ivieleague.kotlin.server

typealias AccessRules = List<List<AccessRule>>
data class Property(
        val name: String,
        val description: String,
        val type: ServerType,
        val default: Any?,
        val read: AccessRules = listOf(listOf()),
        val write: AccessRules = listOf(listOf()),
        val versionStart: Int = 0,
        val versionEnd: Int = Int.MAX_VALUE,
        val calculated: ((String, DAO) -> Any?)? = null
)