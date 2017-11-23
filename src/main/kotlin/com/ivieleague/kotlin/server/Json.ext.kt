package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
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
import java.io.StringWriter

object JsonGlobals {

    val jsonFactory = JsonFactory()

    val ContentTypeApplicationBson = ContentType("application", "bson")
    val ContentTypeApplicationMessagePack = ContentType("application", "vnd.msgpack")

    val JsonObjectMapper = ObjectMapper(jsonFactory)
            .registerModule(KotlinServerModelsModule)!!
    val BsonObjectMapper = ObjectMapper(BsonFactory())
            .registerModule(KotlinServerModelsModule)!!
    val MessagePackObjectMapper = ObjectMapper(MessagePackFactory())
            .registerModule(KotlinServerModelsModule)!!
    val jsonNodeFactory = JsonObjectMapper.nodeFactory
}

inline fun JsonFactory.generateString(action: JsonGenerator.() -> Unit): String {
    val writer = StringWriter()
    val generator = createGenerator(writer)
    action.invoke(generator)
    return writer.toString()
}

suspend fun ApplicationCall.respondJson(result: Any?, statusCode: HttpStatusCode = HttpStatusCode.OK) {
    val contentType = request.accept()?.let { ContentType.parse(it) }
    when (contentType) {

        JsonGlobals.ContentTypeApplicationMessagePack -> respond(object : FinalContent.ByteArrayContent() {
            val bytes = JsonGlobals.MessagePackObjectMapper.writeValueAsBytes(result)
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

        JsonGlobals.ContentTypeApplicationBson -> respond(object : FinalContent.ByteArrayContent() {
            val bytes = JsonGlobals.BsonObjectMapper.writeValueAsBytes(result)
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
            val bytes = JsonGlobals.JsonObjectMapper.writeValueAsString(result).toByteArray()
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

inline suspend fun <reified T> ApplicationRequest.receiveJson2(): T? {
    val contentType = this.contentType()
    try {
        val noparams = contentType.withoutParameters()
        return when (noparams) {
            JsonGlobals.ContentTypeApplicationMessagePack -> JsonGlobals.MessagePackObjectMapper.readValue(receive<InputStream>(), T::class.java)
            JsonGlobals.ContentTypeApplicationBson -> JsonGlobals.BsonObjectMapper.readValue(receive<InputStream>(), T::class.java)
            ContentType.Application.Json -> JsonGlobals.JsonObjectMapper.readValue(receive<String>(), T::class.java)
            ContentType.Any -> JsonGlobals.JsonObjectMapper.readValue(receive<String>().also { println(it) }, T::class.java)
            else -> throw exceptionBadRequest("Cannot read format '$noparams', supported types are ${listOf(
                    JsonGlobals.ContentTypeApplicationMessagePack,
                    JsonGlobals.ContentTypeApplicationBson,
                    ContentType.Application.Json
            ).joinToString()}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}