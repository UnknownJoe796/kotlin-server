package com.ivieleague.kotlin.server

import java.util.*

class SimpleMemoryDao : DAO {

    val data = HashMap<String, MutableMap<String, Any?>>()

    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
        val row = data[id] ?: return null
        return Instance(table, id, properties.associate {
            it to row[it.name]
        })
    }

    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
        val endId = id ?: UUID.randomUUID().toString()
        val row = data.getOrPut(endId) { HashMap() }
        row.putAll(inProperties.entries.map { it.key.name to it.value })
        return Instance(table, endId, outProperties.associate {
            it to row[it.name]
        })
    }

    override fun query(table: Table, queryConditions: List<Condition>, outProperties: Collection<Property>): Collection<Instance> {
        return data
                .asSequence()
                .filter { (_, row) ->
                    queryConditions.all {
                        when (it) {
                            is Condition.Between -> TODO()
                            is Condition.Equal -> row[it.property.name] == it.value
                            is Condition.NotEqual -> row[it.property.name] != it.value
                            is Condition.Comparison -> TODO()
                        }
                    }
                }
                .mapNotNull { get(table, it.key, outProperties) }
                .toList()
    }
}