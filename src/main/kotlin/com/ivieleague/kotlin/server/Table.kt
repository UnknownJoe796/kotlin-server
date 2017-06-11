package com.ivieleague.kotlin.server

interface Table {
    val tableName: String
    val tableDescription: String
    val properties: Map<String, Property>

    val query: TableAccessRule get() = TableAccessRules.Always
    val create: TableAccessRule get() = TableAccessRules.Always
    val edit: TableAccessRule get() = TableAccessRules.Always

    val queryFilter: (user: Instance?) -> Condition? get() = { null }
}

