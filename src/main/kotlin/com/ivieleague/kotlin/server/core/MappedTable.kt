package com.ivieleague.kotlin.server.core


class MappedTable(val wraps: TableAccess) : TableAccess, Table {

    //Table

    override val tableName: String
        get() = wraps.table.tableName
    override val tableDescription: String
        get() = wraps.table.tableDescription
    override val scalars = ArrayList<Scalar>()
    override val links = ArrayList<Link>()
    override val multilinks = ArrayList<Multilink>()
    override var readPermission: (user: Instance?) -> Condition = { Condition.Always }
    override var writePermission: (user: Instance?) -> Condition = { Condition.Always }

    val scalarMappers = HashMap<Scalar, Mapping>()


    //Access

    override val table: Table
        get() = this

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(user: Instance?, write: Write): Instance {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(user: Instance?, id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    //Do actions

    fun scalarPassThrough(scalar: Scalar) {
        val mapper = ThroughMapping(scalar)
        scalars += scalar
    }


    interface Mapping<T> {
        fun mapWrite(input: Write, modify: Write)
        fun mapRead(input: Read, modify: Read)
        fun doRead(instance: Instance): T
        fun mapQuery(input: Condition): Condition
    }

    class ThroughMapping(val scalar: Scalar) : Mapping {
        override fun mapWrite(input: Write, modify: Write) {
            input.scalars[scalar]?.let { modify.scalars[scalar] = it }
        }

        override fun mapRead(input: Read, modify: Read) {
            modify.scalars.add(scalar)
        }

        override fun doRead(instance: Instance): Any? = instance.scalars[scalar]
        override fun mapQuery(input: Condition): Condition = input
    }
}