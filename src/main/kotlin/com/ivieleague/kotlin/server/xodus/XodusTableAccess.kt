package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.model.*
import jetbrains.exodus.entitystore.*

class XodusAccess(val entityStore: PersistentEntityStore) : Fetcher<Table, XodusTableAccess> {

    val cached = HashMap<Table, XodusTableAccess>()

    override fun get(key: Table): XodusTableAccess = cached.getOrPut(key) { XodusTableAccess(this, key) }
}

class XodusTableAccess(
        val xodusAccess: XodusAccess,
        override val table: Table
) : TableAccess {

    private fun Entity.toInstance(read: Read): Instance? {
        return Instance(
                id = id.toString(),
                scalars = read.scalars.associate { it to getProperty(it.key) },
                links = read.links.entries.associate { it.key to getLink(it.key.key)?.toInstance(it.value) },
                multilinks = read.multilinks.entries.associate { it.key to getLinks(it.key.key).mapNotNull { e -> e.toInstance(it.value) } }
        )
    }

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        return xodusAccess.entityStore.beginReadonlyTransaction().use {
            try {
                it.getEntity(it.toEntityId(id)).toInstance(read)
            } catch(e: EntityRemovedInDatabaseException) {
                throw IllegalArgumentException("ID not found")
            }
        }
    }

    private fun Condition.makeEntityIterable(transaction: StoreTransaction): EntityIterable = when (this) {
        Condition.Always -> transaction.getAll(table.tableName)
        Condition.Never -> TODO()

        is Condition.AllCondition -> if (this.conditions.isEmpty())
            transaction.getAll(table.tableName)
        else
            this.conditions.map { it.makeEntityIterable(transaction) }.reduce { a, b -> a.intersect(b) }

        is Condition.AnyCondition -> if (this.conditions.isEmpty())
            transaction.getAll(table.tableName)
        else
            this.conditions.map { it.makeEntityIterable(transaction) }.reduce { a, b -> a.union(b) }

        is Condition.ScalarEqual -> transaction.find(table.tableName, this.scalar.key, this.equals as Comparable<*>)
        is Condition.ScalarNotEqual -> transaction.getAll(table.tableName).minus(transaction.find(table.tableName, this.scalar.key, this.doesNotEqual as Comparable<*>))
        is Condition.ScalarBetween<*> -> transaction.find(table.tableName, this.scalar.key, this.lower, this.upper)
    }

    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
        return xodusAccess.entityStore.beginReadonlyTransaction().use {
            condition.makeEntityIterable(it).mapNotNull { it.toInstance(read) }
        }
    }

    override fun update(user: Instance?, write: Write): Instance {
        return xodusAccess.entityStore.beginTransaction().use {
            privateWrite(write, it).second
        }
    }

    private fun privateWrite(write: Write, txn: StoreTransaction): Pair<Entity, Instance> {

        val linkEntities = HashMap<Link, Instance?>()
        val multilinkEntities = HashMap<Multilink, List<Instance>>()

        val id = write.id
        val entity = if (id == null) txn.newEntity(table.tableName) else try {
            txn.getEntity(txn.toEntityId(id))
        } catch(e: EntityRemovedInDatabaseException) {
            throw IllegalArgumentException("ID not found")
        }
        for ((scalar, value) in write.scalars) {
            entity.setProperty(scalar.key, value as Comparable<*>)
        }
        for ((link, subwrite) in write.links) {
            if (subwrite != null) {
                val result = xodusAccess[link.table].privateWrite(subwrite, txn)
                entity.setLink(link.key, result.first)
                linkEntities[link] = result.second
            } else {
                entity.setLink(link.key, null)
            }
        }
        for ((multilink, writes) in write.multilinks) {
            val instances = ArrayList<Instance>()

            val replacements = writes.replacements
            if (replacements != null) {
                entity.deleteLinks(multilink.key)
                for (subwrite in replacements) {
                    val result = xodusAccess[multilink.table].privateWrite(subwrite, txn)
                    entity.addLink(multilink.key, result.first)
                    instances.add(result.second)
                }
            }

            val additions = writes.additions
            if (additions != null) {
                for (subwrite in additions) {
                    val result = xodusAccess[multilink.table].privateWrite(subwrite, txn)
                    entity.addLink(multilink.key, result.first)
                    instances.add(result.second)
                }
            }

            val removals = writes.removals
            if (removals != null) {
                for (subwrite in removals) {
                    val result = xodusAccess[multilink.table].privateWrite(subwrite, txn)
                    entity.deleteLink(multilink.key, result.first)
                }
            }
        }

        return entity to Instance(entity.id.toString(), mapOf(), linkEntities, multilinkEntities)
    }

    override fun delete(user: Instance?, id: String): Boolean {
        return xodusAccess.entityStore.beginTransaction().use {
            try {
                it.getEntity(it.toEntityId(id)).delete()
            } catch(e: EntityRemovedInDatabaseException) {
                throw IllegalArgumentException("ID not found")
            }
        }
    }

}