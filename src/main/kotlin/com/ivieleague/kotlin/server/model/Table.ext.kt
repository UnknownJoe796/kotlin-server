package com.ivieleague.kotlin.server.model

import java.util.*

private val Table_properties = WeakHashMap<Table, Map<String, Property>>()
val Table.properties: Map<String, Property>
    get() = Table_properties.getOrPut(this) {
        HashMap<String, Property>().also {
            for (scalar in this.scalars)
                it[scalar.key] = scalar
            for (link in this.links)
                it[link.key] = link
            for (multilink in this.multilinks)
                it[multilink.key] = multilink
        }
    }

fun Table.defaultRead() = Read(scalars, links.associate { it to Read.EMPTY }, multilinks.associate { it to Read.EMPTY })