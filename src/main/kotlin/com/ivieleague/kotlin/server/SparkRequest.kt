//package com.ivieleague.kotlin.server
//
//import com.fasterxml.jackson.databind.JsonNode
//import com.ivieleague.kotlin.server.type.SType
//import spark.Request
//import spark.Response
//
//private typealias RequestTransformer<T> = (request:Request, type:SType<T>)->T
//
//object SparkRequest {
//
//    val map = HashMap<String, HashMap<SType<*>, RequestTransformer<*>>>()
//
//    fun <T> register(mimeType:String, type: SType<T>, transformer:RequestTransformer<T>){
//        map.getOrPut(mimeType, {HashMap()}).put(type, transformer as RequestTransformer<*>)
//    }
//
//    fun <T> transform(request:Request, type:SType<T>):T{
//        return (map[ContentType.parameterless(request.contentType())]!![type]!!)(request, type) as T
//    }
//}
//
//fun <T> Request.bodyTransformed(type:SType<T>):T{
//    return SparkRequest.transform(this, type)
//}