package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.model.*
import jetbrains.exodus.entitystore.*
import org.jetbrains.annotations.NotNull

class XodusTableAccess(
        val entityStore: PersistentEntityStore,
        override val table: Table
) : TableAccess {

    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
        val txn = transaction.getXodus(entityStore)
        val neededSubreads = HashMap<Link, ArrayList<Pair<Instance, String>>>()
        val neededMultiSubreads = HashMap<Multilink, ArrayList<Pair<Instance, List<String>>>>()
        val instances = ids.associate { id ->
            id to txn.getEntityOrNull(id)?.let { entity ->
                val instance = Instance(
                        table,
                        id = id,
                        scalars = read.scalars.associate { scalar -> scalar to entity.getProperty(scalar.key) }.toMutableMap()
                )
                read.links.forEach { (link, _) ->
                    val linkedId = entity.getProperty(link.key) as? String
                    if (linkedId != null) {
                        neededSubreads.getOrPut(link) { ArrayList() } += instance to linkedId
                    } else {
                        instance.links[link] = null
                    }
                }
                read.multilinks.forEach { (multilink, read) ->
                    val linkedId = entity.getProperty(multilink.key) as? String
                    if (linkedId != null) {
                        neededMultiSubreads.getOrPut(multilink) { ArrayList() } += instance to linkedId.split(',')
                    }
                }
                instance
            }
        }

        for ((link, list) in neededSubreads) {
            val getsResult = transaction.tableAccesses[link.table].gets(transaction, list.map { it.second }, read.links[link]!!)
            for ((instance, sid) in list) {
                instance.links[link] = getsResult[sid]
            }
        }
        for ((multilink, list) in neededMultiSubreads) {
            val getsResult = transaction.tableAccesses[multilink.table].gets(transaction, list.flatMap { it.second }, read.multilinks[multilink]!!)
            for ((instance, sids) in list) {
                instance.multilinks[multilink] = sids.mapNotNull { getsResult[it] }
            }
        }

        return instances
    }

    fun Condition.toEntityIterable(txn: StoreTransaction): EntityIterable = when (this) {
        Condition.Always -> txn.getAll(table.tableName)
        Condition.Never -> txn.find(table.tableName, "__does_not_exist__", 0)
        is Condition.AllConditions -> TODO()
        is Condition.AnyConditions -> TODO()
        is Condition.ScalarEqual -> txn.find(table.tableName, this.scalar.key, this.value as Comparable<*>)
        is Condition.ScalarNotEqual -> txn.getAll(table.tableName).
        is Condition.ScalarBetween<*> -> txn.find(table.tableName, this.scalar.key, this.lower, this.upper)
        is Condition.IdEquals -> TODO()
        is Condition.MultilinkContains -> TODO()
        is Condition.MultilinkDoesNotContain -> TODO()
    }

    fun EntityIterable.applyCondition(condition: Condition): EntityIterable {
        return when (condition) {
            Condition.Always -> this
            Condition.Never -> this.
            is Condition.AllConditions -> TODO()
            is Condition.AnyConditions -> TODO()
            is Condition.ScalarEqual -> TODO()
            is Condition.ScalarNotEqual -> TODO()
            is Condition.ScalarBetween -> TODO()
            is Condition.IdEquals -> TODO()
            is Condition.MultilinkContains -> TODO()
            is Condition.MultilinkDoesNotContain -> TODO()
        }
    }

    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
        val txn = transaction.getXodus(entityStore)

        txn.getAll(table.tableName)
    }

    override fun update(transaction: Transaction, write: Write): WriteResult {
        if (write.id == null && write.delete) return WriteResult(table, write, null)

        val txn = transaction.getXodus(entityStore)

        val id = write.id
        val entity = if (id == null) txn.newEntity(table.tableName) else try {
            txn.getEntity(txn.toEntityId(id))
        } catch (e: EntityRemovedInDatabaseException) {
            throw IllegalArgumentException("ID not found")
        }

        val writeResult = WriteResult(table, write, entity.toIdString())
        doSubs(transaction, write, entity, writeResult)
        if (write.delete) {
            updateEntity(write, entity, writeResult)
        } else {
            entity.delete()
        }
        return writeResult
    }

    private fun updateEntity(write: Write, entity: @NotNull Entity, writeResult: WriteResult) {

        //Scalars
        for ((scalar, value) in write.scalars) {
            entity.setProperty(scalar.key, value as Comparable<*>)
        }

        //Links
        for ((link, result) in writeResult.links) {
            if (result == null) {
                entity.deleteProperty(link.key)
            } else {
                entity.setProperty(link.key, result.id!!)
            }
        }

        //Multilinks
        for ((multilink, result) in writeResult.multilinks) {
            val replacements = result.replacements
            if (replacements != null) {
                entity.setProperty(multilink.key, replacements.joinToString(",") { it.id!! })
            }

            val removals = result.removals
            if (removals != null) {
                val result = (entity.getProperty(multilink.key) as? String)
                        ?.split(',')
                        ?.minus(removals.asSequence().map { it.id })
                        ?.joinToString(",")
                if (result != null) entity.setProperty(multilink.key, result)
            }

            val additions = result.additions
            if (additions != null) {
                (entity.getProperty(multilink.key) as? String)?.let {
                    if (additions.isNotEmpty()) {
                        val string = if (it.isBlank()) additions.joinToString(",")
                        else additions.joinToString(",", ",")
                        entity.setProperty(multilink.key, string)
                    }
                }
            }
        }
    }

    private fun doSubs(transaction: Transaction, write: Write, entity: Entity, writeResult: WriteResult) {
        //Links
        for ((link, subwrite) in write.links) {
            val result = subwrite?.let { transaction.tableAccesses[link.table].update(transaction, it) }
            writeResult.links[link] = result
        }

        //Multilinks
        for ((multilink, modifications) in write.multilinks) {
            val modificationResults = WriteResult.MultilinkModificationsResults()

            //replacements
            val replacements = modifications.replacements
            if (replacements != null) {
                val repRes = replacements.map { transaction.tableAccesses[multilink.table].update(transaction, it) }
                modificationResults.replacements = repRes
            }

            //removals
            val removals = modifications.removals
            if (removals != null) {
                val remRes = removals.map { transaction.tableAccesses[multilink.table].update(transaction, it) }
                modificationResults.removals = remRes
            }

            //additions
            val additions = modifications.additions
            if (additions != null) {
                val addRes = additions.map { transaction.tableAccesses[multilink.table].update(transaction, it) }
                modificationResults.additions = addRes
            }

            writeResult.multilinks[multilink] = modificationResults
        }
    }
}