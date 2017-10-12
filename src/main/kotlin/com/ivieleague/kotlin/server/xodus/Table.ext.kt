package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.type.Table
import jetbrains.exodus.entitystore.PersistentEntityStore

/**
 *
 * Created by josep on 7/13/2017.
 */
fun Table.xodus(persistentEntityStore: PersistentEntityStore): XodusTableAccess
        = XodusTableAccess(persistentEntityStore, this)