package com.ivieleague.kotlin.server

interface Table {
    val tableName: String
    val tableDescription: String
    val properties: Map<String, Property>
}

