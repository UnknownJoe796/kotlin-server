package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SBoolean
import com.ivieleague.kotlin.server.type.SClass

class SWrite private constructor(val type: SClass) : SClass {
    override val name: String
        get() = type.name + "_Write"
    override val description: String
        get() = "A description of what to write for ${type.name}"
    override val fields: Map<String, SClass.Field<*>> = run {
        val it = HashMap<String, SClass.Field<*>>()

        for (field in type.fields.values) {
            val fieldType = field.type
            val newField = when (fieldType) {
                is SClass -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = SWrite[fieldType],
                        default = null
                )
                else -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = field.type,
                        default = null
                )
            }
            it[newField.key] = newField
        }

        it["delete"] = SClass.Field(
                key = "delete",
                description = "Whether or not the object should be deleted",
                type = SBoolean,
                default = false
        )

        it
    }

    companion object {
        private val cache = HashMap<SClass, SWrite>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SWrite(type) }
    }
}