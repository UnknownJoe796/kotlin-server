package com.ivieleague.kotlin.server.model

data class WriteResult(
        var table: Table,
        var write: Write,
        var id: String,
        val links: MutableMap<Link, WriteResult> = mutableMapOf(),
        val multilinks: MutableMap<Multilink, MultilinkModificationsResults> = mutableMapOf()
) {
    class MultilinkModificationsResults(
            var additions: Collection<String>? = null,
            var removals: Collection<String>? = null,
            var replacements: Collection<String>? = null
    )
}