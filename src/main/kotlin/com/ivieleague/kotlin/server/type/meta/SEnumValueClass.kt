package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SEnum
import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.TypedObject

object SEnumValueClass : SClass {
    override val name: String = "Field"
    override val description: String = "A field of a class or interface."

    val fieldName = SClass.Field(
            key = "name",
            description = "The name of the enum value.",
            type = SString,
            default = null
    )
    val fieldDescription = SClass.Field(
            key = "description",
            description = "A description of the enum value.",
            type = SString,
            default = null
    )

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            fieldName,
            fieldDescription
    ).associate { it.key to it }

    fun make(field: SEnum.Value) = TypedObject(this).apply {
        this[fieldName] = field.name
        this[fieldDescription] = field.description
    }
}