package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SEnum

/**
 * A generated enum type that represents the various ways a query could be sorted.
 */
class SSort private constructor(val type: SClass) : SEnum {
    override val name: String = "${type.name}_Sort"
    override val description: String = "The ways that this can be sorted."
    val breakApart = HashMap<SEnum.Value, Sort>()
    override val values: Set<SEnum.Value> = run {
        val set = HashSet<SEnum.Value>()
        for ((_, field) in type.fields) {
            val ascending = SEnum.Value(
                    name = field.key + "_ascending",
                    description = "Sort on ${field.key} ascending, i.e. 1, 2, 3"
            )
            breakApart[ascending] = Sort(true, field)
            set += ascending
            val descending = SEnum.Value(
                    name = field.key + "_descending",
                    description = "Sort on ${field.key} descending, i.e. 3, 2, 1"
            )
            breakApart[descending] = Sort(false, field)
            set += descending
        }
        set
    }

    companion object {
        private val cache = HashMap<SClass, SSort>()
        operator fun get(type: SClass) = cache.getOrPut(type) { SSort(type) }
    }
}