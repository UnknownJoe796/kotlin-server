package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.access.*
import com.ivieleague.kotlin.server.generateString
import com.ivieleague.kotlin.server.type.*
import jetbrains.exodus.entitystore.Entity
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

    @Suppress("UNCHECKED_CAST")
    fun Entity.toTypedObject(transaction: Transaction, read: TypedObject): TypedObject {
        val output = SimpleTypedObject(this@XodusDAO.type)
        for (field in read.type.fields.values) {
            val key = field.key
            val subread = if (field.type is SRead) read[field] as? TypedObject else null
            val shouldRead = subread != null || read[field] == true
            if (shouldRead) {
                val primitiveValue = this.getProperty(key)
                val actualField = this@XodusDAO.type.fields[key] as TypeField<Any>
                output[actualField] = when {
                    actualField == IdField -> this.id.toString()
                    primitiveValue == null -> null
                    else -> actualField.fromXodusPrimitive(
                            transaction = transaction,
                            read = subread,
                            item = primitiveValue
                    )
                }
            }
        }
        return output
    }

    override fun get(transaction: Transaction, id: String, read: TypedObject): TypedObject? {
        val txn = transaction.getXodus(store)
        val entity = txn.getEntityOrNull(id) ?: return null
        return entity.toTypedObject(transaction, read)
    }

    override fun query(transaction: Transaction, read: TypedObject): List<TypedObject> {
        val txn = transaction.getXodus(store)
        return txn.getAll(type.name).map {
            it.toTypedObject(transaction, read)
        }
    }

    override fun update(transaction: Transaction, write: TypedObject): TypedObject {
        val txn = transaction.getXodus(store)

        val delete = write[SWrite.delete] ?: false
        val partialFields = (write.type as SWrite).partialFields
        val subwriteFields = (write.type as SWrite).subwriteFields.filter { it.key in type.externalFields }

        val partialIdField = partialFields[IdField]!!
        val id = (write[partialIdField] as? Exists<String>)?.value
        if (id == null && delete) return SimpleTypedObject(type)
        val entity = if (id == null) txn.newEntity(type.name) else txn.getEntityOrNull(id) ?: throw IllegalStateException("Item with ID '$id' not found.")

        val result = SimpleTypedObject(type).apply {
            this[IdField] = entity.id.toString()
        }

        for ((field, writeField) in subwriteFields) {
            val subwriteContainer = write[writeField] ?: continue
            val subwrite = subwriteContainer.value
            val external = field in type.externalFields
            if (subwrite == null) {
                entity.deleteProperty(writeField.key)
            } else {
                if (external) {
                    val resultOfSub = schema.daos[writeField.type.name]!!.update(transaction, subwrite)
                    val resultId = resultOfSub[IdField]
                    if (resultId == null) {
                        entity.deleteProperty(writeField.key)
                    } else {
                        entity.setProperty(resultId, )
                        result[field] =
                    }
                }
            }
        }

        if (delete) {
            entity.delete()
            result[IdField] = null
            return result
        }

        for ((field, writeField) in partialFields) {
            if (field == IdField) continue
            @Suppress("UNCHECKED_CAST")
            val untypedField = field as TypeField<Any>
            val toWriteContainer = write[writeField] as? Exists<Any> ?: continue
            val valueToWrite = toWriteContainer.value?.let { untypedField.toXodusPrimitive(it) }
            if (valueToWrite == null)
                entity.deleteProperty(writeField.key)
            else
                entity.setProperty(writeField.key, valueToWrite)
        }

        return result
    }
}