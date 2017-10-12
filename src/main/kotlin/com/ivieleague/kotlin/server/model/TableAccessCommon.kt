package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Link
import com.ivieleague.kotlin.server.type.Multilink

object TableAccessCommon {

    fun deferPopulateLinks(transaction: Transaction, property: Link, otherAccess: TableAccess, read: Read, idMap: Map<Instance, String>) {
        val table = property.table

        //subquery
        val ids = idMap.entries.associate { it.value to it.key }
        otherAccess.gets(transaction, ids.keys, read).forEach { (refId, instance) -> ids[refId]?.let { it.links[property] = instance } }
    }

    fun deferPopulateMultilinks(transaction: Transaction, property: Multilink, otherAccess: TableAccess, read: Read, idMap: Map<Instance, List<String>>) {
        val table = property.table

        //subquery
        val results = otherAccess.gets(transaction, idMap.flatMap { it.value }, read)
        for ((instance, foreignIds) in idMap) {
            instance.multilinks[property] = foreignIds.mapNotNull { results[it] }
        }
    }

//    fun deferWriteLinks(transaction: Transaction, property: Link, otherAccess: TableAccess, write:WriteResult):WriteResult
}
