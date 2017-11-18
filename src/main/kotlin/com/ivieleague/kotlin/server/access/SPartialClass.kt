package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SPartialClass private constructor(val type: SHasFields<TypedObject>) : SClass {
    override val name: String
        get() = type.name + "_Partial"
    override val description: String
        get() = "A partial version of ${type.name}"
    override val fields: Map<String, TypeField<*>> = run {
        val it = HashMap<String, TypeField<*>>()

        for (field in type.fields.values) {
            val newField = TypeField(
                    key = field.key,
                    description = field.description,
                    type = SPartial[field.type],
                    default = null
            )
            it[newField.key] = newField
        }

        it
    }

    companion object {
        private val cache = HashMap<SHasFields<TypedObject>, SPartialClass>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SPartialClass(type) }
    }
}