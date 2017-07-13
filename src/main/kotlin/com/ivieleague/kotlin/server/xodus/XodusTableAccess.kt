package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.model.*
import jetbrains.exodus.entitystore.*
import java.util.*

val Transaction_xodus = WeakHashMap<PersistentEntityStore, WeakHashMap<Transaction, StoreTransaction>>()
fun Transaction.getXodus(store: PersistentEntityStore): StoreTransaction {
    return Transaction_xodus.getOrPut(store) { WeakHashMap() }.getOrPut(this) {
        val txn = if (this.readOnly) store.beginReadonlyTransaction()
        else store.beginTransaction()

        this.onCommit += { txn.commit() }
        this.onFail += { txn.abort() }

        txn
    }
}

val Instance_xodus = WeakHashMap<EntityStore, WeakHashMap<String, Entity>>()
fun Instance.getXodus(transaction: StoreTransaction): Entity? {
    val store = transaction.store
    return Instance_xodus.getOrPut(store, { WeakHashMap() }).getOrPut(this.id, {
        transaction.getEntity(transaction.toEntityId(this.id))
    })
}

fun Instance.setXodus(store: EntityStore, entity: Entity) {
    Instance_xodus.getOrPut(store, { WeakHashMap() })[this.id] = entity
}

class XodusTableAccess(
        val entityStore: PersistentEntityStore,
        val tableAccesses: Fetcher<Table, TableAccess>,
        override val table: Table
) : TableAccess {

    private fun Entity.toInstance(read: Read): Instance? {
        return Instance(
                table = table,
                id = id.toString(),
                scalars = read.scalars.associate { it to getProperty(it.key) }.toMutableMap(),
                links = read.links.entries.associate { it.key to getLink(it.key.key)?.toInstance(it.value) }.toMutableMap(),
                multilinks = read.multilinks.entries.associate { it.key to getLinks(it.key.key).mapNotNull { e -> e.toInstance(it.value) } }.toMutableMap()
        )
    }

    inner class EntityIterablePlus(val entityIterable: EntityIterable? = null, val filters: List<(Entity) -> Boolean> = listOf()) {
        fun toList(transaction: StoreTransaction, read: Read): List<Instance> {
            val iter = entityIterable ?: transaction.getAll(table.tableName)
            return iter.asSequence().filter { entity -> filters.all { it.invoke(entity) } }.mapNotNull { it.toInstance(read) }.toList()
        }
    }

    private fun Condition.makeEntityIterable(
            transaction: StoreTransaction
    ): EntityIterablePlus = when (this) {
        Condition.Always -> EntityIterablePlus(null)
        Condition.Never -> EntityIterablePlus(null, listOf({ it: Entity -> false }))

        is Condition.AllConditions -> if (this.conditions.isEmpty())
            EntityIterablePlus(transaction.getAll(table.tableName))
        else this.conditions.map { it.makeEntityIterable(transaction) }.reduce { a, b ->
            EntityIterablePlus(
                    entityIterable = if (a.entityIterable != null) {
                        if (b.entityIterable != null)
                            a.entityIterable.intersect(b.entityIterable)
                        else
                            a.entityIterable
                    } else {
                        b.entityIterable
                    },
                    filters = a.filters + b.filters
            )
        }

        is Condition.AnyConditions -> if (this.conditions.isEmpty())
            EntityIterablePlus(transaction.getAll(table.tableName))
        else this.conditions.map { it.makeEntityIterable(transaction) }.reduce { a, b ->
            EntityIterablePlus(
                    entityIterable = if (a.entityIterable != null) {
                        if (b.entityIterable != null)
                            a.entityIterable.union(b.entityIterable)
                        else
                            a.entityIterable
                    } else {
                        b.entityIterable
                    },
                    filters = a.filters + b.filters
            )
        }

        is Condition.ScalarEqual -> if (this.path.isEmpty())
            EntityIterablePlus(transaction.find(table.tableName, this.scalar.key, this.value as Comparable<*>))
        else
            defaultIterable(transaction)

        is Condition.ScalarNotEqual -> if (this.path.isEmpty())
            EntityIterablePlus(transaction.getAll(table.tableName).minus(transaction.find(table.tableName, this.scalar.key, this.value as Comparable<*>)))
        else
            defaultIterable(transaction)

        is Condition.ScalarBetween<*> -> if (this.path.isEmpty())
            EntityIterablePlus(transaction.find(table.tableName, this.scalar.key, this.lower, this.upper))
        else
            defaultIterable(transaction)

        is Condition.IdEquals -> defaultIterable(transaction)
        is Condition.MultilinkContains -> defaultIterable(transaction)
        is Condition.MultilinkDoesNotContain -> defaultIterable(transaction)
    }

    private fun Condition.defaultIterable(transaction: StoreTransaction): EntityIterablePlus {
        return EntityIterablePlus(transaction.getAll(table.tableName), listOf({ it: Entity ->
            val read = Read()
            this.dependencies(read)
            it.toInstance(read)?.let { this.invoke(it) } ?: false
        }))
    }

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        val it = transaction.getXodus(entityStore)
        return try {
            it.getEntity(it.toEntityId(id)).toInstance(read)
        } catch(e: EntityRemovedInDatabaseException) {
            throw IllegalArgumentException("ID not found")
        }
    }

    override fun query(transaction: Transaction, condition: Condition, read: Read): List<Instance> {
        val it = transaction.getXodus(entityStore)
        return condition.makeEntityIterable(it).toList(it, read)
    }

    override fun update(transaction: Transaction, write: Write): Instance {
        val linkEntities = HashMap<Link, Instance?>()
        val multilinkEntities = HashMap<Multilink, Collection<Instance>>()
        val txn = transaction.getXodus(entityStore)

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
                val result = tableAccesses[link.table].update(transaction, subwrite)
                val xodus = result.getXodus(txn)
                if (xodus != null) {
                    entity.setLink(link.key, xodus)
                    linkEntities[link] = result
                } else {
                    entity.setProperty(link.key, result.id)
                }
            } else {
                entity.setLink(link.key, null)
            }
        }
        for ((multilink, writes) in write.multilinks) {
            val instances = ArrayList<Instance>()

            val replacements = writes.replacements
            if (replacements != null) {
                entity.deleteLinks(multilink.key)
                entity.deleteProperty(multilink.key)
                val ids = ArrayList<String>()
                for (subwrite in replacements) {
                    val result = tableAccesses[multilink.table].update(transaction, subwrite)
                    val xodus = result.getXodus(txn)
                    if (xodus != null) {
                        entity.addLink(multilink.key, xodus)
                        instances.add(result)
                    } else {
                        ids += result.id
                    }
                }
                if (ids.isNotEmpty())
                    entity.setProperty(multilink.key, ids.joinToString("|", prefix = "|"))
            }

            val additions = writes.additions
            if (additions != null) {
                val ids = ArrayList<String>()
                for (subwrite in additions) {
                    val result = tableAccesses[multilink.table].update(transaction, subwrite)
                    val xodus = result.getXodus(txn)
                    if (xodus != null) {
                        entity.addLink(multilink.key, xodus)
                        instances.add(result)
                    } else {
                        ids += result.id
                    }
                }
                if (ids.isNotEmpty())
                    entity.setProperty(multilink.key, entity.getProperty(multilink.key).toString() + ids.joinToString("|", prefix = "|"))
            }

            val removals = writes.removals
            if (removals != null) {
                val ids = ArrayList<String>()
                for (subwrite in removals) {
                    val result = tableAccesses[multilink.table].update(transaction, subwrite)
                    val xodus = result.getXodus(txn)
                    if (xodus != null) {
                        entity.deleteLink(multilink.key, xodus)
                        instances.add(result)
                    } else {
                        ids += result.id
                    }
                }
                if (ids.isNotEmpty()) {
                    var stringIds = entity.getProperty(multilink.key).toString()
                    for (subId in ids) {
                        stringIds = stringIds.replace("|" + subId, "")
                    }
                    entity.setProperty(multilink.key, stringIds)
                }
            }
        }

        val instance = Instance(table, entity.id.toString(), mutableMapOf(), linkEntities, multilinkEntities)
        instance.setXodus(entityStore, entity)
        return instance
    }

    override fun delete(transaction: Transaction, id: String): Boolean {
        val it = transaction.getXodus(entityStore)
        return try {
            it.getEntity(it.toEntityId(id)).delete()
        } catch(e: EntityRemovedInDatabaseException) {
            throw IllegalArgumentException("ID not found")
        }
    }

}
