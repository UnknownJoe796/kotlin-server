package com.ivieleague.kotlin.server

sealed class Property {
    abstract val name: String
    abstract val description: String
    abstract val dependencies: Collection<Property>
    abstract val read: RowPermission
    abstract val write: RowPermission
    abstract val versionStart: Int
    abstract val versionEnd: Int
}

typealias ValueDefault = (user: Instance) -> Any?

sealed class Scalar : Property() {
    abstract val type: ScalarType

    data class Standard(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val type: ScalarType,
            val default: ValueDefault
    ) : Scalar() {
        override val dependencies: Collection<Property>
            get() = listOf()
    }

    data class Calculated(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val type: ScalarType,
            override val dependencies: Collection<Property>,
            val readCalc: (dao: DAO, user: Instance?, row: Instance) -> Any?,
            val writeCalc: (dao: DAO, user: Instance?, row: Instance, value: Any?) -> Unit
    ) : Scalar()
}

sealed class Link : Property() {
    abstract val table: Table

    data class Standard(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val table: Table
    ) : Link() {
        override val dependencies: Collection<Property>
            get() = listOf()
    }

    data class Calculated(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val table: Table,
            override val dependencies: Collection<Property>,
            val readCalc: (dao: DAO, user: Instance?, row: Instance) -> Instance?,
            val writeCalc: (dao: DAO, user: Instance?, row: Instance, value: Instance?) -> Unit
    ) : Link()
}

sealed class MultiLink : Property() {
    abstract val table: Table

    data class Standard(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val table: Table
    ) : MultiLink() {
        override val dependencies: Collection<Property>
            get() = listOf()
    }

    data class Calculated(
            override val name: String,
            override val description: String,
            override val read: RowPermission = RowPermissions.Always,
            override val write: RowPermission = RowPermissions.Always,
            override val versionStart: Int = 0,
            override val versionEnd: Int = Int.MAX_VALUE,
            override val table: Table,
            override val dependencies: Collection<Property>,
            val readCalc: (dao: DAO, user: Instance?, row: Instance) -> List<Instance>,
            val writeCalc: (dao: DAO, user: Instance?, row: Instance, value: List<Instance>) -> Unit
    ) : MultiLink()
}