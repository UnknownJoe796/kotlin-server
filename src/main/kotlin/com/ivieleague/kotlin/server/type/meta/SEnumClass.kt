package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SEnum
import com.ivieleague.kotlin.server.type.SList
import com.ivieleague.kotlin.server.type.SimpleTypedObject

object SEnumClass : SClass {
    override val name: String = "Enum"
    override val description: String = "Metadata about an enum."

    val fieldFields = SClass.Field(
            key = "fields",
            description = "A list of the values.",
            type = SList[SEnumValueClass],
            default = listOf()
    )

    override val fields: Map<String, SClass.Field<*>> = listOf<SClass.Field<*>>(
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

