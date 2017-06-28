package com.ivieleague.kotlin.server.model

class Multilink(
        override val key: String,
        override val description: String,
        override val startVersion: Int = 0,
        override val endVersion: Int = Int.MAX_VALUE,
        override val readPermission: SecurityRule = SecurityRules.always,
        override val writeBeforePermission: SecurityRule = SecurityRules.always,
        override val writeAfterPermission: SecurityRule = SecurityRules.always,
        val table: Table
) : Property