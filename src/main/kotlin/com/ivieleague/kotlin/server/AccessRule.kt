package com.ivieleague.kotlin.server

typealias TableAccessRule = (dao: DAO, user: Instance?) -> Boolean
typealias PropertyAccessRule = (dao: DAO, user: Instance?, item: Instance) -> Boolean

object TableAccessRules {
    val Always: TableAccessRule = { _: DAO, _: Instance? -> true }
    val Never: TableAccessRule = { _: DAO, _: Instance? -> false }
}

object PropertyAccessRules {
    val Always: PropertyAccessRule = { _: DAO, _: Instance?, _: Instance -> true }
    val Never: PropertyAccessRule = { _: DAO, _: Instance?, _: Instance -> false }
}