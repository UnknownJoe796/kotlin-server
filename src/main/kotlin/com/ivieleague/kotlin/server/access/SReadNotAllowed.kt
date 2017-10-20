package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.STypedCondition

class SReadNotAllowed private constructor(val type: SClass) : SClass {
    override val name: String
        get() = "NotAllowed<${type.name}>"
    override val description: String
        get() = "Information about a write that went wrong for type ${type.name}."
    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            SClass.Field(
                    key = "operation",
                    description = "The part of the read operation that was rejected.",
                    type = SRead[type],
                    default = null
            ),
            SClass.Field(
                    key = "condition",
                    description = "The condition under which you can perform an operation like this.",
                    type = STypedCondition[type],
                    default = null
            )
    ).associate { it.key to it }

    companion object {
        private val cache = HashMap<SClass, SReadNotAllowed>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SReadNotAllowed(type) }
    }
}