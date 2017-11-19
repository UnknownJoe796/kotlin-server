package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.*

object SRPCMethod : SClass {
    override val name: String = "Method"
    override val description: String = "A method that can be executed on the server using instructions from a client."

    val methodDescription = TypeField(
            key = "description",
            description = "A description of the method.",
            type = SString
    )
    val methodArguments = TypeField(
            key = "arguments",
            description = "A list of arguments.",
            type = SList[Argument]
    )
    val methodReturns = TypeField(
            key = "returns",
            description = "A description of what the method returns.",
            type = Returns
    )
    val methodExceptions = TypeField(
            key = "exceptions",
            description = "A list of possible exceptions.",
            type = SList[PotentialException]
    )
    val methodDeprecated = TypeField(
            key = "deprecated",
            description = "Whether or not this method is deprecated.",
            type = SBoolean
    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            methodDescription,
            methodArguments,
            methodReturns,
            methodExceptions,
            methodDeprecated
    ).associate { it.key to it }


    object Argument : SClass {
        override val name: String = "Method.Argument"
        override val description: String = "An argument for a method."

        val argKey = TypeField(
                key = "key",
                description = "The string key of the argument.",
                type = SString
        )
        val argDescription = TypeField(
                key = "description",
                description = "A description of the argument.",
                type = SString
        )
        val argType = TypeField(
                key = "type",
                description = "The name of the type of the argument.",
                type = SString
        )

        override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
                argKey,
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.Argument<*>): SimpleTypedObject = SimpleTypedObject(Argument).apply {
            this[argKey] = item.key
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    object Returns : SClass {
        override val name: String = "Method.Returns"
        override val description: String = "Information about what a method returns."

        val argDescription = TypeField(
                key = "description",
                description = "A description of the argument.",
                type = SString
        )
        val argType = TypeField(
                key = "type",
                description = "The name of the type of the argument.",
                type = SString
        )

        override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.Returns<*>): SimpleTypedObject = SimpleTypedObject(Returns).apply {
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    object PotentialException : SClass {
        override val name: String = "Method.PotentialException"
        override val description: String = "Information about how a method may fail."

        val argCode = TypeField(
                key = "code",
                description = "The integer code of this exception.",
                type = SInt
        )
        val argName = TypeField(
                key = "name",
                description = "The name of this exception.",
                type = SString
        )
        val argDescription = TypeField(
                key = "description",
                description = "A description of the exception.",
                type = SString
        )
        val argType = TypeField(
                key = "type",
                description = "The name of the type of the data in this exception.",
                type = SString
        )

        override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
                argCode,
                argName,
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.PotentialException<*>): SimpleTypedObject = SimpleTypedObject(PotentialException).apply {
            this[argCode] = item.code
            this[argName] = item.name
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    fun make(item: RPCMethod): SimpleTypedObject = SimpleTypedObject(SRPCMethod).apply {
        this[methodDescription] = item.description
        this[methodReturns] = Returns.make(item.returns)
        this[methodArguments] = item.arguments.map { Argument.make(it) }
        this[methodExceptions] = item.potentialExceptions.values.map { PotentialException.make(it) }
        this[methodDeprecated] = item.deprecated
    }
}