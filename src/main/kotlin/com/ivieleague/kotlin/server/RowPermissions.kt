package com.ivieleague.kotlin.server


typealias RowPermission = (dao: DAO, user: Instance?) -> Condition
object RowPermissions {
    val Always: RowPermission = { _, _ -> Condition.Always }
    val Never: RowPermission = { _, _ -> Condition.Never }
}