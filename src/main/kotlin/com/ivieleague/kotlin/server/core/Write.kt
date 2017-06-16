package com.ivieleague.kotlin.server.core

class Write(
        var id: String? = null,
        val scalars: Map<Scalar, Any?> = mapOf(),
        val links: Map<Link, Write> = mapOf(),
        val multilinks: Map<Multilink, MultilinkModifications> = mapOf()
)