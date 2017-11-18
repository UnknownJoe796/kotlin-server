package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SHasFields
import com.ivieleague.kotlin.server.type.TypeField
import com.ivieleague.kotlin.server.type.TypedObject

class SWriteNotAllowed private constructor(val type: SHasFields<TypedObject>) : SClass {
    override val name: String
        get() = "NotAllowed<${type.name}>"
    override val description: String
        get() = "Information about a write that went wrong for type ${type.name}."
    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            TypeField(
                    key = "write",
                    description = "The part of the write operation that was rejected.",
                    type = SWrite[type],
                    default = null
            ),
            TypeField(
                    key = "condition",
                    description = "The condition under which you can perform an operation like this.",
                    type = SCondition[type],
                    default = null
            )
    ).associate { it.key to it }

    companion object {
        private val cache = HashMap<SHasFields<TypedObject>, SWriteNotAllowed>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SWriteNotAllowed(type) }
    }
}