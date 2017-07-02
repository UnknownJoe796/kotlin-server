package com.ivieleague.kotlin.server.model

interface Table {
    val tableName: String
    val tableDescription: String

    val scalars: Collection<Scalar>
    val links: Collection<Link>
    val multilinks: Collection<Multilink>

    val readPermission: SecurityRule
    val writeBeforePermission: SecurityRule
    val writeAfterPermission: SecurityRule

    fun defaultRead() = Read(scalars, links.associate { it to Read.EMPTY }, multilinks.associate { it to Read.EMPTY })
}

/*

Use Cases

Column security access - Query users - you can only see details on yourself and your friends
Row security access - write user - You can only edit yourself
Private field - Passwords - The hash exists in the DB, but is unreadable and unwritable by public
Validation - Valid range - You can only write positive values in this field

*/