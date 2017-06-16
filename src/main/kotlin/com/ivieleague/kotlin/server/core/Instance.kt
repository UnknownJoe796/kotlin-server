package com.ivieleague.kotlin.server.core

class Instance(
        val id: String,
        val scalars: Map<Scalar, Any?>,
        val links: Map<Link, Instance?>,
        val multilinks: Map<Multilink, Collection<Instance>>
)