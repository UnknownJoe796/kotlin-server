package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.TableAccess

/**
 *
 * Created by josep on 7/13/2017.
 */

fun TableAccess.security() = SecurityTableAccess(this)