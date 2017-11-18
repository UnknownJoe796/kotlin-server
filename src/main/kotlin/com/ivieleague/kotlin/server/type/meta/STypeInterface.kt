package com.ivieleague.kotlin.server.type.meta

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SInterface
import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.TypeField

object STypeInterface : SInterface {
    override val name: String = "Type"
    override val description: String = "A type."

    val fieldName = TypeField(
            key = "name",
            description = "The name of the type.",
            type = SString,
            default = null
    )

    val fieldDescription = TypeField(
            key = "description",
            description = "A description of the type.",
            type = SString,
            default = null
    )

    override val fields: Map<String, TypeField<*>> = listOf<TypeField<*>>(
            fieldName,
            fieldDescription
    ).associate { it.key to it }

    override val implementers: Map<String, SClass> = listOf(
            SPrimitiveClass,
            SClassClass,
            SInterfaceClass
    ).associate { it.name to it }
}