package com.ivieleague.kotlin.server.core

interface Property {
    val key: String
    val description: String
    val startVersion: Int
    val endVersion: Int

    val readPermission: SecurityRule
    val writeBeforePermission: SecurityRule
    val writeAfterPermission: SecurityRule
}