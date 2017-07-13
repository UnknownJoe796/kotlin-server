package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.model.TableAccess

/**
 *
 * Created by josep on 7/13/2017.
 */

fun TableAccess.user(tokenInformation: TokenInformation) = UserTableAccess(this, tokenInformation)