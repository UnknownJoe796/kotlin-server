package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.access.ModifyDAO
import com.ivieleague.kotlin.server.access.QueryableDAO
import com.ivieleague.kotlin.server.access.SPartialClass
import com.ivieleague.kotlin.server.access.Sort
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.*
import org.mindrot.jbcrypt.BCrypt

class HashingDAO(
        val innerAsQuerable: QueryableDAO<TypedObject>,
        val innerAsModify: ModifyDAO<TypedObject, TypedObject>,
        val hashField: TypeField<String>
) : QueryableDAO<TypedObject>, ModifyDAO<TypedObject, TypedObject> {

    override val type: SType<TypedObject> get() = innerAsQuerable.type
    override val modifyType = innerAsModify.modifyType as SPartialClass

    override fun query(transaction: Transaction, condition: TypedObject, sort: List<Sort>, count: Int, start: TypedObject?): List<TypedObject> = innerAsQuerable.query(transaction, condition, sort, count, start)

    override fun get(transaction: Transaction, pointer: String): TypedObject? = innerAsQuerable.get(transaction, pointer)

    override fun create(transaction: Transaction, value: TypedObject): String = innerAsQuerable.create(transaction, value.mutate().apply {
        this[hashField] = BCrypt.hashpw(value[hashField], BCrypt.gensalt())
    })

    override fun set(transaction: Transaction, pointer: String, value: TypedObject) = innerAsQuerable.set(transaction, pointer, value.mutate().apply {
        this[hashField] = BCrypt.hashpw(value[hashField], BCrypt.gensalt())
    })

    override fun delete(transaction: Transaction, pointer: String) = innerAsQuerable.delete(transaction, pointer)

    override fun modify(transaction: Transaction, pointer: String, value: TypedObject) = innerAsModify.modify(transaction, pointer, value.mutate().apply {
        val hashField = modifyType.mappedFields[hashField] as TypeField<Partial<String>>
        val originalValueExists = value[hashField]
        if (originalValueExists.exists) {
            this[hashField] = Partial(BCrypt.hashpw(originalValueExists.value!!, BCrypt.gensalt()))
        }
    })

    fun check(value: TypedObject, plainText: String): Boolean {
        return BCrypt.checkpw(plainText, value[hashField])
    }
}