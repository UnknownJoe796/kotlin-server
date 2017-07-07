package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationRequest
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.content.FinalContent
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.request.accept
import org.jetbrains.ktor.request.contentType
import org.jetbrains.ktor.response.respondText
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.io.InputStream


val jsonFactory = JsonFactory()

val ContentTypeApplicationBson = ContentType("application", "bson")
val ContentTypeApplicationMessagePack = ContentType("application", "vnd.msgpack")

val JsonObjectMapper = ObjectMapper(jsonFactory)
        .registerModule(KotlinServerModelsModule)!!
val BsonObjectMapper = ObjectMapper(BsonFactory())
        .registerModule(KotlinServerModelsModule)!!
val MessagePackObjectMapper = ObjectMapper(MessagePackFactory())
        .registerModule(KotlinServerModelsModule)!!

suspend fun ApplicationCall.respondJson(result: Any?) {
    val contentType = request.accept()?.let { ContentType.parse(it) }
    when (contentType) {
        ContentTypeApplicationMessagePack -> respond(object : FinalContent.ByteArrayContent() {
            override fun bytes(): ByteArray = MessagePackObjectMapper.writeValueAsBytes(result)
        })
        ContentTypeApplicationBson -> respond(object : FinalContent.ByteArrayContent() {
            override fun bytes(): ByteArray = BsonObjectMapper.writeValueAsBytes(result)
        })
        ContentType.Application.Json, null, ContentType.Application.Any, ContentType.Any -> respondText(JsonObjectMapper.writeValueAsString(result), ContentType.Application.Json)
    }
}

inline suspend fun <reified T> ApplicationRequest.receiveJson(): T? {
    try {
        val contentType = this.contentType()
        return when (contentType) {
            ContentTypeApplicationMessagePack -> MessagePackObjectMapper.readValue(receive<InputStream>(), T::class.java)
            ContentTypeApplicationBson -> MessagePackObjectMapper.readValue(receive<InputStream>(), T::class.java)
            ContentType.Application.Json -> MessagePackObjectMapper.readValue(receive<String>(), T::class.java)
            ContentType.Any -> MessagePackObjectMapper.readValue(receive<String>(), T::class.java)
            else -> throw exceptionBadRequest("Cannot read format $contentType")
        }
    } catch(e: Exception) {
        throw exceptionBadRequest("Bad format")
    }
}