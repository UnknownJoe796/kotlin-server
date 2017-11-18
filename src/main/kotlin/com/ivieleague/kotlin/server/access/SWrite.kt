package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SWrite private constructor(val type: SHasFields<TypedObject>) : SClass {
    override val name: String
        get() = type.name + "_Write"
    override val description: String
        get() = "A description of what to write for ${type.name}"
    override val fields: Map<String, TypeField<*>> = run {
        val it = HashMap<String, TypeField<*>>()

        for (field in type.fields.values) {
            val fieldType = field.type
            val newField = when (fieldType) {
                is SClass -> TypeField(
                        key = field.key,
                        description = field.description,
                        type = SWrite[fieldType],
                        default = null
                )
                else -> TypeField(
                        key = field.key,
                        description = field.description,
                        type = SPartial[field.type],
                        default = null
                )
            }
            it[newField.key] = newField
        }

        it["delete"] = TypeField(
                key = "delete",
                description = "Whether or not the object should be deleted",
                type = SBoolean,
                default = false
        )

        it
    }

    companion object {
        private val cache = HashMap<SHasFields<TypedObject>, SWrite>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SWrite(type) }
    }
}