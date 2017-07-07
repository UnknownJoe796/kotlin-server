package com.ivieleague.kotlin.server.model

class Instance(
        val table: Table,
        var id: String,
        val scalars: MutableMap<Scalar, Any?> = mutableMapOf(),
        val links: MutableMap<Link, Instance?> = mutableMapOf(),
        val multilinks: MutableMap<Multilink, Collection<Instance>> = mutableMapOf()
) {
}