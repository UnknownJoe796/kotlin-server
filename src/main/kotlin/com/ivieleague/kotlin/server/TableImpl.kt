package com.ivieleague.kotlin.server

abstract class TableImpl(override val tableName: String, override val tableDescription: String) : Table {
    override val properties = HashMap<String, Property>()

    override var read: RowPermission = RowPermissions.Always
    override var write: RowPermission = RowPermissions.Always

    fun <T : Property> T.register(): T {
        properties[this.name] = this
        return this
    }
}