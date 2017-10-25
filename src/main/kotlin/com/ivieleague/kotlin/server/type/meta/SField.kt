package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.TypedObject

object SField : SClass {
    override val name: String = "Field"
    override val description: String = "A field of a class or interface."

    val fieldName = SClass.Field(
            key = "name",
            description = "The name of the field.",
            type = SString,
            default = null
    )
    val fieldDescription = SClass.Field(
            key = "description",
            description = "A description of the field.",
            type = SString,
            default = null
    )
    val fieldType = SClass.Field(
            key = "type",
            description = "The type of the field.",
            type = SString,
            default = null
    )
//    val fieldDefault = SClass.Field(
//            key = "default",
//            description = "The default of the field.",
//            type = ,
//            default = null
//    )

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            fieldName,
            fieldDescription,
            fieldType
    ).associate { it.key to it }

    fun make(field: SClass.Field<*>) = TypedObject(this).apply {
        this[fieldName] = field.key
        this[fieldDescription] = field.description
        this[fieldType] = field.type.name
    }
}