package com.ivieleague.kotlin.server

data class Instance(
        val table: Table,
        val id: String,
        val scalars: Map<Scalar, Any?>,
        val links: Map<Link, Instance?>,
        val multilinks: Map<MultiLink, List<Instance>>
)