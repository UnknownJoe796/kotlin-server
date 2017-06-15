package com.ivieleague.kotlin.server

import com.lightningkite.kotlin.collection.Cache
import java.util.*

class MemoryDAO : DAO {

    val tables = Cache<Table, HashMap<String, HashMap<Property, Any?>>> { HashMap() }

    override fun get(table: Table, id: String, output: Output): Instance? {
        val row = tables[table]!![id] ?: return null
        return instanceFromRow(table, id, output, row)
    }

    private fun instanceFromRow(table: Table, id: String, output: Output, row: java.util.HashMap<Property, Any?>): Instance {
        return Instance(
                table = table,
                id = id,
                scalars = output.scalars.associate { it to row[it] },
                links = output.links.entries.associate {
                    val result = (row[it.key] as? String)?.let { otherId ->
                        get(it.key.table, otherId, it.value)
                    }
                    it.key to result
                },
                multilinks = output.multilinks.entries.associate {
                    val result = (row[it.key] as? List<String>?)?.mapNotNull { otherId ->
                        get(it.key.table, otherId, it.value)
                    } ?: listOf()
                    it.key to result
                }
        )
    }

    override fun query(table: Table, condition: Condition, output: Output): Collection<Instance> = tables[table]!!.entries.filter {
        condition.invoke(it.value)
    }.map { instanceFromRow(table, it.key, output, it.value) }

    override fun update(table: Table, input: Input): Instance {
        val id = input.id ?: UUID.randomUUID().toString()
        val row = tables[table]!!.getOrPut(id) { HashMap() }
        for ((scalar, value) in input.scalars) {
            row[scalar] = value
        }
        for ((link, value) in input.links) {
            row[link] = update(link.table, value).id
        }
        for ((link, value) in input.multilinkReplacements) {
            row[link] = value.map { item -> update(link.table, item).id }.toMutableList()
        }
        for ((link, value) in input.multilinkSubtractions) {
            (row[link] as MutableList<String>).removeAll(value.map { item -> update(link.table, item).id }.toMutableList())
        }
        for ((link, value) in input.multilinkAdditions) {
            (row[link] as MutableList<String>).addAll(value.map { item -> update(link.table, item).id }.toMutableList())
        }
        return get(table, id, input.toOutput())!!
    }

    override fun delete(table: Table, id: String): Boolean {
        return tables[table]!!.remove(id) != null
    }
}