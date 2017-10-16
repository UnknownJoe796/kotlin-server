package com.ivieleague.kotlin.server.type

import com.lightningkite.kotlin.cast

class STypedCondition private constructor(val thisType: SClass) : SInterface {
    private interface SConditionClass : SClass {
        fun construct(typedObject: TypedObject): Condition
    }

    override val name: String = "Condition<${thisType.name}>"
    override val description: String = "A condition that works on a ${thisType.name} object."
    override val fields: Map<String, SClass.Field<*>> = mapOf()
    override val implementers: Map<String, SClass> = listOf<SClass>(
            object : SConditionClass {
                override val name: String = "Condition<${thisType.name}>.Always"
                override val description: String = "Always."
                override val fields: Map<String, SClass.Field<*>> = mapOf()
                override fun construct(typedObject: TypedObject): Condition = Condition.Always
            },
            object : SConditionClass {
                override val name: String = "Condition<${thisType.name}>.Never"
                override val description: String = "Never."
                override val fields: Map<String, SClass.Field<*>> = mapOf()
                override fun construct(typedObject: TypedObject): Condition = Condition.Never
            },
            object : SConditionClass {
                override val name: String = "Condition<${thisType.name}>.Any"
                override val description: String = "Any of the contained conditions."
                override val fields: Map<String, SClass.Field<*>> = listOf(
                        SClass.Field(
                                key = "conditions",
                                description = "The list of conditions to check.",
                                type = SList(this@STypedCondition),
                                default = listOf()
                        )
                ).associate { it.key to it }

                override fun construct(typedObject: TypedObject): Condition = Condition.AnyCondition(
                        typedObject["conditions"]!!.cast<List<TypedObject>>().map { wrap(it) }
                )
            },
            object : SConditionClass {
                override val name: String = "Condition<${thisType.name}>.All"
                override val description: String = "All of the contained conditions."
                override val fields: Map<String, SClass.Field<*>> = listOf(
                        SClass.Field(
                                key = "conditions",
                                description = "The list of conditions to check.",
                                type = SList(this@STypedCondition),
                                default = listOf()
                        )
                ).associate { it.key to it }

                override fun construct(typedObject: TypedObject): Condition = Condition.AllConditions(
                        typedObject["conditions"]!!.cast<List<TypedObject>>().map { wrap(it) }
                )
            },
            object : SConditionClass {
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
            },
            object : SConditionClass {
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
            },
            object : SConditionClass {
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
            },
            object : SConditionClass {
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
            },
            object : SConditionClass {
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
            },
            object : SConditionClass {
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
    ).associate { it.name to it }


    companion object {
        private val cache = HashMap<SClass, STypedCondition>()
        operator fun get(type: SClass) = cache.getOrPut(type) { STypedCondition(type) }

        fun wrap(typedObject: TypedObject): Condition {
            return (typedObject.type as SConditionClass).construct(typedObject)
        }
    }
}