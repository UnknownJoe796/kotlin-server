//package com.ivieleague.kotlin.server
//
//import com.ivieleague.kotlin.server.type.SType
//import spark.Request
//import spark.Response
//
//
//private typealias ResponseTransformer<T> = (headers:Map<String, String>, item:T, type:SType<T>, response:Response)->Unit
//
//object SparkResponse {
//
//    val map = HashMap<String, HashMap<SType<*>, ResponseTransformer<*>>>()
//
//    fun <T> register(mimeType:String, type: SType<T>, transformer:ResponseTransformer<T>){
//        map.getOrPut(mimeType, {HashMap()}).put(type, transformer as ResponseTransformer<*>)
//    }
//
//    fun <T> transform(request:Request, type: SType<T>, item:T, response:Response){
//        request.headers()
//        val accepted = request.acceptList()
//        //Prioritize content type over type accuracy
//        val mimeTypeSet = accepted.asSequence()
//                .mapNotNull { map[it.parameterless()] }
//                .firstOrNull() ?: throw exceptionBadRequest("Content types ${request.headers("accept")} not supported.")
//        return (mimeTypeSet[type]!!)(request, item, type, response)
//    }
//}
//
//fun <T> Response.respondTransformed(data:T, type: SType<T>){
//    return SparkRequest.transform(this, type)
//}