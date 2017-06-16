package com.ivieleague.kotlin.server.core

interface Table {
    val tableName: String
    val tableDescription: String

    val scalars: Collection<Scalar>
    val links: Collection<Link>
    val multilinks: Collection<Multilink>

    val readPermission: (user: Instance?) -> Condition
    val writePermission: (user: Instance?) -> Condition
}