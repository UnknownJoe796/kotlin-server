//package com.ivieleague.kotlin.server.xodus
//
//import com.ivieleague.kotlin.server.access.DAO
//import com.ivieleague.kotlin.server.access.IdField
//import com.ivieleague.kotlin.server.access.Query
//import com.ivieleague.kotlin.server.access.Transaction
//import com.ivieleague.kotlin.server.type.SClass
//import com.ivieleague.kotlin.server.type.TypedObject
//import jetbrains.exodus.entitystore.Entity
//import jetbrains.exodus.entitystore.StoreTransaction
//
//class XodusDAO(override val type: SClass) : DAO {
//
//    init {
//        assert(type.fields.values.contains(IdField))
//    }
//
//    fun Entity.toTypedObject(transaction: Transaction, txn: StoreTransaction, read:TypedObject):TypedObject{
//
//    }
//
//    fun get(transaction: Transaction, id: String, read: TypedObject): TypedObject {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun query(transaction: Transaction, read: TypedObject): List<TypedObject> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun update(transaction: Transaction, write: TypedObject): TypedObject {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}