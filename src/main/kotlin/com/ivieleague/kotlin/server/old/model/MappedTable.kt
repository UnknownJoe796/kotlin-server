package com.ivieleague.kotlin.server.old.model

import com.ivieleague.kotlin.server.old.type.*


class MappedTable(val wraps: TableAccess) : TableAccess, Table {

    //Table

    override val tableName: String
        get() = wraps.table.tableName
    override val tableDescription: String
        get() = wraps.table.tableDescription


    val scalarMappers = HashMap<Primitive, ScalarMapping>()
    val linkMappers = HashMap<Link, LinkMapping>()
    val multilinkMappers = HashMap<Multilink, MultilinkMapping>()

    override val primitives by lazy { scalarMappers.values.map { it.wraps } }
    override val links by lazy { linkMappers.values.map { it.wraps } }
    override val multilinks by lazy { multilinkMappers.values.map { it.wraps } }

    override var readPermission: (user: Instance?) -> Condition = wraps.table.readPermission
    override var writeBeforePermission: (user: Instance?) -> Condition = wraps.table.writeBeforePermission
    override var writeAfterPermission: (user: Instance?) -> Condition = wraps.table.writeAfterPermission

    private fun innerInstanceToOuter(transaction: Transaction, row: Instance, myRead: Read): Instance {
        return Instance(
                table,
                row.id,
                myRead.primitives.associate { it to scalarMappers[it]!!.doRead(transaction, row) }.toMutableMap(),
                myRead.links.entries.associate { it.key to linkMappers[it.key]!!.doRead(transaction, row) }.toMutableMap(),
                myRead.multilinks.entries.associate { it.key to multilinkMappers[it.key]!!.doRead(transaction, row) }.toMutableMap()
        )
    }

    private fun innerRead(transaction: Transaction, outer: Read): Read {
        val inner = Read()
        inner.condition = innerQuery(transaction, outer.condition)
        for (property in outer.primitives) {
            scalarMappers[property]?.mapRead(transaction, inner)
        }
        for ((property, sub) in outer.links) {
            linkMappers[property]?.mapRead(transaction, sub, inner)
        }
        for ((property, sub) in outer.multilinks) {
            multilinkMappers[property]?.mapRead(transaction, sub, inner)
        }
        return inner
    }

    private fun innerWrite(transaction: Transaction, outer: Write): Write {
        val inner = Write()
        for ((property, value) in outer.scalars) {
            scalarMappers[property]?.mapWrite(transaction, value, inner)
        }
        for ((property, sub) in outer.links) {
            linkMappers[property]?.mapWrite(transaction, sub, inner)
        }
        for ((property, sub) in outer.multilinks) {
            multilinkMappers[property]?.mapWrite(transaction, sub, inner)
        }
        return inner
    }

    private fun innerQuery(transaction: Transaction, condition: Condition): Condition {
        var current = condition
        for ((_, mapper) in scalarMappers) {
            current = mapper.mapQuery(transaction, current)
        }
        for ((_, mapper) in linkMappers) {
            current = mapper.mapQuery(transaction, current)
        }
        for ((_, mapper) in multilinkMappers) {
            current = mapper.mapQuery(transaction, current)
        }
        return current
    }

    //Access

    override val table: Table
        get() = this

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        return wraps.get(transaction, id, innerRead(transaction, read))?.let { innerInstanceToOuter(transaction, it, read) }
    }

    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
        return wraps.gets(transaction, ids, innerRead(transaction, read)).mapValues { it.value?.let { innerInstanceToOuter(transaction, it, read) } }
    }

    override fun query(transaction: Transaction, read: Read): List<Instance> {
        return wraps.query(transaction, innerRead(transaction, read)).map { innerInstanceToOuter(transaction, it, read) }
    }

    override fun update(transaction: Transaction, write: Write): WriteResult {
        return wraps.update(transaction, innerWrite(transaction, write))
    }


    //Do actions

    interface ScalarMapping {
        val wraps: Primitive
        fun mapWrite(transaction: Transaction, value: Any?, modify: Write)
        fun mapRead(transaction: Transaction, modify: Read)
        fun doRead(transaction: Transaction, instance: Instance): Any?
        fun mapQuery(transaction: Transaction, input: Condition): Condition
    }

    interface LinkMapping {
        val wraps: Link
        fun mapWrite(transaction: Transaction, value: Write?, modify: Write)
        fun mapRead(transaction: Transaction, input: Read, modify: Read)
        fun doRead(transaction: Transaction, instance: Instance): Instance?
        fun mapQuery(transaction: Transaction, input: Condition): Condition
    }

    interface MultilinkMapping {
        val wraps: Multilink
        fun mapWrite(transaction: Transaction, value: MultilinkModifications, modify: Write)
        fun mapRead(transaction: Transaction, input: Read, modify: Read)
        fun doRead(transaction: Transaction, instance: Instance): Collection<Instance>
        fun mapQuery(transaction: Transaction, input: Condition): Condition
    }

    class ScalarThroughMapping(override val wraps: Primitive) : ScalarMapping {
        override fun mapWrite(transaction: Transaction, value: Any?, modify: Write) {
            modify.scalars[wraps] = value
        }

        override fun mapRead(transaction: Transaction, modify: Read) {
            modify.primitives.add(wraps)
        }

        override fun doRead(transaction: Transaction, instance: Instance): Any? = instance.scalars[wraps]
        override fun mapQuery(transaction: Transaction, input: Condition): Condition = input
    }

    class LinkThroughMapping(override val wraps: Link) : LinkMapping {
        override fun mapWrite(transaction: Transaction, value: Write?, modify: Write) {
            modify.links[wraps] = value
        }

        override fun mapRead(transaction: Transaction, input: Read, modify: Read) {
            modify.links[wraps] = input.links[wraps]!!
        }

        override fun doRead(transaction: Transaction, instance: Instance): Instance? = instance.links[wraps]
        override fun mapQuery(transaction: Transaction, input: Condition): Condition = input
    }

    class MultilinkThroughMapping(override val wraps: Multilink) : MultilinkMapping {
        override fun mapWrite(transaction: Transaction, value: MultilinkModifications, modify: Write) {
            modify.multilinks[wraps] = value
        }

        override fun mapRead(transaction: Transaction, input: Read, modify: Read) {
            modify.multilinks[wraps] = input.multilinks[wraps]!!
        }

        override fun doRead(transaction: Transaction, instance: Instance): Collection<Instance> = instance.multilinks[wraps]!!
        override fun mapQuery(transaction: Transaction, input: Condition): Condition = input
    }
}