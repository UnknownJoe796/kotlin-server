package com.ivieleague.kotlin.server

sealed class ServerType {
    object TBoolean : ServerType()

    object TByte : ServerType()
    object TShort : ServerType()
    object TInt : ServerType()
    object TLong : ServerType()

    object TFloat : ServerType()
    object TDouble : ServerType()

    object TString : ServerType()
    class TPointer(val table: Table) : ServerType()
    class TListPointers(val table: Table) : ServerType()

    class TEnum(val enum: ServerEnum) : ServerType()
}