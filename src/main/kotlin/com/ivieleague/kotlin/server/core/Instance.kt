package com.ivieleague.kotlin.server.core

class Instance(
        val id: String,
        val scalars: Map<Scalar, Any?> = mapOf(),
        val links: Map<Link, Instance?> = mapOf(),
        val multilinks: Map<Multilink, Collection<Instance>> = mapOf()
)