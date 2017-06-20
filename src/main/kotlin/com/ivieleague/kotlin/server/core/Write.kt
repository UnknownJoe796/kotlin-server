package com.ivieleague.kotlin.server.core

class Write(
        var id: String? = null,
        val scalars: HashMap<Scalar, Any?> = hashMapOf(),
        val links: HashMap<Link, Write?> = hashMapOf(),
        val multilinks: HashMap<Multilink, MultilinkModifications> = hashMapOf()
)