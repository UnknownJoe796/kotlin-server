package com.ivieleague.kotlin.server

interface DAO {
    fun get(table: Table, id: String, output: Output): Instance?
    fun query(table: Table, condition: Condition, output: Output): Collection<Instance>
    fun update(table: Table, input: Input): Instance
    fun delete(table: Table, id: String): Boolean
}

