package com.ivieleague.kotlin.server

fun Table.defaultOutput() = Output(
        properties.values.mapNotNull { it as? Scalar },
        properties.values.mapNotNull { it as? Link }.associate { it to Output() },
        properties.values.mapNotNull { it as? MultiLink }.associate { it to Output() }
)