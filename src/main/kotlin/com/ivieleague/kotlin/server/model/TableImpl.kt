package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.*

/**
 * Created by josep on 6/15/2017.
 */
open class TableImpl(
        override val tableName: String,
        override val tableDescription: String
) : Table {
    override val primitives: ArrayList<Primitive> = ArrayList()
    override val links: ArrayList<Link> = ArrayList()
    override val multilinks: ArrayList<Multilink> = ArrayList()
    override var readPermission: (user: Instance?) -> Condition = SecurityRules.always
    override var writeBeforePermission: SecurityRule = SecurityRules.always
    override var writeAfterPermission: SecurityRule = SecurityRules.always

    fun Primitive.register(): Primitive {
        primitives += this
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