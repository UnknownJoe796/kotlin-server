package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KClass

interface SType<T : Any> {
    val kclass: KClass<*>
    val jclass: Class<*> get() = kclass.java
    fun parse(node: JsonNode): T?
    fun parse(parser: JsonParser): T? = parse(parser.readValueAsTree<JsonNode>())
    fun serialize(generator: JsonGenerator, value: T?)
}