package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.access.*
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.StoreTransaction

/**
 * A DAO backed by a Xodus database.
 */
class XodusDAO(val store: PersistentEntityStore, override val type: SClass) : ModifyDAO<TypedObject, TypedObject>, QueryableDAO<TypedObject> {

    override val modifyType: SPartialClass
        get() = SPartialClass[type]

    init {
        assert(type.fields.values.contains(IdField[type])) { "${type.name} does not contain ID field" }
    }

    override fun get(transaction: Transaction, pointer: String): TypedObject? {
        val txn = transaction.getXodus(store)
        return txn.getTypedObjectOrNull(type, pointer)?.mutate()
    }

    override fun create(transaction: Transaction, value: TypedObject): String {
        val txn = transaction.getXodus(store)
        return txn.newTypedObject(value).entity.id.toString()
    }

    override fun set(transaction: Transaction, pointer: String, value: TypedObject) {
        val txn = transaction.getXodus(store)
        val entity = txn.getTypedObjectOrNull(type, pointer) ?: txn.newTypedObject(value)
        entity.pullFrom(value)
    }

    override fun modify(transaction: Transaction, pointer: String, value: TypedObject) {
        val txn = transaction.getXodus(store)
        val entity = txn.getTypedObjectOrNull(type, pointer) ?: txn.newTypedObject(value)
        modifyType.pullFromPartial(entity, value)
    }

    override fun delete(transaction: Transaction, pointer: String) {
        val txn = transaction.getXodus(store)
        txn.getTypedObjectOrNull(type, pointer)?.entity?.delete()
    }

    val conditionType = SCondition[type]
    fun conditionToIterable(txn: StoreTransaction, condition: TypedObject): EntityIterable? {
        return when (condition.type) {
            conditionType.always -> {
                txn.getAll(type.name)
            }
            conditionType.never -> {
                null
            }
            conditionType.any -> {
                condition[conditionType.fieldConditions]!!
                        .map { conditionToIterable(txn, it) }
                        .reduce { first, second ->
                            if (first == null) second
                            else if (second == null) first
                            else first.union(second)
                        }
            }
            conditionType.all -> {
                condition[conditionType.fieldConditions]!!
                        .map { conditionToIterable(txn, it) }
                        .reduce { first, second ->
                            if (first == null || second == null) null
                            else first.intersect(second)
                        }
            }
            conditionType.equal -> {
                val value = condition[conditionType.fieldValue]
                type.fields.values.mapNotNull { field ->
                    val subvalueExists = value[SPartialClass[type].mappedFields[field]!!] as Exists<*>
                    subvalueExists.letNotNull(
                            ifNotNull = {
                                val subvalue = EntityTypedObject.convertToXodus(field.type as SType<Any?>, it)
                                if (subvalue == null) {
                                    txn.getAll(type.name).minus(txn.findWithProp(type.name, field.key))
                                } else {
                                    txn.find(
                                            type.name,
                                            field.key,
                                            subvalue
                                    )
                                }
                            },
                            otherwise = { null }
                    )
                }.reduce { first, second ->
                    first.intersect(second)
                }
            }
            conditionType.notEqual -> {
                throw UnsupportedOperationException()
            }
            conditionType.greaterThanOrEqualTo -> {
                throw UnsupportedOperationException()
            }
            conditionType.lessThanOrEqualTo -> {
                throw UnsupportedOperationException()
            }
            conditionType.greaterThan -> {
                throw UnsupportedOperationException()
            }
            conditionType.lessThan -> {
                throw UnsupportedOperationException()
            }
            else -> throw UnsupportedOperationException()
        }
    }

    override fun query(
            transaction: Transaction,
            condition: TypedObject,
            sort: List<Sort>,
            count: Int,
            start: TypedObject?
    ): List<TypedObject> {

        //current limitations
        assert(sort.size <= 1)
        assert(start == null)

        val txn = transaction.getXodus(store)

        var entityIterable: EntityIterable = conditionToIterable(txn, condition) ?: return listOf()

        if (!sort.isEmpty()) {
            entityIterable = txn.sort(type.name, sort.first().field.key, entityIterable, sort.first().ascending)
        }

        return entityIterable.take(count).map {
            EntityTypedObject(type, txn, it).mutate()
        }
    }
}