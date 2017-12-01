package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SCondition private constructor(val thisType: SHasFields<TypedObject>) : SInterface {
    interface SConditionClass : SClass {
        fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean
    }


    override val name: String = "Condition<${thisType.name}>"
    override val description: String = "A condition that works on a ${thisType.name} object."
    override val fields: Map<String, TypeField<*>> = mapOf()

    val always = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Always"
        override val description: String = "Always."
        override val fields: Map<String, TypeField<*>> = mapOf()
        override fun invoke(condition: TypedObject, testAgainst: TypedObject) = true
    }

    val never = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Never"
        override val description: String = "Never."
        override val fields: Map<String, TypeField<*>> = mapOf()
        override fun invoke(condition: TypedObject, testAgainst: TypedObject) = false
    }

    val fieldConditions = TypeField(
            key = "conditions",
            description = "The list of conditions to check.",
            type = SList[this@SCondition],
            default = listOf()
    )

    val any = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Any"
        override val description: String = "Any of the contained conditions."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldConditions
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            return condition[fieldConditions]?.any { SCondition.invoke(condition, testAgainst) } ?: false
        }
    }

    val all = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.All"
        override val description: String = "All of the contained conditions."
        override val fields: Map<String, TypeField<*>> = listOf(
                fieldConditions
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            return condition[fieldConditions]?.all { SCondition.invoke(condition, testAgainst) } ?: true
        }
    }

    val fieldValue = TypeField(
            key = "value",
            description = "The values to test against.",
            type = SPartialClass[thisType],
            default = SimpleTypedObject(SPartialClass[thisType])
    )

    val equal = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.Equal"
        override val description: String = "A condition checking if all of the properties listed in [value] are equal to the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Any?>
                if(!subvalue.exists) continue
                if (subvalue.value != testAgainst[field]) return false
            }
            return true
        }
    }

    val notEqual = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.NotEqual"
        override val description: String = "A condition checking if all of the properties listed in [value] are not equal to the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Any?>
                if(!subvalue.exists) continue
                if (subvalue.value == testAgainst[field]) return false
            }
            return true
        }
    }

    val greaterThanOrEqualTo = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.GreaterThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than or equal to the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Comparable<Any>?>
                if(!subvalue.exists) continue
                val testAgainstValue = testAgainst[field] as? Comparable<Any> ?: continue
                if (!(testAgainstValue >= subvalue)) return false
            }
            return true
        }
    }

    val lessThanOrEqualTo = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.LessThanOrEqualTo"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than or equal to the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Comparable<Any>?>
                if(!subvalue.exists) continue
                val testAgainstValue = testAgainst[field] as? Comparable<Any> ?: continue
                if (!(testAgainstValue <= subvalue)) return false
            }
            return true
        }
    }

    val greaterThan = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.GreaterThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are greater than the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Comparable<Any>?>
                if(!subvalue.exists) continue
                val testAgainstValue = testAgainst[field] as? Comparable<Any> ?: continue
                if (!(testAgainstValue > subvalue)) return false
            }
            return true
        }
    }

    val lessThan = object : SConditionClass {
        override val name: String = "Condition<${thisType.name}>.LessThan"
        override val description: String = "A condition checking if all of the properties listed in [value] are less than the properties in the object."

        override val fields: Map<String, TypeField<*>> = listOf(
                fieldValue
        ).associate { it.key to it }

        override fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            val valueObj = condition[fieldValue] ?: return true
            for (field in thisType.fields.values) {
                val subvalue = valueObj[field] as Exists<Comparable<Any>?>
                if(!subvalue.exists) continue
                val testAgainstValue = testAgainst[field] as? Comparable<Any> ?: continue
                if (!(testAgainstValue < subvalue)) return false
            }
            return true
        }
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
        private val cache = HashMap<SHasFields<TypedObject>, SCondition>()
        operator fun get(type: SHasFields<TypedObject>) = cache.getOrPut(type) { SCondition(type) }

        fun invoke(condition: TypedObject, testAgainst: TypedObject): Boolean {
            return (condition.type as SConditionClass).invoke(condition, testAgainst)
        }
    }
}