package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SPartialClass private constructor(val type: SHasFields<TypedObject>) : SClass {
    override val name: String
        get() = type.name + "_Partial"
    override val description: String
        get() = "A partial version of ${type.name}"

    val mappedFields: Map<TypeField<*>, TypeField<Exists<Any?>>> = type.fields.values.associate { field ->
        field to TypeField<Exists<Any?>>(
                key = field.key,
                description = field.description,
                type = SPartial[field.type] as SPartial<Any?>,
                default = Exists()
        )
    }

    override val fields: Map<String, TypeField<*>> = mappedFields.values.associate { it.key to it }

    @Suppress("UNCHECKED_CAST")
    fun pullFromPartial(into: MutableTypedObject, fromPartial: TypedObject) {
        for (field in this.type.fields) {
            val untypedField = field as TypeField<Any?>
            val untypedPartialField = mappedFields[untypedField] as TypeField<Exists<Any?>>
            val exists = fromPartial[untypedPartialField]
            if(exists.exists) {
                into[untypedField] = exists.value
            }
        }
    }

    companion object {
        private val cache = HashMap<SHasFields<TypedObject>, SPartialClass>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SPartialClass(type) }
    }
}