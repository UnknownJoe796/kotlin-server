package com.ivieleague.kotlin.server.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.ivieleague.kotlin.server.InstanceSerializer

@JsonSerialize(using = InstanceSerializer::class)
class Instance(
        val table: Table,
        var id: String,
        val scalars: MutableMap<Scalar, Any?> = mutableMapOf(),
        val links: MutableMap<Link, Instance?> = mutableMapOf(),
        val multilinks: MutableMap<Multilink, Collection<Instance>> = mutableMapOf()
) {
}