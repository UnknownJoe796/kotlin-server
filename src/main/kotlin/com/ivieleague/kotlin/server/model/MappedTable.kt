package com.ivieleague.kotlin.server.model


class MappedTable(val wraps: TableAccess) : TableAccess, Table {

    //Table

    override val tableName: String
        get() = wraps.table.tableName
    override val tableDescription: String
        get() = wraps.table.tableDescription


    val scalarMappers = HashMap<Scalar, ScalarMapping>()
    val linkMappers = HashMap<Link, LinkMapping>()
    val multilinkMappers = HashMap<Multilink, MultilinkMapping>()

    override val scalars by lazy { scalarMappers.values.map { it.wraps } }
    override val links by lazy { linkMappers.values.map { it.wraps } }
    override val multilinks by lazy { multilinkMappers.values.map { it.wraps } }

    override var readPermission: (user: Instance?) -> Condition = wraps.table.readPermission
    override var writeBeforePermission: (user: Instance?) -> Condition = wraps.table.writeBeforePermission
    override var writeAfterPermission: (user: Instance?) -> Condition = wraps.table.writeAfterPermission

    private fun innerInstanceToOuter(user: Instance?, row: Instance, myRead: Read): Instance {
        return Instance(
                row.id,
                myRead.scalars.associate { it to scalarMappers[it]!!.doRead(user, row) }.toMutableMap(),
                myRead.links.entries.associate { it.key to linkMappers[it.key]!!.doRead(user, row) }.toMutableMap(),
                myRead.multilinks.entries.associate { it.key to multilinkMappers[it.key]!!.doRead(user, row) }.toMutableMap()
        )
    }

    private fun innerRead(user: Instance?, outer: Read): Read {
        val inner = Read()
        for (property in outer.scalars) {
            scalarMappers[property]?.mapRead(user, inner)
        }
        for ((property, sub) in outer.links) {
            linkMappers[property]?.mapRead(user, sub, inner)
        }
        for ((property, sub) in outer.multilinks) {
            multilinkMappers[property]?.mapRead(user, sub, inner)
        }
        return inner
    }

    private fun innerWrite(user: Instance?, outer: Write): Write {
        val inner = Write()
        for ((property, value) in outer.scalars) {
            scalarMappers[property]?.mapWrite(user, value, inner)
        }
        for ((property, sub) in outer.links) {
            linkMappers[property]?.mapWrite(user, sub, inner)
        }
        for ((property, sub) in outer.multilinks) {
            multilinkMappers[property]?.mapWrite(user, sub, inner)
        }
        return inner
    }

    private fun innerQuery(user: Instance?, condition: Condition): Condition {
        var current = condition
        for ((_, mapper) in scalarMappers) {
            current = mapper.mapQuery(user, current)
        }
        for ((_, mapper) in linkMappers) {
            current = mapper.mapQuery(user, current)
        }
        for ((_, mapper) in multilinkMappers) {
            current = mapper.mapQuery(user, current)
        }
        return current
    }

    //Access

    override val table: Table
        get() = this

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        return wraps.get(user, id, innerRead(user, read))?.let { innerInstanceToOuter(user, it, read) }
    }

    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
        return wraps.query(user, innerQuery(user, condition), innerRead(user, read)).map { innerInstanceToOuter(user, it, read) }
    }

    override fun update(user: Instance?, write: Write): Instance {
        return wraps.update(user, innerWrite(user, write))
    }

    override fun delete(user: Instance?, id: String): Boolean {
        return wraps.delete(user, id)
    }


    //Do actions

    interface ScalarMapping {
        val wraps: Scalar
        fun mapWrite(user: Instance?, value: Any?, modify: Write)
        fun mapRead(user: Instance?, modify: Read)
        fun doRead(user: Instance?, instance: Instance): Any?
        fun mapQuery(user: Instance?, input: Condition): Condition
    }

    interface LinkMapping {
        val wraps: Link
        fun mapWrite(user: Instance?, value: Write?, modify: Write)
        fun mapRead(user: Instance?, input: Read, modify: Read)
        fun doRead(user: Instance?, instance: Instance): Instance?
        fun mapQuery(user: Instance?, input: Condition): Condition
    }

    interface MultilinkMapping {
        val wraps: Multilink
        fun mapWrite(user: Instance?, value: MultilinkModifications, modify: Write)
        fun mapRead(user: Instance?, input: Read, modify: Read)
        fun doRead(user: Instance?, instance: Instance): Collection<Instance>
        fun mapQuery(user: Instance?, input: Condition): Condition
    }

    class ScalarThroughMapping(override val wraps: Scalar) : ScalarMapping {
        override fun mapWrite(user: Instance?, value: Any?, modify: Write) {
            modify.scalars[wraps] = value
        }

        override fun mapRead(user: Instance?, modify: Read) {
            modify.scalars.add(wraps)
        }

        override fun doRead(user: Instance?, instance: Instance): Any? = instance.scalars[wraps]
        override fun mapQuery(user: Instance?, input: Condition): Condition = input
    }

    class LinkThroughMapping(override val wraps: Link) : LinkMapping {
        override fun mapWrite(user: Instance?, value: Write?, modify: Write) {
            modify.links[wraps] = value
        }

        override fun mapRead(user: Instance?, input: Read, modify: Read) {
            modify.links[wraps] = input.links[wraps]!!
        }

        override fun doRead(user: Instance?, instance: Instance): Instance? = instance.links[wraps]
        override fun mapQuery(user: Instance?, input: Condition): Condition = input
    }

    class MultilinkThroughMapping(override val wraps: Multilink) : MultilinkMapping {
        override fun mapWrite(user: Instance?, value: MultilinkModifications, modify: Write) {
            modify.multilinks[wraps] = value
        }

        override fun mapRead(user: Instance?, input: Read, modify: Read) {
            modify.multilinks[wraps] = input.multilinks[wraps]!!
        }

        override fun doRead(user: Instance?, instance: Instance): Collection<Instance> = instance.multilinks[wraps]!!
        override fun mapQuery(user: Instance?, input: Condition): Condition = input
    }
}