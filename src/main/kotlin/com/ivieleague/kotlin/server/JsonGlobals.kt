package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import de.undercouch.bson4jackson.BsonFactory
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.io.StringWriter

object JsonGlobals {

    val jsonFactory = JsonFactory()

    val ContentTypeApplicationJson = "application/json"
    val ContentTypeApplicationBson = "application/bson"
    val ContentTypeApplicationMessagePack = "application/vnd.msgpack"

    val JsonObjectMapper = ObjectMapper(jsonFactory)
    val YamlObjectMapper = ObjectMapper(YAMLFactory())
    val BsonObjectMapper = ObjectMapper(BsonFactory())
    val MessagePackObjectMapper = ObjectMapper(MessagePackFactory())
    val jsonNodeFactory = JsonObjectMapper.nodeFactory
}

inline fun JsonFactory.generateString(action: JsonGenerator.() -> Unit): String {
    val writer = StringWriter()
    val generator = createGenerator(writer)
    action.invoke(generator)
    return writer.toString()
}