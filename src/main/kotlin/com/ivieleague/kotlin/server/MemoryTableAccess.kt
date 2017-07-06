package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.*
import java.util.*

object MemoryDatabaseAccess : Fetcher<Table, TableAccess> {
    private val cached = HashMap<Table, MemoryTableAccess>()
    override fun get(key: Table): TableAccess = cached.getOrPut(key) {
        MemoryTableAccess(this, key)
    }
}

class MemoryTableAccess(val tableAccessFetcher: Fetcher<Table, TableAccess>, override val table: Table) : TableAccess {

    val data = HashMap<String, HashMap<Property, Any?>>()

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        return data[id]?.let { instanceFromRow(user, id, read, it) }
    }

    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
        val rows = data.entries.filter { evaluateCondition(condition, it.key, it.value) }
        return rows.map { instanceFromRow(user, it.key, read, it.value) }
    }

    override fun update(user: Instance?, write: Write): Instance {
        val resultLinks = HashMap<Link, Instance?>()
        val resultMultilinks = HashMap<Multilink, Collection<Instance>>()
        val id = write.id ?: UUID.randomUUID().toString()
        val row = data.getOrPut(id) { HashMap() }
        for ((scalar, value) in write.scalars) {
            row[scalar] = value
        }
        for ((link, value) in write.links) {
            if (value == null) {
                row[link] = null
            } else {
                val newInstance = tableAccessFetcher[link.table].update(user, value)
                row[link] = newInstance.id
                resultLinks[link] = newInstance
            }
        }
        for ((link, mutation) in write.multilinks) {
            val replacements = mutation.replacements
            val removals = mutation.removals
            val additions = mutation.additions
            if (replacements != null) {
                val new = replacements.map { item -> tableAccessFetcher[link.table].update(user, item) }
                row[link] = new.map { it.id }
                resultMultilinks[link] = new
            }
            if (removals != null) {
                (row[link] as MutableList<String>).removeAll(removals.map {
                    item ->
                    tableAccessFetcher[link.table].update(user, item).id
                })
            }
            if (additions != null) {
                val new = additions.map { item -> tableAccessFetcher[link.table].update(user, item) }
                (row[link] as MutableList<String>).addAll(new.map { it.id })
                resultMultilinks[link] = new
            }
        }
        return Instance(table, id, mutableMapOf(), resultLinks, resultMultilinks)
    }

    override fun delete(user: Instance?, id: String): Boolean = data.remove(id) != null

    private fun evaluateCondition(condition: Condition, key: String, row: HashMap<Property, Any?>): Boolean {
        return when (condition) {
            Condition.Always -> true
            Condition.Never -> false
            is Condition.AllConditions -> condition.conditions.all { evaluateCondition(it, key, row) }
            is Condition.AnyConditions -> condition.conditions.any { evaluateCondition(it, key, row) }
            is Condition.ScalarEqual -> TODO() //row[condition.scalar] == condition.equals
            is Condition.ScalarNotEqual -> TODO() //row[condition.scalar] != condition.doesNotEqual
            is Condition.IdEquals -> TODO() //key == condition.equals
            is Condition.ScalarBetween<*> -> TODO() //(row[condition.scalar] as Comparable<Any>) in (condition.lower as Comparable<Any>..condition.upper as Comparable<Any>)
            is Condition.MultilinkContains -> TODO()
            is Condition.MultilinkDoesNotContain -> TODO()
        }
    }

    private fun instanceFromRow(user: Instance?, id: String, output: Read, row: HashMap<Property, Any?>): Instance {
        return Instance(
                table = table,
                id = id,
                scalars = output.scalars.associate { it to row[it] }.toMutableMap(),
                links = output.links.entries.associate {
                    val result = (row[it.key] as? String)?.let { otherId ->
                        tableAccessFetcher[it.key.table].get(user, otherId, it.value)
                    }
                    it.key to result
                }.toMutableMap(),
                multilinks = output.multilinks.entries.associate {
                    val result = (row[it.key] as? List<String>?)?.mapNotNull { otherId ->
                        tableAccessFetcher[it.key.table].get(user, otherId, it.value)
                    } ?: listOf()
                    it.key to result
                }.toMutableMap()
        )
    }
}