package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.type.SVoid

/**
 * Various exceptions that can happen when accessing the database directly.
 */
object SecurityPotentialExceptions {
    const val START_CODE = 180

    val readTypeViolation = RPCMethod.PotentialException(
            code = START_CODE + 0,
            name = "Read Not Allowed",
            description = "You do not have permission to read this object.",
            type = SVoid
    )
    val writeTypeViolation = RPCMethod.PotentialException(
            code = START_CODE + 1,
            name = "Write Not Allowed",
            description = "You do not have permission to write this object.",
            type = SVoid
    )
    val modifyTypeViolation = RPCMethod.PotentialException(
            code = START_CODE + 2,
            name = "Modify Not Allowed",
            description = "You do not have permission to modify this object.",
            type = SVoid
    )

    val readFieldViolation = RPCMethod.PotentialException(
            code = START_CODE + 3,
            name = "Read Field Not Allowed",
            description = "You do not have permission to read this field.",
            type = SVoid
    )
    val writeFieldViolation = RPCMethod.PotentialException(
            code = START_CODE + 4,
            name = "Write Field Not Allowed",
            description = "You do not have permission to write this field.",
            type = SVoid
    )
    val modifyFieldViolation = RPCMethod.PotentialException(
            code = START_CODE + 5,
            name = "Modify Field Not Allowed",
            description = "You do not have permission to modify this field.",
            type = SVoid
    )
}