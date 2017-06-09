package com.ivieleague.kotlin.server

abstract class Table(val tableName: String, val tableDescription: String) {
    val properties = HashMap<String, Property>()

    fun property(
            name: String,
            description: String = "",
            type: ServerType,
            default: Any?,
            read: AccessRules = listOf(listOf()),
            write: AccessRules = listOf(listOf()),
            versionStart: Int = 0,
            versionEnd: Int = Int.MAX_VALUE,
            calculated: ((String, DAO) -> Any?)? = null
    ): Property {
        val prop = Property(
                name = name,
                description = description,
                type = type,
                default = default,
                read = read,
                write = write,
                versionStart = versionStart,
                versionEnd = versionEnd,
                calculated = calculated
        )
        properties[name] = prop
        return prop
    }
}