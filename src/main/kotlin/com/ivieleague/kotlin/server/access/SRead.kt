package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.*

class SRead private constructor(val type: SClass) : SClass {
    override val name: String
        get() = type.name + "_Read"
    override val description: String
        get() = "A description of what to read for ${type.name}"

    val fieldCondition = SClass.Field(
            key = "condition",
            description = "The condition to query for",
            type = SCondition[type],
            default = null
    )
    val fieldSort = SClass.Field(
            key = "sort",
            description = "The things to sort on",
            type = SList[SSort[type]],
            default = listOf()
    )
    val fieldField = SClass.Field(
            key = "startAfter",
            description = "The object to start after",
            type = type,
            default = null
    )
    val fieldCount = SClass.Field(
            key = "count",
            description = "The maximum number of instances to return",
            type = SInt,
            default = 100
    )

    override val fields: Map<String, SClass.Field<*>> = run {
        val it = HashMap<String, SClass.Field<*>>()

        for (field in type.fields.values) {
            val fieldType = field.type
            val newField = when (fieldType) {
                is SClass -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = SRead[fieldType],
                        default = null
                )
                else -> SClass.Field(
                        key = field.key,
                        description = field.description,
                        type = SBoolean,
                        default = false
                )
            }
            it[newField.key] = newField
        }

        it += listOf(
                fieldCondition,
                fieldSort,
                fieldField,
                fieldCount
        ).associate { it.key to it }

        it
    }

    companion object {
        private val cache = HashMap<SClass, SRead>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SRead(type) }
    }
}

class SSort private constructor(val type: SClass) : SEnum {
    override val name: String = "${type.name}_Sort"
    override val description: String = "The ways that this can be sorted."
    val breakApart = HashMap<SEnum.Value, Pair<SClass.Field<*>, Boolean>>()
    override val values: Set<SEnum.Value> = run {
        val set = HashSet<SEnum.Value>()
        for ((_, field) in type.fields) {
            val ascending = SEnum.Value(
                    name = field.key + "_ascending",
                    description = "Sort on ${field.key} ascending, i.e. 1, 2, 3"
            )
            breakApart[ascending] = field to false
            set += ascending
            val descending = SEnum.Value(
                    name = field.key + "_descending",
                    description = "Sort on ${field.key} descending, i.e. 3, 2, 1"
            )
            breakApart[descending] = field to true
            set += descending
        }
        set
    }

    companion object {
        private val cache = HashMap<SClass, SSort>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SSort(type) }
    }
}