package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import kotlin.reflect.KClass

interface SType<T> {
    val name: String
    val description: String
    val dependencies: Collection<SType<*>> get() = listOf()

    val kclass: KClass<*>
    val jclass: Class<*> get() = kclass.java
    fun reflect(): TypedObject

    fun parse(node: JsonNode?): T
    fun parse(parser: JsonParser): T = parse(parser.readValueAsTree<JsonNode>())
    fun serialize(generator: JsonGenerator, value: T)
    fun serialize(factory: JsonNodeFactory, value: T): JsonNode?
}