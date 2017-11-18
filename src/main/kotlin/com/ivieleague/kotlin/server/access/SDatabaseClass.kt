package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SVoid
import com.ivieleague.kotlin.server.type.TypeField
import com.ivieleague.kotlin.server.type.TypedObject
import com.ivieleague.kotlin.server.type.meta.SField

typealias SecurityRule = (user: TypedObject?, item: TypedObject) -> Boolean
object SecurityRules {
    val always: SecurityRule = { _, _ -> true }
    val never: SecurityRule = { _, _ -> false }
}

data class DataSecurityRules(
        val read: SecurityRule = SecurityRules.always,
        val write: SecurityRule = SecurityRules.always,
        val modify: SecurityRule = SecurityRules.always
) {
    companion object {
        val always = DataSecurityRules()
        val never = DataSecurityRules(
                SecurityRules.never,
                SecurityRules.never,
                SecurityRules.never
        )
    }
}

interface SDatabaseClass : SClass {
    val externalFields: Set<TypeField<*>> get() = setOf()
    val wholeSecurityRules: DataSecurityRules get() = DataSecurityRules.always
    val fieldSecurityRules: Map<TypeField<*>, DataSecurityRules> get() = mapOf()

    companion object {
        const val CODE_START = 3210

        val exceptionModifyFieldNotAllowed = RPCMethod.PotentialException(
                CODE_START + 0,
                "Modify Field Not Allowed",
                "You are not allowed to modify to this field.",
                type = SField
        )
        val exceptionWriteFieldNotAllowed = RPCMethod.PotentialException(
                CODE_START + 1,
                "Write Field Not Allowed",
                "You are not allowed to write to this field.",
                type = SField
        )
        val exceptionReadFieldNotAllowed = RPCMethod.PotentialException(
                CODE_START + 2,
                "Read Field Not Allowed",
                "You are not allowed to read to this field.",
                type = SField
        )

        val exceptionModifyObjectNotAllowed = RPCMethod.PotentialException(
                CODE_START + 3,
                "Modify Object Not Allowed",
                "You are not allowed to modify to this object.",
                type = SVoid
        )
        val exceptionWriteObjectNotAllowed = RPCMethod.PotentialException(
                CODE_START + 4,
                "Write Object Not Allowed",
                "You are not allowed to write to this object.",
                type = SVoid
        )
        val exceptionReadObjectNotAllowed = RPCMethod.PotentialException(
                CODE_START + 5,
                "Read Object Not Allowed",
                "You are not allowed to read to this object.",
                type = SVoid
        )

        val exceptions = listOf(
                exceptionModifyFieldNotAllowed,
                exceptionWriteFieldNotAllowed,
                exceptionReadFieldNotAllowed,
                exceptionModifyObjectNotAllowed,
                exceptionWriteObjectNotAllowed,
                exceptionReadObjectNotAllowed
        )
    }
}