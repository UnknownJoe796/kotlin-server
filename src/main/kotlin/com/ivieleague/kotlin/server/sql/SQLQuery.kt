package com.ivieleague.kotlin.server.sql

data class SQLQuery(
        val select: Collection<SQLResultColumn>,
        val from: Collection<SQLDataSourceAccess>,
        val join: List<Join>,
        val where: SQLCondition,
        val orderBy: List<SQLResultColumn>
) : SQLDataSource {

    class Join(
            val type: Join.Type,
            val source: SQLDataSourceAccess,
            val on: SQLCondition
    ) {
        enum class Type {
            LEFT, INNER, OUTER, RIGHT
        }

        override fun toString(): String = "$type JOIN $source ON $on"
    }

    override fun toString(): String = """
SELECT ${select.joinToString { it.toDefineString() }}
FROM ${from.joinToString { it.toDefineString() }}
WHERE $where
${join.joinToString("\n")}
ORDER BY ${orderBy.joinToString()}"""
}