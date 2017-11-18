package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.*

object SEnumClass : SClass {
    override val name: String = "Enum"
    override val description: String = "Metadata about an enum."

    val fieldFields = TypeField(
            key = "fields",
            description = "A list of the values.",
            type = SList[SEnumValueClass],
            default = listOf()
    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            STypeInterface.fieldName,
            STypeInterface.fieldDescription,
            fieldFields
    ).associate { it.key to it }

    fun make(enum: SEnum) = SimpleTypedObject(this).apply {
        this[STypeInterface.fieldName] = enum.name
        this[STypeInterface.fieldDescription] = enum.description
        this[fieldFields] = enum.values.map { SEnumValueClass.make(it) }
    }
}

