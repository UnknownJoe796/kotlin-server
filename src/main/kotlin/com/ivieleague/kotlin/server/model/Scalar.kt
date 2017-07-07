package com.ivieleague.kotlin.server.model

class Scalar(
        override val key: String,
        override val description: String,
        override val startVersion: Int = 0,
        override val endVersion: Int = Int.MAX_VALUE,
        override val readPermission: SecurityRule = SecurityRules.always,
        override val editPermission: SecurityRule = SecurityRules.always,
        override val writePermission: SecurityRule = SecurityRules.always,
        val type: ScalarType
) : Property