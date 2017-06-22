package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.core.*
import java.util.*


object JSON {
    fun parseRead(table: Table, map: Map<String, Any?>): Read {
        val scalars = ArrayList<Scalar>()
        val links = HashMap<Link, Read>()
        val multilinks = HashMap<Multilink, Read>()

        for ((key, value) in map) {
            val property = table.properties[key]
            when (property) {
                is Scalar -> if (value !is Boolean || value) scalars += property
                is Link -> links[property] = parseRead(property.table, value as Map<String, Any?>)
                is Multilink -> multilinks[property] = parseRead(property.table, value as Map<String, Any?>)
                null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
            }
        }

        return Read(scalars, links, multilinks)
    }

    fun parseWrite(table: Table, map: Map<String, Any?>): Write {
        val scalars = HashMap<Scalar, Any?>()
        val links = HashMap<Link, Write?>()
        val multilinks = HashMap<Multilink, MultilinkModifications>()

        for ((key, value) in map) {
            if (key.startsWith('+')) {
                val property = table.properties[key.substring(1)] as Multilink
                multilinks.getOrPut(property) { MultilinkModifications() }.additions = (value as List<Map<String, Any?>>).map { item ->
                    parseWrite(property.table, item)
                }
            } else if (key.startsWith('-')) {
                val property = table.properties[key.substring(1)] as Multilink
                multilinks.getOrPut(property) { MultilinkModifications() }.removals = (value as List<Map<String, Any?>>).map { item ->
                    parseWrite(property.table, item)
                }
            } else {
                val property = table.properties[key]
                when (property) {
                    is Scalar -> {
                        val type = property.type
                        scalars[property] = when (type) {
                            ScalarType.Boolean -> value as Boolean
                            ScalarType.Byte -> (value as Number).toByte()
                            ScalarType.Short -> (value as Number).toShort()
                            ScalarType.Int -> (value as Number).toInt()
                            ScalarType.Long -> (value as Number).toLong()
                            ScalarType.Float -> (value as Number).toFloat()
                            ScalarType.Double -> (value as Number).toDouble()
                            ScalarType.ShortString -> value as String
                            ScalarType.LongString -> value as String
                            ScalarType.Date -> Date(value as Long)
                            is ScalarType.Enum -> type.enum.indexedByName[value as String]!!.value
                        }
                    }
                    is Link -> links[property] = parseWrite(property.table, value as Map<String, Any?>)
                    is Multilink -> multilinks.getOrPut(property) { MultilinkModifications() }.replacements = (value as List<Map<String, Any?>>).map { item ->
                        parseWrite(property.table, item)
                    }
                    null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
                }
            }
        }
        return Write(map["id"]?.toString(), scalars, links, multilinks)
    }

    fun parseWrite(table: Table, id: String?, map: Map<String, Any?>): Write {
        val scalars = HashMap<Scalar, Any?>()
        val links = HashMap<Link, Write?>()
        val multilinks = HashMap<Multilink, MultilinkModifications>()

        for ((key, value) in map) {
            if (key.startsWith('+')) {
                val property = table.properties[key.substring(1)] as Multilink
                multilinks.getOrPut(property) { MultilinkModifications() }.additions = (value as List<Map<String, Any?>>).map { item ->
                    parseWrite(property.table, item)
                }
            } else if (key.startsWith('-')) {
                val property = table.properties[key.substring(1)] as Multilink
                multilinks.getOrPut(property) { MultilinkModifications() }.removals = (value as List<Map<String, Any?>>).map { item ->
                    parseWrite(property.table, item)
                }
            } else {
                val property = table.properties[key]
                when (property) {
                    is Scalar -> {
                        val type = property.type
                        scalars[property] = when (type) {
                            ScalarType.Boolean -> value as Boolean
                            ScalarType.Byte -> (value as Number).toByte()
                            ScalarType.Short -> (value as Number).toShort()
                            ScalarType.Int -> (value as Number).toInt()
                            ScalarType.Long -> (value as Number).toLong()
                            ScalarType.Float -> (value as Number).toFloat()
                            ScalarType.Double -> (value as Number).toDouble()
                            ScalarType.ShortString -> value as String
                            ScalarType.LongString -> value as String
                            ScalarType.Date -> Date(value as Long)
                            is ScalarType.Enum -> type.enum.indexedByName[value as String]!!.value
                        }
                    }
                    is Link -> links[property] = parseWrite(property.table, value as Map<String, Any?>)
                    is Multilink -> multilinks.getOrPut(property) { MultilinkModifications() }.replacements = (value as List<Map<String, Any?>>).map { item ->
                        parseWrite(property.table, item)
                    }
                    null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
                }
            }
        }
        return Write(id, scalars, links, multilinks)
    }

    fun serializeInstance(instance: Instance): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        for ((key, value) in instance.scalars) {
            val type = key.type
            result[key.key] = when (type) {
                ScalarType.Date -> (value as Date).time
                is ScalarType.Enum -> type.enum.indexedByValue[value as Byte]!!.name
                else -> value
            }
        }
        for ((key, value) in instance.links) {
            result[key.key] = value?.let { serializeInstance(it) }
        }
        for ((key, values) in instance.multilinks) {
            result[key.key] = values.map { serializeInstance(it) }
        }
        result["id"] = instance.id
        return result
    }

}