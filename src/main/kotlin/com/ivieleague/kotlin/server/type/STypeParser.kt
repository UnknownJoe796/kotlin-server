package com.ivieleague.kotlin.server.type

import spark.Request

typealias STypeParseFunction<T> = (request: Request, mimeType: String, default: T) -> T

interface STypeParser {
    val deferToParser: STypeParser
    val parseMap: MutableMap<String, STypeParseFunction<*>>
}