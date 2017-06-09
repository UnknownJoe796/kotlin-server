package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.*
import java.sql.Connection

class JDBCDAO(val connection: Connection, val database: String, val schema: Schema) : DAO {

    fun createTable(table: Table) {
        connection.createStatement().executeQuery(
                """CREATE TABLE ${table.tableName}(
                    ID INT  NOT NULL,
                    PRIMARY KEY (ID)
                )"""
        )
    }

    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
        connection.createStatement().executeQuery()
    }

    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(table: Table, queryConditions: List<Condition>, outProperties: Collection<Property>): Collection<Instance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}