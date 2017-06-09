package com.ivieleague.kotlin.server

interface DAO {
    fun get(table: Table, id: String, properties: Collection<Property>): Instance?
    fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance
    fun query(table: Table, queryConditions: List<Condition>, outProperties: Collection<Property>): Collection<Instance>
}