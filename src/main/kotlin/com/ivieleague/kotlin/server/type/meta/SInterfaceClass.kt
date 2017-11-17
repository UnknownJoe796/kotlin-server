package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.*

object SInterfaceClass : SClass {
    override val name: String = "Interface"
    override val description: String = "Metadata about a particular interface."

    val fieldFields = SClass.Field(
            key = "fields",
            description = "A list of the fields.",
            type = SMap[SField],
            default = mapOf()
    )
    val fieldImplementers = SClass.Field(
            key = "implementers",
            description = "A list of all the implementers of this interface.",
            type = SList[SString],
            default = listOf()
    )

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
            STypeInterface.fieldName,
            STypeInterface.fieldDescription,
            fieldFields,
            fieldImplementers
    ).associate { it.key to it }

    fun make(type: SInterface) = SimpleTypedObject(this).apply {
        this[STypeInterface.fieldName] = type.name
        this[STypeInterface.fieldDescription] = type.description
        this[fieldFields] = type.fields.mapValues { SField.make(it.value) }
        this[fieldImplementers] = type.fields.keys.toList()
    }
}