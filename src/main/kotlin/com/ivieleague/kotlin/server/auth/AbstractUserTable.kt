package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.model.PrimitiveType
import com.ivieleague.kotlin.server.model.TableImpl
import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Primitive
import com.ivieleague.kotlin.server.type.SecurityRules
import org.mindrot.jbcrypt.BCrypt


/*

A provided wrapper allows you to write to the 'password' field when editing
A provided endpoint creator allows you to obtain a token
A provided user reader allows you to read a user from a JWT

*/

abstract class AbstractUserTable(tableName: String, tableDescription: String) : TableImpl(tableName, tableDescription) {
    val hash = Primitive(
            key = "passwordhash",
            description = "The password hash.",
            type = PrimitiveType.ShortString,
            readPermission = SecurityRules.never,
            editPermission = SecurityRules.never,
            writePermission = SecurityRules.never
    ).register()

    fun checkPassword(instance: Instance, password: String): Boolean {
        return BCrypt.checkpw(password, instance.scalars[hash].toString())
    }
}

