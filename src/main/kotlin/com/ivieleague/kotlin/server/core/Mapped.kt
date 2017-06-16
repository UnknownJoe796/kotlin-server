//package com.ivieleague.kotlin.server.property
//
//import com.ivieleague.kotlin.server.DAO
//
//class MappedTable(val wraps: TableAccess) : Table, TableAccess {
//    override val table: Table
//        get() = this
//    override var tableName: String = wraps.table.tableName
//    override var tableDescription: String = wraps.table.tableDescription
//
//    override var readPermission: (dao: DAO, user: Instance?) -> Condition = { _, _ -> Condition.Always }
//    override var writePermission: (dao: DAO, user: Instance?) -> Condition = { _, _ -> Condition.Always }
//
//    private val internalScalars: ArrayList<Scalar> = ArrayList()
//    private val internalLinks: ArrayList<Link> = ArrayList()
//    private val internalMultilinks: ArrayList<Multilink> = ArrayList()
//
//    private val scalarMappers: HashMap<Scalar, ScalarMapper> = HashMap()
//    private val linkMappers: HashMap<Link, LinkMapper> = HashMap()
//    private val multilinkMappers: HashMap<Multilink, MultilinkMapper> = HashMap()
//
//    override val scalars get() = internalScalars
//    override val links get() = internalLinks
//    override val multilinks get() = internalMultilinks
//
//    private fun innerRead(requestedRead: Read): Read {
//        val reads = requestedRead.scalars.asSequence().mapNotNull<Scalar, Mapper> { scalarMappers[it] } +
//                requestedRead.links.keys.asSequence().mapNotNull<Link, Mapper> { linkMappers[it] } +
//                requestedRead.multilinks.keys.asSequence().mapNotNull <Multilink, Mapper> { multilinkMappers[it] }
//        return Read().apply {
//            reads.forEach { merge(it.dependencies) }
//        }
//    }
//
//    private fun innerWrite(user: Instance?, requestedWrite: Write): Write {
//        val modifyWrite: Write = Write()
//        for ((scalar, value) in requestedWrite.scalars) {
//            scalarMappers[scalar]!!.write(user, value, modifyWrite)
//        }
//        for ((link, subwrite) in requestedWrite.links) {
//            linkMappers[link]!!.write(user, subwrite, modifyWrite)
//        }
//        for ((multilink, subwrites) in requestedWrite.multilinks) {
//            multilinkMappers[multilink]!!.write(user, subwrites, modifyWrite)
//        }
//        return modifyWrite
//    }
//
//    private fun innerInstanceToOuter(user: Instance?, row: Instance, myRead: Read): Instance {
//        return Instance(
//                row.id,
//                myRead.scalars.associate { it to scalarMappers[it]!!.read(user, row) },
//                myRead.links.entries.associate { it.key to linkMappers[it.key]!!.read(user, row) },
//                myRead.multilinks.entries.associate { it.key to multilinkMappers[it.key]!!.read(user, row) }
//        )
//    }
//
//    override fun get(user: Instance?, id: String, read: Read): Instance? {
//        return wraps.get(user, id, innerRead(read))?.let { innerInstanceToOuter(user, it, read) }
//    }
//
//    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
//        //OH SNAP - how do I translate conditions?!
//        return wraps.query(user, )?.let { innerInstanceToOuter(user, it, read) }
//    }
//
//    override fun post(user: Instance?, write: Write): Boolean {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun put(user: Instance?, id: String, write: Write): Boolean {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun delete(user: Instance?, id: String): Boolean {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    interface Mapper {
//        val dependencies: Read
//    }
//
//    class ScalarMapper(
//            val scalar: Scalar,
//            override val dependencies: Read,
//            val read: (user: Instance?, row: Instance?) -> Any?,
//            val write: (user: Instance?, value: Any?, modifyWrite: Write) -> Unit
//    ) : Mapper
//
//    class LinkMapper(
//            val link: Link,
//            override val dependencies: Read,
//            val read: (user: Instance?, row: Instance?) -> Instance?,
//            val write: (user: Instance?, value: Write, modifyWrite: Write) -> Unit
//    ) : Mapper
//
//    class MultilinkMapper(
//            val multilink: Multilink,
//            override val dependencies: Read,
//            val read: (user: Instance?, row: Instance?) -> List<Instance>,
//            val write: (user: Instance?, value: MultilinkModifications, modifyWrite: Write) -> Unit
//    ) : Mapper
//}