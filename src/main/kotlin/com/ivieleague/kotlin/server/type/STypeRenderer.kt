package com.ivieleague.kotlin.server.type

import spark.Request

typealias STypeRenderFunction<T> = (request: Request, mimeType: String, type: SType<T>, item: T) -> String

interface STypeRenderer {
    val deferToRenderer: STypeRenderer
    val renderMap: MutableMap<String, STypeRenderFunction<*>>
}