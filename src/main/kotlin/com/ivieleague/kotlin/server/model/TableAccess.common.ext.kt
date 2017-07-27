package com.ivieleague.kotlin.server.model

fun TableAccess.deferPopulateLinks(transaction: Transaction, property: Link, otherAccess: TableAccess, read: Read, instances: Map<Instance, String>) {
    val table = property.table

    //TODO: Optimize for same-database subqueries
    //subquery
    val ids = instances.entries.associate { it.value to it.key }
    otherAccess.gets(transaction, ids.keys, read).forEach { (refId, instance) -> ids[refId]?.let { it.links[property] = instance } }
}

fun TableAccess.deferPopulateMultilinks(transaction: Transaction, property: Multilink, otherAccess: TableAccess, read: Read, instances: Map<Instance, List<String>>) {
    val table = property.table

    //TODO: Optimize for same-database subqueries
    //subquery
    val results = otherAccess.gets(transaction, instances.flatMap { it.value }, read)
    for ((instance, foreignIds) in instances) {
        instance.multilinks[property] = foreignIds.mapNotNull { results[it] }
    }
}