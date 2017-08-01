package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.Fetcher
import com.ivieleague.kotlin.server.model.Schema
import com.ivieleague.kotlin.server.model.Table
import com.ivieleague.kotlin.server.model.TableAccess
import jetbrains.exodus.entitystore.PersistentEntityStore

/**
 *
 * Created by josep on 7/13/2017.
 */
fun Table.xodus(fetcher: Fetcher<Table, TableAccess>, persistentEntityStore: PersistentEntityStore): XodusTableAccess
        = XodusTableAccess(persistentEntityStore, fetcher, this)

fun Schema.xodus(table: Table, persistentEntityStore: PersistentEntityStore): XodusTableAccess
        = XodusTableAccess(persistentEntityStore, this, table)