package com.ivieleague.kotlin.server.model

/**
 * By implementing this interface, you state that you do not modify
 *
 * Created by joseph on 7/26/17.
 */
interface TableAccessWrapper : TableAccess {
    val wraps: TableAccess

    val modifiesRead: Boolean
    val modifiesPost: Boolean
    val modifiesQuery: Boolean
}