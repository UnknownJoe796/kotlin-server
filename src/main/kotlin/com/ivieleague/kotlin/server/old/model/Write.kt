package com.ivieleague.kotlin.server.old.model

import com.ivieleague.kotlin.server.old.type.Link
import com.ivieleague.kotlin.server.old.type.Multilink
import com.ivieleague.kotlin.server.old.type.Primitive

class Write(
        var id: String? = null,
        var delete: Boolean = false,
        val scalars: HashMap<Primitive, Any?> = hashMapOf(),
        val links: HashMap<Link, Write?> = hashMapOf(),
        val multilinks: HashMap<Multilink, MultilinkModifications> = hashMapOf()
)