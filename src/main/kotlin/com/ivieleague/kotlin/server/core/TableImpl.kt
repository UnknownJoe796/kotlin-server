package com.ivieleague.kotlin.server.core

/**
 * Created by josep on 6/15/2017.
 */
open class TableImpl(
        override val tableName: String,
        override val tableDescription: String
) : Table {
    override val scalars: ArrayList<Scalar> = ArrayList()
    override val links: ArrayList<Link> = ArrayList()
    override val multilinks: ArrayList<Multilink> = ArrayList()
    override var readPermission: (user: Instance?) -> Condition = { _ -> Condition.Always }
    override var writePermission: (user: Instance?) -> Condition = { _ -> Condition.Always }

    fun Scalar.register(): Scalar {
        scalars += this
        return this
    }

    fun Link.register(): Link {
        links += this
        return this
    }

    fun Multilink.register(): Multilink {
        multilinks += this
        return this
    }
}