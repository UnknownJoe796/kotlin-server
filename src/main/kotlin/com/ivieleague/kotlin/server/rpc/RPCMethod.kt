package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject


interface RPCMethod {
    val description: String
    val arguments: List<Argument>
    val returns: Returns
    val potentialExceptions: Map<Int, PotentialException<*>>

//    //builder fun
//    fun <T: Any> potentialException(
//            code:Int,
//            name:String,
//            description: String,
//            type: SType<T>
//    ): PotentialException<T> {
//        val pE = PotentialException(
//                code = code,
//                name = name,
//                description = description,
//                type = type
//        )
//        potentialExceptions[pE.code] = pE
//        return pE
//    }

    data class Argument(
            val key: String,
            val description: String,
            val type: SType<*>
    )

    data class Returns(
            val description: String,
            val type: SType<*>
    )

    data class PotentialException<T : Any>(
            val code: Int,
            val name: String,
            val description: String,
            val type: SType<T>
    ) {
        fun exception(data: T? = null, message: String = description) = RPCException(RPCError(
                code = code,
                message = message,
                data = data
        ))
    }

    @Throws(RPCException::class)
    operator fun invoke(user: TypedObject?, arguments: Map<String, Any?>): Any?
}

