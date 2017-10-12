package com.ivieleague.kotlin.server.type

interface Table {
    val tableName: String
    val tableDescription: String

    val primitives: Collection<Primitive>
    val links: Collection<Link>
    val multilinks: Collection<Multilink>

    val readPermission: SecurityRule
    val writeBeforePermission: SecurityRule
    val writeAfterPermission: SecurityRule
}

/*

Use Cases

Column security access - Query users - you can only see details on yourself and your friends
Row security access - write user - You can only edit yourself
Private field - Passwords - The hash exists in the DB, but is unreadable and unwritable by public
Validation - Valid range - You can only write positive values in this field

*/