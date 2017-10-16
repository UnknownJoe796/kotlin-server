package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SBoolean
import com.ivieleague.kotlin.server.type.SClass

class SRead private constructor(val type: SClass) : SClass {
    override val name: String
        get() = type.name + "_Read"
    override val description: String
        get() = "A description of what to read for ${type.name}"
    override val fields: Map<String, SClass.Field<*>> = run {
        val it = HashMap<String, SClass.Field<*>>()

        for (field in type.fields.values) {
            val fieldType = field.type
            val newField = when (fieldType) {
                is SClass -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = SRead[fieldType],
                        default = null
                )
                else -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = SBoolean,
                        default = false
                )
            }
            it[newField.key] = newField
        }

        it
    }

    companion object {
        private val cache = HashMap<SClass, SRead>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SRead(type) }
    }
}