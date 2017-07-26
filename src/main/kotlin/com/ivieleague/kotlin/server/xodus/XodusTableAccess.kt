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

    private fun Entity.toInstance(transaction: StoreTransaction, read: Read): Instance? {
        return Instance(
                table = table,
                id = id.toString(),
                scalars = read.scalars.associate { it to getProperty(it.key) }.toMutableMap(),
                links = read.links.entries.associate { it.key to getLink(it.key.key)?.toInstance(transaction, it.value) }.toMutableMap(),
                multilinks = read.multilinks.entries.associate {
                    val plus = EntityIterablePlusAlt(getLinks(it.key.key)).applyCondition(transaction, it.value.condition)
                    it.key to plus.toInstances(transaction, it.value)
                }.toMutableMap()
        )
    }

    fun Condition.invoke(transaction: StoreTransaction, entity: Entity): Boolean {
        val read = Read()
        this.dependencies(read)
        return entity.toInstance(transaction, read)?.let { this.invoke(it) } ?: false
    }

    inner class EntityIterablePlusAlt(var entityIterable: EntityIterable, var filter: ((Entity) -> Boolean)? = null) : Iterable<Entity> {
        fun andFilter(condition: Condition) = andFilter { condition.invoke(entityIterable.transaction, it) }
        fun andFilter(newFilter: (Entity) -> Boolean) {
            val oldFilter = filter
            if (oldFilter == null) {
                filter = newFilter
            } else {
                filter = { oldFilter.invoke(it) && newFilter.invoke(it) }
            }
        }

        fun orFilter(condition: Condition) = orFilter { condition.invoke(entityIterable.transaction, it) }
        fun orFilter(newFilter: (Entity) -> Boolean) {
            val oldFilter = filter
            if (oldFilter == null) {
                filter = newFilter
            } else {
                filter = { oldFilter.invoke(it) || newFilter.invoke(it) }
            }
        }

        override fun iterator(): Iterator<Entity> {
            val filter = filter
            return if (filter == null)
                entityIterable.asSequence().iterator()
            else
                entityIterable.asSequence().filter(filter).iterator()
        }

        fun copy() = EntityIterablePlusAlt(entityIterable, filter)
        fun toInstances(transaction: StoreTransaction, read: Read): List<Instance> {
            val startAfter = read.startAfter
            val comparator = if (read.sort.isNotEmpty()) {
                { a: Entity, b: Instance -> a.id.localId < b.id.substring(b.id.indexOf('-') + 1).toLongOrNull() ?: 0L }
            } else lambdaBefore(read.sort)

            val seq = if (startAfter == null)
                asSequence()
            else
                asSequence().dropWhile {
                    comparator.invoke(it, startAfter)
                }

            if (read.sort.isEmpty())
                return seq.take(read.count).mapNotNull { it.toInstance(transaction, read) }.toList()
            else
                return seq.sortedWith(comparator(read.sort)).take(read.count).mapNotNull { it.toInstance(transaction, read) }.toList()
        }
    }

    fun comparator(sort: List<Sort>) = object : kotlin.Comparator<Entity> {

        @Suppress("UNCHECKED_CAST")
        val comparators = sort.map { it.comparator() as SortComparator<Any?> }

        override fun compare(a: Entity, b: Entity): Int {
            for (comp in comparators) {
                val scalar = comp.sort.scalar
                val result = comp.compare(a.getProperty(scalar.key), b.getProperty(scalar.key))
                if (result != 0) return result
            }
            return a.id.compareTo(b.id)
        }
    }

    fun lambdaBefore(sort: List<Sort>): (Entity, Instance) -> Boolean = object : (Entity, Instance) -> Boolean {

        @Suppress("UNCHECKED_CAST")
        val comparators = sort.map { it.comparator() as SortComparator<Any?> }

        override fun invoke(a: Entity, b: Instance): Boolean {
            for (comp in comparators) {
                val scalar = comp.sort.scalar
                val result = comp.compare(a.getProperty(scalar.key), b.scalars[scalar])
                if (result != 0) return result == -1
            }
            return a.id.localId < b.id.substring(b.id.indexOf('-') + 1).toLongOrNull() ?: 0L
        }
    }

    fun EntityIterablePlusAlt.applyCondition(transaction: StoreTransaction, condition: Condition): EntityIterablePlusAlt {
        when (condition) {
            Condition.Always -> {
            }
            Condition.Never -> {
                filter = { false }
            }
            is Condition.AllConditions -> {
                condition.conditions.forEach { applyCondition(transaction, it) }
            }
            is Condition.AnyConditions -> {
                val iterables = condition.conditions.map { this.copy().applyCondition(transaction, it) }
                if (iterables.all { it.filter == null }) {
                    entityIterable = iterables.asSequence().map { it.entityIterable }.reduce { acc, other -> acc.union(other) }
                } else {
                    entityIterable = iterables.asSequence().map { it.entityIterable }.reduce { acc, other -> acc.union(other) }
                    andFilter { condition.conditions.any { sub -> sub.invoke(transaction, it) } }
                }
            }
            is Condition.ScalarEqual -> {
                if (condition.path.isEmpty())
                    entityIterable = entityIterable.intersect(transaction.find(table.tableName, condition.scalar.key, condition.value as Comparable<*>))
                else
                    andFilter(condition)
            }
            is Condition.ScalarNotEqual -> {
                if (condition.path.isEmpty())
                    entityIterable = entityIterable.intersect(
                            transaction.getAll(table.tableName).minus(transaction.find(table.tableName, condition.scalar.key, condition.value as Comparable<*>))
                    )
                else
                    andFilter(condition)
            }
            is Condition.ScalarBetween<*> -> {
                if (condition.path.isEmpty())
                    entityIterable = entityIterable.intersect(
                            transaction.find(table.tableName, condition.scalar.key, condition.lower, condition.upper)
                    )
                else
                    andFilter(condition)
            }
            is Condition.IdEquals -> {
                andFilter { it.id.toString() == condition.id }
            }
            is Condition.MultilinkContains -> {
                andFilter(condition)
            }
            is Condition.MultilinkDoesNotContain -> {
                andFilter(condition)
            }
        }
        return this
    }

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        val it = transaction.getXodus(entityStore)
        return try {
            it.getEntity(it.toEntityId(id)).toInstance(it, read)
        } catch(e: EntityRemovedInDatabaseException) {
            throw IllegalArgumentException("ID not found")
        }
    }

    override fun query(transaction: Transaction, read: Read): List<Instance> {
        val it = transaction.getXodus(entityStore)
        val run = EntityIterablePlusAlt(it.getAll(table.tableName)).applyCondition(it, read.condition)
        return run.toInstances(it, read)
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
