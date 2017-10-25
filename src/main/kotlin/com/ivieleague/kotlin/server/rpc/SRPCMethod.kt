package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.*

object SRPCMethod : SClass {
    override val name: String = "Method"
    override val description: String = "A method that can be executed on the server using instructions from a client."

    val methodDescription = SClass.Field(
            key = "description",
            description = "A description of the method.",
            type = SString,
            default = null
    )
    val methodArguments = SClass.Field(
            key = "arguments",
            description = "A list of arguments.",
            type = SList[Argument],
            default = listOf()
    )
    val methodReturns = SClass.Field(
            key = "returns",
            description = "A description of what the method returns.",
            type = Returns,
            default = null
    )
    val methodExceptions = SClass.Field(
            key = "exceptions",
            description = "A list of possible exceptions.",
            type = SList[PotentialException],
            default = listOf()
    )
    val methodDeprecated = SClass.Field(
            key = "deprecated",
            description = "Whether or not this method is deprecated.",
            type = SBoolean,
            default = false
    )

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            methodDescription,
            methodArguments
    ).associate { it.key to it }


    object Argument : SClass {
        override val name: String = "Method.Argument"
        override val description: String = "An argument for a method."

        val argKey = SClass.Field(
                key = "key",
                description = "The string key of the argument.",
                type = SString,
                default = null
        )
        val argDescription = SClass.Field(
                key = "description",
                description = "A description of the argument.",
                type = SString,
                default = null
        )
        val argType = SClass.Field(
                key = "type",
                description = "The name of the type of the argument.",
                type = SString,
                default = null
        )

        override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
                argKey,
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.Argument): TypedObject = TypedObject(Argument).apply {
            this[argKey] = item.key
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    object Returns : SClass {
        override val name: String = "Method.Returns"
        override val description: String = "Information about what a method returns."

        val argDescription = SClass.Field(
                key = "description",
                description = "A description of the argument.",
                type = SString,
                default = null
        )
        val argType = SClass.Field(
                key = "type",
                description = "The name of the type of the argument.",
                type = SString,
                default = null
        )

        override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.Returns): TypedObject = TypedObject(Returns).apply {
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    object PotentialException : SClass {
        override val name: String = "Method.PotentialException"
        override val description: String = "Information about how a method may fail."

        val argCode = SClass.Field(
                key = "code",
                description = "The integer code of this exception.",
                type = SInt,
                default = null
        )
        val argName = SClass.Field(
                key = "name",
                description = "The name of this exception.",
                type = SString,
                default = null
        )
        val argDescription = SClass.Field(
                key = "description",
                description = "A description of the exception.",
                type = SString,
                default = null
        )
        val argType = SClass.Field(
                key = "type",
                description = "The name of the type of the data in this exception.",
                type = SString,
                default = null
        )

        override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
                argCode,
                argName,
                argDescription,
                argType
        ).associate { it.key to it }

        fun make(item: RPCMethod.PotentialException<*>): TypedObject = TypedObject(PotentialException).apply {
            this[argCode] = item.code
            this[argName] = item.name
            this[argDescription] = item.description
            this[argType] = item.type.name
        }
    }

    fun make(item: RPCMethod): TypedObject = TypedObject(SRPCMethod).apply {
        this[methodDescription] = item.description
        this[methodReturns] = Returns.make(item.returns)
        this[methodArguments] = item.arguments.map { Argument.make(it) }
        this[methodExceptions] = item.potentialExceptions.values.map { PotentialException.make(it) }
        this[methodDeprecated] = item.deprecated
    }
}