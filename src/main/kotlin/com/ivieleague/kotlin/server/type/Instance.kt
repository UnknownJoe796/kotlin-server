package com.ivieleague.kotlin.server.type

class Instance(
        val table: Table,
        var id: String,
        val scalars: MutableMap<Primitive, Any?> = mutableMapOf(),
        val links: MutableMap<Link, Instance?> = mutableMapOf(),
        val multilinks: MutableMap<Multilink, Collection<Instance>> = mutableMapOf()
)