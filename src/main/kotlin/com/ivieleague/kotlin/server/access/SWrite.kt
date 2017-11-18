package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SWrite private constructor(val type: SHasFields<TypedObject>) : SClass {
    override val name: String
        get() = type.name + "_Write"
    override val description: String
        get() = "A description of what to write for ${type.name}"

    val partialFields = type.fields.values
            .filter { it.type !is SHasFields<*> }
            .associate { field ->
                field to TypeField(
                        key = field.key,
                        description = field.description,
                        type = SPartial[field.type],
                        default = null
                )
            }

    val subwriteFields = type.fields.values
            .filter { it.type is SHasFields<*> }
            .associate { field ->
                @Suppress("UNCHECKED_CAST")
                val fieldType = field.type as SHasFields<TypedObject>
                field to TypeField(
                        key = field.key,
                        description = field.description,
                        type = SPartial[SWrite[fieldType]],
                        default = null
                )
            }

    override val fields: Map<String, TypeField<*>> = partialFields.mapKeys { it.key.key } + (delete.key to delete)

    companion object {
        val delete = TypeField(
                key = "delete",
                description = "Whether or not the object should be deleted",
                type = SBoolean,
                default = false
        )

        private val cache = HashMap<SHasFields<TypedObject>, SWrite>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SWrite(type) }
    }
}