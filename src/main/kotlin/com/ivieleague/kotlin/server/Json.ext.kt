package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationRequest
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.content.FinalContent
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.request.accept
import org.jetbrains.ktor.request.contentType
import org.jetbrains.ktor.response.contentLength
import org.jetbrains.ktor.response.contentType
import org.jetbrains.ktor.util.ValuesMap
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

suspend fun ApplicationCall.respondJson(result: Any?, statusCode: HttpStatusCode = HttpStatusCode.OK) {
    val contentType = request.accept()?.let { ContentType.parse(it) }
    when (contentType) {

        ContentTypeApplicationMessagePack -> respond(object : FinalContent.ByteArrayContent() {
            val bytes = MessagePackObjectMapper.writeValueAsBytes(result)
            override fun bytes(): ByteArray = bytes
            override val headers: ValuesMap by lazy {
                ValuesMap.build(true) {
                    contentType(contentType)
                    contentLength(bytes.size.toLong())
                }
            }
            override val status: HttpStatusCode?
                get() = statusCode
        })

        ContentTypeApplicationBson -> respond(object : FinalContent.ByteArrayContent() {
            val bytes = BsonObjectMapper.writeValueAsBytes(result)
            override fun bytes(): ByteArray = bytes
            override val headers: ValuesMap by lazy {
                ValuesMap.build(true) {
                    contentType(contentType)
                    contentLength(bytes.size.toLong())
                }
            }
            override val status: HttpStatusCode?
                get() = statusCode
        })

        ContentType.Application.Json,
        null,
        ContentType.Application.Any,
        ContentType.Any -> respond(object : FinalContent.ByteArrayContent() {
            val bytes = JsonObjectMapper.writeValueAsString(result).toByteArray()
            override fun bytes(): ByteArray = bytes
            override val headers: ValuesMap by lazy {
                ValuesMap.build(true) {
                    contentType(ContentType.Application.Json)
                    contentLength(bytes.size.toLong())
                }
            }
            override val status: HttpStatusCode?
                get() = statusCode
        })
    }
}

inline suspend fun <reified T> ApplicationRequest.receiveJson(): T? {
    val contentType = this.contentType()
    try {
        return when (contentType) {
            ContentTypeApplicationMessagePack -> MessagePackObjectMapper.readValue(receive<InputStream>(), T::class.java)
            ContentTypeApplicationBson -> BsonObjectMapper.readValue(receive<InputStream>(), T::class.java)
            ContentType.Application.Json -> JsonObjectMapper.readValue(receive<String>(), T::class.java)
            ContentType.Any -> JsonObjectMapper.readValue(receive<String>(), T::class.java)
            else -> throw exceptionBadRequest("Cannot read format $contentType")
        }
    } catch (e: Exception) {
        throw exceptionBadRequest("Malformed $contentType")
    }
}