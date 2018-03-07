package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.ServerJackson
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SJson : SType<JsonNode> {
    override val kclass = JsonNode::class
    override fun parse(node: JsonNode?) = node!!
    override fun parse(parser: JsonParser) = parser.readValueAsTree<JsonNode>()
    override fun serialize(generator: JsonGenerator, value: JsonNode) = generator.writeTree(value)
    override fun serialize(factory: JsonNodeFactory, value: JsonNode): JsonNode = value
    override val name: String = "Json"
    override val description: String = "Some JSON data."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: JsonNode = ServerJackson.json.nodeFactory.nullNode()
}