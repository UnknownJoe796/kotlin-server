package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.model.*
import org.mindrot.jbcrypt.BCrypt


/*

A provided wrapper allows you to write to the 'password' field when editing
A provided endpoint creator allows you to obtain a token
A provided user reader allows you to read a user from a JWT

*/

abstract class AbstractUserTable(tableName: String, tableDescription: String) : TableImpl(tableName, tableDescription) {
    val hash = Scalar(
            key = "passwordhash",
            description = "The password hash.",
            type = ScalarType.ShortString,
            readPermission = SecurityRules.never,
            editPermission = SecurityRules.never,
            writePermission = SecurityRules.never
    ).register()

    fun checkPassword(instance: Instance, password: String): Boolean {
        return BCrypt.checkpw(password, instance.scalars[hash].toString())
    }
}

