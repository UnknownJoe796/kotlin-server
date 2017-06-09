package com.ivieleague.kotlin.server

/**
 * Created by josep on 6/8/2017.
 */
interface Schema {
    val tables: Map<String, Table>
    val enums: Map<String, ServerEnum>
}