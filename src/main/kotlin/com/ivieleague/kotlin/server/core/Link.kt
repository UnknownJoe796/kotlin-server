package com.ivieleague.kotlin.server.core

class Link(
        override val key: String,
        override val description: String,
        override val startVersion: Int = 0,
        override val endVersion: Int = Int.MAX_VALUE,
        override val readOnly: Boolean = false,
        val table: Table
) : Property