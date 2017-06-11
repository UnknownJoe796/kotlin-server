package com.ivieleague.kotlin.server

sealed class Property {
    abstract val name: String
    abstract val description: String
    abstract val read: PropertyAccessRule
    abstract val write: PropertyAccessRule
    abstract val versionStart: Int
    abstract val versionEnd: Int
}

typealias ValueDefault = (user: Instance) -> Any?

data class Value(
        override val name: String,
        override val description: String,
        override val read: PropertyAccessRule = PropertyAccessRules.Always,
        override val write: PropertyAccessRule = PropertyAccessRules.Always,
        override val versionStart: Int = 0,
        override val versionEnd: Int = Int.MAX_VALUE,
        val type: ScalarType,
        val default: ValueDefault
) : Property()

data class CalculatedValue(
        override val name: String,
        override val description: String,
        override val read: PropertyAccessRule = PropertyAccessRules.Always,
        override val write: PropertyAccessRule = PropertyAccessRules.Always,
        override val versionStart: Int = 0,
        override val versionEnd: Int = Int.MAX_VALUE,
        val type: ScalarType,
        val calculate: (dao: DAO, instance: Instance) -> Any?,
        val writeCalculated: (dao: DAO, instance: Instance, value: Any?) -> Unit
) : Property()

data class Link(
        override val name: String,
        override val description: String,
        override val read: PropertyAccessRule = PropertyAccessRules.Always,
        override val write: PropertyAccessRule = PropertyAccessRules.Always,
        override val versionStart: Int = 0,
        override val versionEnd: Int = Int.MAX_VALUE,
        val table: Table
) : Property()

data class MultiLink(
        override val name: String,
        override val description: String,
        override val read: PropertyAccessRule = PropertyAccessRules.Always,
        override val write: PropertyAccessRule = PropertyAccessRules.Always,
        override val versionStart: Int = 0,
        override val versionEnd: Int = Int.MAX_VALUE,
        val table: Table
) : Property()