package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SInterface
import com.ivieleague.kotlin.server.type.SList
import com.ivieleague.kotlin.server.type.TypedObject
import com.lightningkite.kotlin.cast

class SCondition private constructor(val thisType: SClass) : SInterface {
    interface SConditionClass : SClass {
        fun construct(typedObject: TypedObject): Condition
    }

    override val name: String = "Condition<${thisType.name}>"
    override val description: String = "A condition that works on a ${thisType.name} object."
    override val fields: Map<String, SClass.Field<*>> = mapOf()

    val always = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Always"
        override val description: String = "Always."
        override val fields: Map<String, SClass.Field<*>> = mapOf()
        override fun construct(typedObject: TypedObject): Condition = Condition.Always
    }

    val never = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Never"
        override val description: String = "Never."
        override val fields: Map<String, SClass.Field<*>> = mapOf()
        override fun construct(typedObject: TypedObject): Condition = Condition.Never
    }

    val any = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Any"
        override val description: String = "Any of the contained conditions."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "conditions",
                        description = "The list of conditions to check.",
                        type = SList[this@SCondition],
                        default = listOf()
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition = Condition.AnyCondition(
                typedObject["conditions"]!!.cast<List<TypedObject>>().map { wrap(it) }
        )
    }

    val all = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.All"
        override val description: String = "All of the contained conditions."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "conditions",
                        description = "The list of conditions to check.",
                        type = SList[this@SCondition],
                        default = listOf()
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition = Condition.AllConditions(
                typedObject["conditions"]!!.cast<List<TypedObject>>().map { wrap(it) }
        )
    }

    val equal = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Equal"
        override val description: String = "A condition checking if all of the properties listed in [value] are equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.Equal(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    val notEqual = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.NotEqual"
        override val description: String = "A condition checking if all of the properties listed in [value] are not equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.NotEqual(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    val greaterThanOrEqualTo = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.GreaterThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than or equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.NotEqual(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    val lessThanOrEqualTo = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.LessThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than or equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.NotEqual(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    val greaterThan = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.GreaterThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.NotEqual(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    val lessThan = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.LessThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = thisType,
                        default = TypedObject(thisType)
                )
        ).associate { it.key to it }

        override fun construct(typedObject: TypedObject): Condition
                = Condition.NotEqual(typedObject["value"] as? TypedObject ?: TypedObject(thisType))
    }

    override val implementers: Map<String, SClass> = listOf<SClass>(
            always,
            never,
            any,
            all,
            equal,
            notEqual,
            greaterThanOrEqualTo,
            lessThanOrEqualTo,
            greaterThan,
            lessThan
    ).associate { it.name to it }


    companion object {
        private val cache = HashMap<SClass, SCondition>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SCondition(type) }

        fun wrap(typedObject: TypedObject): Condition {
            return (typedObject.type as SConditionClass).construct(typedObject)
        }
    }
}