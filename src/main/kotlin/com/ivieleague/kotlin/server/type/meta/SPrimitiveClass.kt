package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.SimpleTypedObject
import com.ivieleague.kotlin.server.type.TypeField

object SPrimitiveClass : SClass {
    override val name: String = "PrimitiveType"
    override val description: String = "Metadata about a primitive type."

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            STypeInterface.fieldName,
            STypeInterface.fieldDescription
    ).associate { it.key to it }

    fun make(type: SType<*>) = SimpleTypedObject(this).apply {
        this[STypeInterface.fieldName] = type.name
        this[STypeInterface.fieldDescription] = type.description
    }
}