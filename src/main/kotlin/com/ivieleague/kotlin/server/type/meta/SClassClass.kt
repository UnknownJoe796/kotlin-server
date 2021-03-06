package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SMap
import com.ivieleague.kotlin.server.type.SimpleTypedObject
import com.ivieleague.kotlin.server.type.TypeField

object SClassClass : SClass {
    override val name: String = "Class"
    override val description: String = "Metadata about a particular class."

    val fieldFields = TypeField(
            key = "fields",
            description = "A list of the fields.",
            type = SMap[SField],
            default = mapOf()
    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            STypeInterface.fieldName,
            STypeInterface.fieldDescription,
            fieldFields
    ).associate { it.key to it }

    fun make(type: SClass) = SimpleTypedObject(this).apply {
        this[STypeInterface.fieldName] = type.name
        this[STypeInterface.fieldDescription] = type.description
        this[fieldFields] = type.fields.mapValues { SField.make(it.value) }
    }
}