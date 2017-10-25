package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.type.SClass

object DAOPotentialExceptions {

    fun notAllowedWriteOver(type: SClass) = RPCMethod.PotentialException(
            code = 1,
            name = "Not Allowed",
            description = "You do not have permission to write over that data.",
            type = SWriteNotAllowed[type]
    )

    fun notAllowedWrite(type: SClass) = RPCMethod.PotentialException(
            code = 2,
            name = "Not Allowed",
            description = "You do not have permission to write that data.",
            type = SWriteNotAllowed[type]
    )

    fun notAllowedRead(type: SClass) = RPCMethod.PotentialException(
            code = 3,
            name = "Not Allowed",
            description = "You do not have permission to read that data.",
            type = SReadNotAllowed[type]
    )
}


