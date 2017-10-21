package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

object SUntypedCondition : SInterface {

    override val name: String = "UntypedCondition"
    override val description: String = "A condition that that is untyped."
    override val fields: Map<String, SClass.Field<*>> = mapOf()

    val always = object : SClass {
        override val name: String = "UntypedCondition.Always"
        override val description: String = "Always."
        override val fields: Map<String, SClass.Field<*>> = mapOf()
    }

    val never = object : SClass {
        override val name: String = "UntypedCondition.Never"
        override val description: String = "Never."
        override val fields: Map<String, SClass.Field<*>> = mapOf()
    }

    val any = object : SClass {
        override val name: String = "UntypedCondition.Any"
        override val description: String = "Any of the contained conditions."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "conditions",
                        description = "The list of conditions to check.",
                        type = SList(SUntypedCondition),
                        default = listOf()
                )
        ).associate { it.key to it }
    }

    val all = object : SClass {
        override val name: String = "UntypedCondition.All"
        override val description: String = "All of the contained conditions."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "conditions",
                        description = "The list of conditions to check.",
                        type = SList(SUntypedCondition),
                        default = listOf()
                )
        ).associate { it.key to it }
    }

    val equal = object : SClass {
        override val name: String = "UntypedCondition.Equal"
        override val description: String = "A condition checking if all of the properties listed in [value] are equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
    }

    val notEqual = object : SClass {
        override val name: String = "UntypedCondition.NotEqual"
        override val description: String = "A condition checking if all of the properties listed in [value] are not equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
    }

    val greaterThanOrEqualTo = object : SClass {
        override val name: String = "UntypedCondition.GreaterThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than or equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
    }

    val lessThanOrEqualTo = object : SClass {
        override val name: String = "UntypedCondition.LessThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than or equal to the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
    }

    val greaterThan = object : SClass {
        override val name: String = "UntypedCondition.GreaterThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
    }

    val lessThan = object : SClass {
        override val name: String = "UntypedCondition.LessThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than the properties in the object."
        override val fields: Map<String, SClass.Field<*>> = listOf(
                SClass.Field(
                        key = "value",
                        description = "The list of conditions to check.",
                        type = SUntypedMap,
                        default = mapOf<String, Any?>()
                )
        ).associate { it.key to it }
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


    fun unwrap(condition: Condition): TypedObject {
        return when (condition) {
            Condition.Always -> TypedObject(always)
            Condition.Never -> TypedObject(never)
            is Condition.AllConditions -> TypedObject(all).apply {
                this["conditions"] = condition.others.map { unwrap(it) }
            }
            is Condition.AnyCondition -> TypedObject(any).apply {
                this["conditions"] = condition.others.map { unwrap(it) }
            }
            is Condition.Equal -> TypedObject(equal).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            is Condition.NotEqual -> TypedObject(notEqual).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            is Condition.GreaterThanOrEqualTo -> TypedObject(greaterThanOrEqualTo).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            is Condition.LessThanOrEqualTo -> TypedObject(lessThanOrEqualTo).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            is Condition.GreaterThan -> TypedObject(greaterThan).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            is Condition.LessThan -> TypedObject(lessThan).apply {
                this["value"] = condition.value as Map<String, Any?>
            }
            else -> throw IllegalArgumentException("Condition not recognized.")
        }
    }
}