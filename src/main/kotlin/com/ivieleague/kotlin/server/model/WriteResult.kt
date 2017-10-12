package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.*

data class WriteResult(
        var table: Table,
        var write: Write,
        var id: String?,
        val links: MutableMap<Link, WriteResult?> = mutableMapOf(),
        val multilinks: MutableMap<Multilink, MultilinkModificationsResults> = mutableMapOf()
) {
    class MultilinkModificationsResults(
            var additions: Collection<WriteResult>? = null,
            var removals: Collection<WriteResult>? = null,
            var replacements: Collection<WriteResult>? = null
    )

    companion object {
        fun table(onTable: Table): Table = object : Table {
            override val tableName: String
                get() = onTable.tableName + "WriteResult"
            override val tableDescription: String
                get() = "A write result for type ${onTable.tableName}"
            override val primitives: Collection<Primitive>
                get() = listOf()
            override val links: Collection<Link> = onTable.links.map {
                Link(
                        key = it.key,
                        description = it.description,
                        table = table(it.table)
                )
            }
            override val multilinks: Collection<Multilink> = onTable.multilinks.asSequence().flatMap {
                sequenceOf(
                        Multilink(
                                key = it.key,
                                description = it.description,
                                table = table(it.table)
                        ),
                        Multilink(
                                key = "+" + it.key,
                                description = it.description,
                                table = table(it.table)
                        ),
                        Multilink(
                                key = "-" + it.key,
                                description = it.description,
                                table = table(it.table)
                        )
                )
            }.toList()
            override val readPermission: SecurityRule
                get() = SecurityRules.always
            override val writeBeforePermission: SecurityRule
                get() = SecurityRules.always
            override val writeAfterPermission: SecurityRule
                get() = SecurityRules.always
        }
    }
}