package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.access.*
import com.ivieleague.kotlin.server.generateString
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.PersistentEntityStore

class XodusDAO(val schema: Schema, val store: PersistentEntityStore, override val type: SDatabaseClass) : DAO {

    init {
        assert(type.fields.values.contains(IdField))
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun <T : Any> TypeField<T>.toXodusPrimitive(item: T): Comparable<*>? {
        val fieldType = this.type
        val external = this in this@XodusDAO.type.externalFields
        return when (fieldType) {
            SBoolean, SDouble, SFloat, SInt, SLong, SString -> item

            is SClass -> if (external) (item as TypedObject)[IdField]
            else JsonGlobals.jsonFactory.generateString { fieldType.serialize(this, item) }

            is SInterface -> if (external) (item as TypedObject).let { it.type.name + "|" + it[IdField] }
            else JsonGlobals.jsonFactory.generateString { fieldType.serialize(this, item) }

            else -> JsonGlobals.jsonFactory.generateString { fieldType.serialize(this, item) }
        } as? Comparable<*>
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun <T : Any> TypeField<T>.fromXodusPrimitive(transaction: Transaction, read: TypedObject?, item: Comparable<T>): T? {
        val fieldType = this.type
        val external = this in this@XodusDAO.type.externalFields
        return when (fieldType) {
            SBoolean, SDouble, SFloat, SInt, SLong, SString -> item

            is SClass -> if (external) schema.daos[fieldType.name]!!.get(transaction, item as String, read!!)
            else fieldType.parse(JsonGlobals.JsonObjectMapper.readTree(item as String))

            is SInterface -> if (external) {
                val actualType = schema.classes[(item as String).substringBefore('|')]!!
                val id = (item as String).substringAfter('|')
                schema.daos[actualType.name]!!.get(transaction, id, read!!)
            } else {
                fieldType.parse(JsonGlobals.JsonObjectMapper.readTree(item as String))
            }

            else -> fieldType.parse(JsonGlobals.JsonObjectMapper.readTree(item as String))
        } as? T
    }


    override fun get(transaction: Transaction, id: String, read: TypedObject): TypedObject {
        val txn = transaction.getXodus(store)
    }

    override fun query(transaction: Transaction, read: TypedObject): List<TypedObject> {
        val txn = transaction.getXodus(store)
        txn.getAll(type.name).
    }

    override fun update(transaction: Transaction, write: TypedObject): TypedObject {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}