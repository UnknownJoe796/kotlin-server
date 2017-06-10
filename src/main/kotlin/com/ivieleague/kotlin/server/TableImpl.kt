package com.ivieleague.kotlin.server

abstract class TableImpl(override val tableName: String, override val tableDescription: String) : Table {
    override val properties = HashMap<String, Property>()

    fun property(
            name: String,
            description: String = "",
            type: ServerType,
            default: Any?,
            read: AccessRules = listOf(listOf()),
            write: AccessRules = listOf(listOf()),
            versionStart: Int = 0,
            versionEnd: Int = Int.MAX_VALUE
    ): Property {
        val prop = Property(
                name = name,
                description = description,
                type = type,
                default = default,
                read = read,
                write = write,
                versionStart = versionStart,
                versionEnd = versionEnd
        )
        properties[name] = prop
        return prop
    }
}