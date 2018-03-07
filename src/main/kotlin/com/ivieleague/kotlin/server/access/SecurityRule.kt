package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.*
import java.util.*

typealias SecurityRule<T> = (transaction: Transaction, item: T) -> Boolean

private val TypeField_readSecurityRule = WeakHashMap<TypeField<*>, SecurityRule<*>>()
var <T> TypeField<T>.readSecurityRule: SecurityRule<T>?
    get() = TypeField_readSecurityRule[this] as? SecurityRule<T>
    set(value) {
        TypeField_readSecurityRule[this] = value
    }
private val TypeField_writeSecurityRule = WeakHashMap<TypeField<*>, SecurityRule<*>>()
var <T> TypeField<T>.writeSecurityRule: SecurityRule<T>?
    get() = TypeField_writeSecurityRule[this] as? SecurityRule<T>
    set(value) {
        TypeField_writeSecurityRule[this] = value
    }
private val TypeField_modifySecurityRule = WeakHashMap<TypeField<*>, SecurityRule<TypedObject>>()
var <T> TypeField<T>.modifySecurityRule: SecurityRule<TypedObject>?
    get() = TypeField_modifySecurityRule[this]
    set(value) {
        TypeField_modifySecurityRule[this] = value
    }

private val SType_readSecurityRule = WeakHashMap<SType<*>, SecurityRule<*>>()
var <T> SType<T>.readSecurityRule: SecurityRule<T>?
    get() = SType_readSecurityRule[this] as? SecurityRule<T>
    set(value) {
        SType_readSecurityRule[this] = value
    }
private val SType_writeSecurityRule = WeakHashMap<SType<*>, SecurityRule<*>>()
var <T> SType<T>.writeSecurityRule: SecurityRule<T>?
    get() = SType_writeSecurityRule[this] as? SecurityRule<T>
    set(value) {
        SType_writeSecurityRule[this] = value
    }
private val SType_modifySecurityRule = WeakHashMap<SType<*>, SecurityRule<*>>()
var <T> SType<T>.modifySecurityRule: SecurityRule<T>?
    get() = SType_modifySecurityRule[this] as? SecurityRule<T>
    set(value) {
        SType_modifySecurityRule[this] = value
    }

fun SClass.assertWriteSecure(transaction: Transaction, value: TypedObject) {
    if (writeSecurityRule?.invoke(transaction, value) == false)
        throw SecurityPotentialExceptions.writeTypeViolation.exception()
    for (field in fields.values) {
        val untypedField = field as TypeField<Any?>
        if (untypedField.writeSecurityRule?.invoke(transaction, value[field]) == false) {
            throw SecurityPotentialExceptions.writeFieldViolation.exception()
        }
    }
}

fun SClass.assertPartialWriteSecure(transaction: Transaction, partialWrite: TypedObject) {
    for (field in fields.values) {
        val untypedField = field as TypeField<Any?>
        val exists = partialWrite[field] as? Partial<Any?>
        if (exists != null && untypedField.writeSecurityRule?.invoke(transaction, exists.value) == false) {
            throw SecurityPotentialExceptions.writeFieldViolation.exception()
        }
    }
}

fun SClass.assertModifySecure(transaction: Transaction, lazyValue: () -> TypedObject) {
    if (modifySecurityRule == null && fields.values.all { it.modifySecurityRule == null })
        return
    val value = lazyValue.invoke()
    if (modifySecurityRule?.invoke(transaction, value) == false)
        throw SecurityPotentialExceptions.modifyTypeViolation.exception()
    for (field in fields.values) {
        val untypedField = field as TypeField<Any?>
        if (untypedField.modifySecurityRule?.invoke(transaction, value) == false) {
            throw SecurityPotentialExceptions.modifyFieldViolation.exception()
        }
    }
}

fun SClass.assertReadSecure(transaction: Transaction, value: TypedObject) {
    if (readSecurityRule?.invoke(transaction, value) == false)
        throw SecurityPotentialExceptions.readTypeViolation.exception()
}

fun SClass.checkReadSecure(transaction: Transaction, value: TypedObject): Boolean {
    if (readSecurityRule?.invoke(transaction, value) == false)
        return false
    return true
}

fun SClass.filterReadSecure(transaction: Transaction, value: TypedObject): TypedObject {
    val copy = SimpleTypedObject(this)
    for (field in fields.values) {
        val untypedField = field as TypeField<Any?>
        if (untypedField.readSecurityRule?.invoke(transaction, value[field]) == false) {
            //don't copy
        } else {
            copy[untypedField] = value[untypedField]
        }
    }
    return copy
}