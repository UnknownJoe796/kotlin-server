package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.Fetcher

/**
 *
 * Created by josep on 7/12/2017.
 */
class Schema() : Fetcher<Table, TableAccess> {

    val nameToAccess = HashMap<String, TableAccess>()
    val tableToAccess = HashMap<Table, TableAccess>()
    val nameToTable = HashMap<String, Table>()
    val tables get() = nameToTable.values
    val accesses get() = tableToAccess.values

    operator fun plusAssign(tableAccess: TableAccess) {
        nameToAccess += tableAccess.table.tableName to tableAccess
        tableToAccess += tableAccess.table to tableAccess
        nameToTable += tableAccess.table.tableName to tableAccess.table
    }


    override fun get(key: Table): TableAccess = tableToAccess[key]!!
}

fun <T : TableAccess> T.register(schema: Schema): T {
    schema += this
    return this
}