package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.model.*
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.*
import org.jetbrains.annotations.NotNull

class XodusTableAccess(
        val entityStore: PersistentEntityStore,
        override val table: Table
) : TableAccess {

    fun Iterable<Entity>.toIntancesBatch(transaction: Transaction, txn: StoreTransaction, read: Read): List<Instance> {
        val neededSubreads = HashMap<Link, ArrayList<Pair<Instance, String>>>()
        val neededMultiSubreads = HashMap<Multilink, ArrayList<Pair<Instance, List<String>>>>()
        val instances = this.map { entity ->
            val instance = Instance(
                    table,
                    id = entity.id.toString(),
                    scalars = read.primitives.associate { scalar -> scalar to entity.getProperty(scalar.key) }.toMutableMap()
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

    fun Entity.toInstance(transaction: Transaction, txn: StoreTransaction, read: Read): Instance {
        val instance = Instance(
                table,
                id = this.id.toString(),
                scalars = read.primitives.associate { scalar -> scalar to this.getProperty(scalar.key) }.toMutableMap()
        )
        read.links.forEach { (link, subread) ->
            val linkedId = this.getProperty(link.key) as? String
            if (linkedId != null) {
                instance.links[link] = transaction.tableAccesses[link.table].get(transaction, linkedId, subread)
            } else {
                instance.links[link] = null
            }
        }
        read.multilinks.forEach { (multilink, subread) ->
            val linkedIds = (this.getProperty(multilink.key) as? String)?.split(',')
            if (linkedIds != null) {
                instance.multilinks[multilink] = transaction.tableAccesses[multilink.table].gets(transaction, linkedIds, subread).values.filterNotNull()
            }
        }
        return instance
    }

    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
        val txn = transaction.getXodus(entityStore)
        val neededSubreads = HashMap<Link, ArrayList<Pair<Instance, String>>>()
        val neededMultiSubreads = HashMap<Multilink, ArrayList<Pair<Instance, List<String>>>>()
        val instances = ids.associate { id ->
            id to txn.getEntityOrNull(id)?.let { entity ->
                val instance = Instance(
                        table,
                        id = id,
                        scalars = read.primitives.associate { scalar -> scalar to entity.getProperty(scalar.key) }.toMutableMap()
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

    fun Condition.slowDefault(transaction: Transaction, txn: StoreTransaction, read: Read): kotlin.sequences.Sequence<Instance> {
        return txn.getAll(table.tableName)
                .asSequence()
                .map { it.toInstance(transaction, txn, read) }
                .filter { instance -> invoke(instance) }
    }

    //Possible return types: EntityIterable, kotlin.sequences.Sequence<Instance>
    fun Condition.tryXodusIterable(transaction: Transaction, txn: StoreTransaction, read: Read): Any? = when (this) {
        Condition.Always -> txn.getAll(table.tableName)
        Condition.Never -> txn.find(table.tableName, "__does_not_exist__", 0)
        is Condition.AllConditions -> {
            val xodusIterables = ArrayList<EntityIterable>()
            val others = ArrayList<Condition>()
            for (condition in this.conditions) {
                val attemptedXodus = condition.tryXodusIterable(transaction, txn, read)
                if (attemptedXodus is EntityIterable) xodusIterables += attemptedXodus
                else others += condition
            }
            val xodusBase = if (xodusIterables.isNotEmpty()) xodusIterables.reduce { a, b -> a.intersect(b) } else txn.getAll(table.tableName)
            if (others.isEmpty()) {
                xodusBase
            } else
                xodusBase
                        .asSequence()
                        .map { it.toInstance(transaction, txn, read) }
                        .filter { instance -> others.any { it.invoke(instance) } }
        }
        is Condition.AnyConditions -> {
            val xodusIterables = ArrayList<EntityIterable>()
            val others = ArrayList<Condition>()
            for (condition in this.conditions) {
                val attemptedXodus = condition.tryXodusIterable(transaction, txn, read)
                if (attemptedXodus is EntityIterable) xodusIterables += attemptedXodus
                else others += condition
            }
            val xodusBase = if (xodusIterables.isNotEmpty()) xodusIterables.reduce { a, b -> a.union(b) } else txn.getAll(table.tableName)
            if (others.isEmpty()) {
                xodusBase
            } else
                xodusBase
                        .asSequence()
                        .map { it.toInstance(transaction, txn, read) }
                        .filter { instance -> others.any { it.invoke(instance) } }
        }
        is Condition.ScalarEqual -> {
            if (this.path.isEmpty())
                txn.find(table.tableName, this.primitive.key, this.value as Comparable<*>)
            else
                slowDefault(transaction, txn, read)
        }
        is Condition.ScalarNotEqual -> {
            if (this.path.isEmpty())
                txn.getAll(table.tableName).minus(txn.find(table.tableName, this.primitive.key, this.value as Comparable<*>))
            else
                slowDefault(transaction, txn, read)
        }
        is Condition.ScalarBetween<*> -> {
            if (this.path.isEmpty())
                txn.find(table.tableName, this.primitive.key, this.lower, this.upper)
            else
                slowDefault(transaction, txn, read)
        }
//        is Condition.ScalarLessThanOrEqual<*> -> {
//            if (this.path.isEmpty())
//                txn.find(table.tableName, this.primitive.key, this.lower, this.upper)
//            else
//                slowDefault(transaction, txn, read)
//        }
//        is Condition.ScalarGreaterThanOrEqual<*> -> {
//            if (this.path.isEmpty())
//                txn.find(table.tableName, this.primitive.key, this.lower, this.upper)
//            else
//                slowDefault(transaction, txn, read)
//        }
//        is Condition.ScalarLessThan<*> -> {
//            if (this.path.isEmpty())
//                txn.find(table.tableName, this.primitive.key, this.lower, this.upper)
//            else
//                slowDefault(transaction, txn, read)
//        }
//        is Condition.ScalarGreaterThan<*> -> {
//            if (this.path.isEmpty())
//                txn.find(table.tableName, this.primitive.key, this.lower, this.upper)
//            else
//                slowDefault(transaction, txn, read)
//        }
        is Condition.MultilinkContains -> slowDefault(transaction, txn, read)
        is Condition.MultilinkDoesNotContain -> slowDefault(transaction, txn, read)
        is Condition.IdEquals -> {
            if (this.path.isEmpty())
                txn.findIds(table.tableName, txn.toEntityId(this.id).localId, txn.toEntityId(this.id).localId)
            else
                slowDefault(transaction, txn, read)
        }
        else -> slowDefault(transaction, txn, read)
    }

    fun Condition.query(transaction: Transaction, txn: StoreTransaction, read: Read): kotlin.sequences.Sequence<Instance> {
        val result = tryXodusIterable(transaction, txn, read)
        return when (result) {
            is kotlin.sequences.Sequence<*> -> result as kotlin.sequences.Sequence<Instance>
            is EntityIterable -> result.asSequence().map { it.toInstance(transaction, txn, read) }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
        val sortCondition = read.sortCondition()
        val fullCondition = if (sortCondition != null) Condition.AllConditions(listOf(read.condition, sortCondition)) else read.condition
        val result = fullCondition.simplify().query(transaction, transaction.getXodus(entityStore), read)
        if (read.sort.isEmpty()) {
            return result.take(read.count).toList()
        } else {
            return result.sortedWith(read.sort.instanceComparator()).take(read.count).toList()
        }
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
            entity.delete()
        } else {
            updateEntity(write, entity, writeResult)
            txn.saveEntity(entity)
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
                val resultingMultilink = (entity.getProperty(multilink.key) as? String)
                        ?.split(',')
                        ?.minus(removals.asSequence().map { it.id })
                        ?.joinToString(",")
                if (resultingMultilink != null) entity.setProperty(multilink.key, resultingMultilink)
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