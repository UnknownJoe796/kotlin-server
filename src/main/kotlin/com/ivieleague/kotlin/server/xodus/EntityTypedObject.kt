package com.ivieleague.kotlin.server.xodus

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
        return when (type) {
            SBoolean, SDouble, SFloat, SInt, SLong, SString, is SPointer<*> -> entity.getProperty(field.key) ?: field.default
            SVoid -> Unit
            SDate -> (entity.getProperty(field.key) as? String)?.let { SDate.format.parse(it) } ?: field.default
            is SEnum -> (entity.getProperty(field.key) as? String)?.let { type.get(it) } ?: field.default
            else -> deferReadToJson(field, type)
        } as T
    }

    private fun <T> deferReadToJson(field: TypeField<T>, type: SType<T>): T? {
        return try {
            (entity.getProperty(field.key) as? String)?.let { type.parse(JsonGlobals.JsonObjectMapper.readTree(it)) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    override operator fun <T> set(field: TypeField<T>, value: T) {
        val type = field.type
        when (type) {
            SBoolean, SDouble, SFloat, SInt, SLong, SString, is SPointer<*> -> entity.setPropertyNullable(field.key, value as? Comparable<Nothing>)
            SVoid -> Unit
            SDate -> entity.setPropertyNullable(field.key, value?.let { SDate.format.format(it as ZonedDateTime) })
            is SEnum -> entity.setPropertyNullable(field.key, (value as? SEnum.Value)?.name)
            else -> deferWriteToJson(field, value, type)
        } as? T
    }

    private fun <T> deferWriteToJson(field: TypeField<T>, value: T, type: SType<T>): Boolean? {
        return try {
            entity.setPropertyNullable(
                    field.key,
                    value?.let { type.serialize(JsonGlobals.jsonNodeFactory, it).toString() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}