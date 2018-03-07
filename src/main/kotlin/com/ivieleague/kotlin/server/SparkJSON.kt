package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import de.undercouch.bson4jackson.BsonFactory
import org.msgpack.jackson.dataformat.MessagePackFactory
import spark.Request
import spark.Response
import java.io.StringWriter

object ServerJackson {

    fun ObjectMapper.setup(): ObjectMapper {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return this
    }

    val json = ObjectMapper().setup()
    val bson = ObjectMapper(BsonFactory()).setup()
    val messagePack = ObjectMapper(MessagePackFactory()).setup()
    val yaml = ObjectMapper(YAMLFactory()).setup()

    fun mapper(type: String?): ObjectMapper {
        val partial = type?.let { ContentType.parameterless(it) } ?: return ServerJackson.json
        return when (partial) {
            ContentType.Application.Json.parameterless() -> ServerJackson.json
            ContentType.Application.Bson.parameterless() -> ServerJackson.bson
            ContentType.Application.MessagePack.parameterless() -> ServerJackson.messagePack
            else -> throw exceptionBadRequest("Content type '$type' not known; expected JSON, BSON, or MessagePack.")
        }
    }

//    fun init(){
//        SparkResponse.register(ContentType.Application.Json.parameterless(), Any::class.java, { item, language ->
//            ServerJackson.json.writeValueAsString(item)
//        })
//        SparkResponse.register(ContentType.Application.Bson.parameterless(), Any::class.java, { item, language ->
//            ServerJackson.bson.writeValueAsString(item)
//        })
//        SparkResponse.register(ContentType.Application.MessagePack.parameterless(), Any::class.java, { item, language ->
//            ServerJackson.messagePack.writeValueAsString(item)
//        })
//    }
}

fun Request.bodyMapper() = ServerJackson.mapper(this.contentType())
fun Request.responseMapper() = ServerJackson.mapper(headers("accept"))

fun Request.bodyAsJson(): JsonNode = bodyAsJson(bodyMapper())

fun Response.respondWithJson(request: Request, jsonNode: JsonNode) = respondWithJson(request.responseMapper(), jsonNode)

fun Request.bodyAsJson(mapper: ObjectMapper): JsonNode {
    return mapper.readTree(this.body())
}

fun Response.respondWithJson(mapper: ObjectMapper, jsonNode: JsonNode) {
    body(mapper.factory.generateString { this.writeTree(jsonNode) })
}

inline fun JsonFactory.generateString(action: JsonGenerator.() -> Unit): String {
    val writer = StringWriter()
    val generator = createGenerator(writer)
    action.invoke(generator)
    return writer.toString()
}