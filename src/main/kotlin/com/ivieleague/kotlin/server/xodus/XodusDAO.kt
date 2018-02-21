package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.access.*
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.StoreTransaction
import kotlin.math.nextDown
import kotlin.math.nextUp

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

    fun entityIterator(transaction: Transaction): Iterator<EntityTypedObject> = object : Iterator<EntityTypedObject> {
        val txn = transaction.getXodus(store)
        val wraps = txn.getAll(type.name).iterator()

        override fun hasNext(): Boolean = wraps.hasNext()
        override fun next(): EntityTypedObject = wraps.next().toTypedObject(type, txn)
    }

    private inline fun conditionAll(
            txn: StoreTransaction,
            condition: TypedObject,
            generateIterable: (TypeField<*>, Any?) -> EntityIterable
    ): EntityIterable {

        val value = condition[conditionType.fieldValue]
        return type.fields.values.mapNotNull { field ->
            val subvalueExists = value[SPartialClass[type].mappedFields[field]!!] as Partial<*>
            subvalueExists.letNotNull(
                    ifNotNull = {
                        val subvalue = EntityTypedObject.convertToXodus(field.type as SType<Any?>, it)
                        generateIterable(field, subvalue)
                    },
                    otherwise = { null }
            )
        }.reduce { first, second ->
            first.intersect(second)
        }
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
                val sublist = condition[conditionType.fieldConditions]
                if (sublist.isEmpty()) txn.getAll(type.name)
                else sublist.map { conditionToIterable(txn, it) }
                        .reduce { first, second ->
                            if (first == null) second
                            else if (second == null) first
                            else first.union(second)
                        }
            }
            conditionType.all -> {
                val sublist = condition[conditionType.fieldConditions]
                if (sublist.isEmpty()) txn.getAll(type.name)
                else sublist.map { conditionToIterable(txn, it) }
                        .reduce { first, second ->
                            if (first == null || second == null) null
                            else first.intersect(second)
                        }
            }
            conditionType.equal -> conditionAll(txn, condition, { field, subvalue ->
                txn.find(
                        type.name,
                        field.key,
                        subvalue as Comparable<*>
                )
            })
            conditionType.notEqual -> txn.getAll(type.name)
                    .minus(conditionAll(txn, condition, { field, subvalue ->
                        txn.find(
                                type.name,
                                field.key,
                                subvalue as Comparable<*>
                        )
                    }))
            conditionType.greaterThanOrEqualTo -> conditionAll(txn, condition, { field, subvalue ->
                getIterableGreaterThanEqual(txn, field, subvalue ?: throw IllegalArgumentException("null is not a valid comparable value."))
            })
            conditionType.lessThanOrEqualTo -> conditionAll(txn, condition, { field, subvalue ->
                getIterableLessThanEqual(txn, field, subvalue ?: throw IllegalArgumentException("null is not a valid comparable value."))
            })
            conditionType.greaterThan -> conditionAll(txn, condition, { field, subvalue ->
                getIterableGreaterThan(txn, field, subvalue ?: throw IllegalArgumentException("null is not a valid comparable value."))
            })
            conditionType.lessThan -> conditionAll(txn, condition, { field, subvalue ->
                getIterableLessThan(txn, field, subvalue ?: throw IllegalArgumentException("null is not a valid comparable value."))
            })
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

        if (start != null) {
            for (sortParameter in sort) {
                if (sortParameter.ascending) {
                    entityIterable = entityIterable.intersect(getIterableGreaterThan(txn, sortParameter.field, start[sortParameter.field] ?: throw IllegalArgumentException("Null is not a valid sort value!")))
                } else {
                    entityIterable = entityIterable.intersect(getIterableLessThan(txn, sortParameter.field, start[sortParameter.field] ?: throw IllegalArgumentException("Null is not a valid sort value!")))
                }
            }
            entityIterable = entityIterable.intersect(txn.findIds(type.name, start[IdField[type]]!!.substringAfter('-').toLong(), Long.MAX_VALUE))
        }

        if (!sort.isEmpty()) {
            entityIterable = txn.sort(type.name, sort.first().field.key, entityIterable, sort.first().ascending)
        }

        return entityIterable.take(count).map {
            EntityTypedObject(type, txn, it).mutate()
        }
    }

    fun getIterableLessThan(txn: StoreTransaction, field: TypeField<*>, value: Any): EntityIterable
            = when (field.type) {
        SString -> getIterableStringLessThan(txn, field as TypeField<String>, value as String)
        SInt -> getIterableIntLessThan(txn, field as TypeField<Int>, value as Int)
        SLong -> getIterableLongLessThan(txn, field as TypeField<Long>, value as Long)
        SFloat -> getIterableFloatLessThan(txn, field as TypeField<Float>, value as Float)
        SDouble -> getIterableDoubleLessThan(txn, field as TypeField<Double>, value as Double)
        else -> throw IllegalArgumentException("Cannot handle type ${field.type.name} for sorting.")
    }

    fun getIterableGreaterThan(txn: StoreTransaction, field: TypeField<*>, value: Any): EntityIterable
            = when (field.type) {
        SString -> getIterableStringGreaterThan(txn, field as TypeField<String>, value as String)
        SInt -> getIterableIntGreaterThan(txn, field as TypeField<Int>, value as Int)
        SLong -> getIterableLongGreaterThan(txn, field as TypeField<Long>, value as Long)
        SFloat -> getIterableFloatGreaterThan(txn, field as TypeField<Float>, value as Float)
        SDouble -> getIterableDoubleGreaterThan(txn, field as TypeField<Double>, value as Double)
        else -> throw IllegalArgumentException("Cannot handle type ${field.type.name} for sorting.")
    }

    fun getIterableLessThanEqual(txn: StoreTransaction, field: TypeField<*>, value: Any): EntityIterable
            = when (field.type) {
        SString -> getIterableStringLessThanEqual(txn, field as TypeField<String>, value as String)
        SInt -> getIterableIntLessThanEqual(txn, field as TypeField<Int>, value as Int)
        SLong -> getIterableLongLessThanEqual(txn, field as TypeField<Long>, value as Long)
        SFloat -> getIterableFloatLessThanEqual(txn, field as TypeField<Float>, value as Float)
        SDouble -> getIterableDoubleLessThanEqual(txn, field as TypeField<Double>, value as Double)
        else -> throw IllegalArgumentException("Cannot handle type ${field.type.name} for sorting.")
    }

    fun getIterableGreaterThanEqual(txn: StoreTransaction, field: TypeField<*>, value: Any): EntityIterable
            = when (field.type) {
        SString -> getIterableStringGreaterThanEqual(txn, field as TypeField<String>, value as String)
        SInt -> getIterableIntGreaterThanEqual(txn, field as TypeField<Int>, value as Int)
        SLong -> getIterableLongGreaterThanEqual(txn, field as TypeField<Long>, value as Long)
        SFloat -> getIterableFloatGreaterThanEqual(txn, field as TypeField<Float>, value as Float)
        SDouble -> getIterableDoubleGreaterThanEqual(txn, field as TypeField<Double>, value as Double)
        else -> throw IllegalArgumentException("Cannot handle type ${field.type.name} for sorting.")
    }

    fun getIterableIntLessThan(txn: StoreTransaction, field: TypeField<Int>, value: Int): EntityIterable
            = txn.find(type.name, field.key, Int.MIN_VALUE, value - 1)

    fun getIterableIntGreaterThan(txn: StoreTransaction, field: TypeField<Int>, value: Int): EntityIterable
            = txn.find(type.name, field.key, value + 1, Int.MAX_VALUE)

    fun getIterableIntLessThanEqual(txn: StoreTransaction, field: TypeField<Int>, value: Int): EntityIterable
            = txn.find(type.name, field.key, Int.MIN_VALUE, value)

    fun getIterableIntGreaterThanEqual(txn: StoreTransaction, field: TypeField<Int>, value: Int): EntityIterable
            = txn.find(type.name, field.key, value, Int.MAX_VALUE)

    fun getIterableLongLessThan(txn: StoreTransaction, field: TypeField<Long>, value: Long): EntityIterable
            = txn.find(type.name, field.key, Long.MIN_VALUE, value - 1)

    fun getIterableLongGreaterThan(txn: StoreTransaction, field: TypeField<Long>, value: Long): EntityIterable
            = txn.find(type.name, field.key, value + 1, Long.MAX_VALUE)

    fun getIterableLongLessThanEqual(txn: StoreTransaction, field: TypeField<Long>, value: Long): EntityIterable
            = txn.find(type.name, field.key, Long.MIN_VALUE, value)

    fun getIterableLongGreaterThanEqual(txn: StoreTransaction, field: TypeField<Long>, value: Long): EntityIterable
            = txn.find(type.name, field.key, value, Long.MAX_VALUE)

    fun getIterableDoubleLessThan(txn: StoreTransaction, field: TypeField<Double>, value: Double): EntityIterable
            = txn.find(type.name, field.key, Double.MIN_VALUE, value.nextDown())

    fun getIterableDoubleGreaterThan(txn: StoreTransaction, field: TypeField<Double>, value: Double): EntityIterable
            = txn.find(type.name, field.key, value.nextUp(), Double.MAX_VALUE)

    fun getIterableDoubleLessThanEqual(txn: StoreTransaction, field: TypeField<Double>, value: Double): EntityIterable
            = txn.find(type.name, field.key, Double.MIN_VALUE, value)

    fun getIterableDoubleGreaterThanEqual(txn: StoreTransaction, field: TypeField<Double>, value: Double): EntityIterable
            = txn.find(type.name, field.key, value, Double.MAX_VALUE)

    fun getIterableFloatLessThan(txn: StoreTransaction, field: TypeField<Float>, value: Float): EntityIterable
            = txn.find(type.name, field.key, Float.MIN_VALUE, value.nextDown())

    fun getIterableFloatGreaterThan(txn: StoreTransaction, field: TypeField<Float>, value: Float): EntityIterable
            = txn.find(type.name, field.key, value.nextUp(), Float.MAX_VALUE)

    fun getIterableFloatLessThanEqual(txn: StoreTransaction, field: TypeField<Float>, value: Float): EntityIterable
            = txn.find(type.name, field.key, Float.MIN_VALUE, value)

    fun getIterableFloatGreaterThanEqual(txn: StoreTransaction, field: TypeField<Float>, value: Float): EntityIterable
            = txn.find(type.name, field.key, value, Float.MAX_VALUE)

    //TODO: Fix
    fun getIterableStringLessThan(txn: StoreTransaction, field: TypeField<String>, value: String): EntityIterable
            = txn.find(type.name, field.key, object : Comparable<String> {
        override fun compareTo(other: String): Int = 1
    }, value)

    //TODO: Fix
    fun getIterableStringGreaterThan(txn: StoreTransaction, field: TypeField<String>, value: String): EntityIterable
            = txn.find(type.name, field.key, value, object : Comparable<String> {
        override fun compareTo(other: String): Int = -1
    })

    fun getIterableStringLessThanEqual(txn: StoreTransaction, field: TypeField<String>, value: String): EntityIterable
            = txn.find(type.name, field.key, object : Comparable<String> {
        override fun compareTo(other: String): Int = 1
    }, value)

    fun getIterableStringGreaterThanEqual(txn: StoreTransaction, field: TypeField<String>, value: String): EntityIterable
            = txn.find(type.name, field.key, value, object : Comparable<String> {
        override fun compareTo(other: String): Int = -1
    })
}