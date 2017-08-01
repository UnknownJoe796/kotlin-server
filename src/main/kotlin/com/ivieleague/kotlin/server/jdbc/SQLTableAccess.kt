package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.model.Instance
import com.ivieleague.kotlin.server.model.Read
import com.ivieleague.kotlin.server.model.TableAccess
import com.ivieleague.kotlin.server.sql.SQLColumn
import com.ivieleague.kotlin.server.sql.SQLQuery
import com.ivieleague.kotlin.server.sql.SQLResultColumn
import com.ivieleague.kotlin.server.sql.SQLTable
import java.sql.Connection
import java.sql.ResultSet

interface SQLTableAccess : TableAccess {
    val connection: Connection
    val sqlTable: SQLTable

    fun parseResult(read: Read, resultSet: ResultSet, map: Map<SQLColumn, SQLResultColumn>): Instance?
    fun getSubquery(read: Read, id: SQLResultColumn): Pair<List<SQLResultColumn>, SQLQuery.Join>

}