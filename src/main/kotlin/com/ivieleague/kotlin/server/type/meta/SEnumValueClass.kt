package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.*

object SEnumValueClass : SClass {
    override val name: String = "Field"
    override val description: String = "A field of a class or interface."

    val fieldName = TypeField(
            key = "name",
            description = "The name of the enum value.",
            type = SString,
            default = null
    )
    val fieldDescription = TypeField(
            key = "description",
            description = "A description of the enum value.",
            type = SString,
            default = null
    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            fieldName,
            fieldDescription
    ).associate { it.key to it }

    fun make(field: SEnum.Value) = SimpleTypedObject(this).apply {
        this[fieldName] = field.name
        this[fieldDescription] = field.description
    }
}