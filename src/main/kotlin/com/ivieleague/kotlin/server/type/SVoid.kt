package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SVoid : SType<Unit> {
    override val kclass = Unit::class
    override fun parse(parser: JsonParser): Unit? = Unit
    override fun parse(node: JsonNode) = Unit
    override fun serialize(generator: JsonGenerator, value: Unit?) = generator.writeNull()
    override val name: String = "Void"
    override val description: String = "A type used to indicate no data."
    override fun reflect(user: TypedObject?): TypedObject = SPrimitiveClass.make(this)
}