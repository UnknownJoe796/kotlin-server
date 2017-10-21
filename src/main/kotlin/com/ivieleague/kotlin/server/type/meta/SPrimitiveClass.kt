package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject

object SPrimitiveClass : SClass {
    override val name: String = "PrimitiveType"
    override val description: String = "Metadata about a primitive type."

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            STypeInterface.fieldName,
            STypeInterface.fieldDescription
    ).associate { it.key to it }

    fun make(type: SType<*>) = TypedObject(this).apply {
        this[STypeInterface.fieldName] = type.name
        this[STypeInterface.fieldDescription] = type.description
    }
}