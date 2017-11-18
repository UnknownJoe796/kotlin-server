package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.SimpleTypedObject
import com.ivieleague.kotlin.server.type.TypeField

object SField : SClass {
    override val name: String = "Field"
    override val description: String = "A field of a class or interface."

    val fieldName = TypeField(
            key = "name",
            description = "The name of the field.",
            type = SString,
            default = null
    )
    val fieldDescription = TypeField(
            key = "description",
            description = "A description of the field.",
            type = SString,
            default = null
    )
    val fieldType = TypeField(
            key = "type",
            description = "The type of the field.",
            type = SString,
            default = null
    )
//    val fieldDefault = TypeField(
//            key = "default",
//            description = "The default of the field.",
//            type = ,
//            default = null
//    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            fieldName,
            fieldDescription,
            fieldType
    ).associate { it.key to it }

    fun make(field: TypeField<*>) = SimpleTypedObject(this).apply {
        this[fieldName] = field.key
        this[fieldDescription] = field.description
        this[fieldType] = field.type.name
    }
}