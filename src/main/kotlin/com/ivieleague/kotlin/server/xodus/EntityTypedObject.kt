package com.ivieleague.kotlin.server.xodus

import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.access.IdField
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.StoreTransaction
import java.time.ZonedDateTime

fun Entity.toTypedObject(type: SClass, transaction: StoreTransaction) = EntityTypedObject(
        type,
        transaction,
        this
)

fun StoreTransaction.newTypedObject(type: SClass) = EntityTypedObject(
        type = type,
        transaction = this,
        entity = this.newEntity(type.name)
)

@Suppress("UNCHECKED_CAST")
fun StoreTransaction.newTypedObject(item: TypedObject) = EntityTypedObject(
        type = item.type,
        transaction = this,
        entity = this.newEntity(item.type.name)
).apply {
    val idField = IdField[item.type]
    for (field in item.type.fields.values) {
        if (idField == field) continue
        val untypedField = field as TypeField<Any?>
        this[untypedField] = item[untypedField]
    }
}

fun StoreTransaction.getTypedObject(type: SClass, id: String) = getEntity(id).let {
    EntityTypedObject(
            type = type,
            transaction = this,
            entity = it
    )
}

fun StoreTransaction.getTypedObjectOrNull(type: SClass, id: String) = getEntityOrNull(id)?.let {
    EntityTypedObject(
            type = type,
            transaction = this,
            entity = it
    )
}

fun <T : Comparable<*>> StoreTransaction.find(type: SClass, property: TypeField<T>, value: T)
        = find(type.name, property.key, value)

fun <T : Comparable<*>> StoreTransaction.findNullable(type: SClass, property: TypeField<T?>, value: T)
        = find(type.name, property.key, value)


/**
 * A TypedObject that is backed by a Xodus entity.  This allows for persisting a TypedObject effectively.
 */
class EntityTypedObject(
        override val type: SClass,
        val transaction: StoreTransaction,
        val entity: Entity
) : MutableTypedObject {
    val idField = IdField[type]

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    override fun <T> get(field: TypeField<T>): T {
        if (field == idField)
            return entity.id.toString() as T
        val type = field.type
        return convertFromXodus<T>(field.type, entity.getProperty(field.key))
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    override operator fun <T> set(field: TypeField<T>, value: T) {
        val type = field.type
        val write = convertToXodus(type, value)
        if (write != null) {
            entity.setPropertyNullable(field.key, write)
        }
    }

    companion object {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        fun <T> convertFromXodus(type: SType<T>, value: Comparable<*>?): T {
            return when (type) {
                SBoolean, SDouble, SFloat, SInt, SLong, SString, is SPointer<*> -> value
                SVoid -> Unit
                SDate -> (value as? String)?.let { SDate.format.parse(it) }
                is SEnum -> (value as? String)?.let { type[it] }
                else -> value?.let { deferReadToJson(type, it as String) }
            } as T
        }

        private fun <T> deferReadToJson(type: SType<T>, value: String): T =
                type.parse(JsonGlobals.JsonObjectMapper.readTree(value))

        fun <T> convertToXodus(type: SType<T>, value: T): Comparable<*>? {
            return when (type) {
                SBoolean, SDouble, SFloat, SInt, SLong, SString, is SPointer<*> -> value as Comparable<*>
                SVoid -> null
                SDate -> (value as? ZonedDateTime)?.let { SDate.format.format(it) }
                is SEnum -> (value as? SEnum.Value)?.name
                else -> deferWriteToJson(type, value)?.toString()
            }
        }

        private fun <T> deferWriteToJson(type: SType<T>, value: T): JsonNode? =
                type.serialize(JsonGlobals.jsonNodeFactory, value)
    }
}