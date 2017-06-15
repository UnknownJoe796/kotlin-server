package com.ivieleague.kotlin.server

import java.util.*


object JSON {
    fun parseOutput(table: Table, map: Map<String, Any?>): Output {
        val scalars = ArrayList<Scalar>()
        val links = HashMap<Link, Output>()
        val multilinks = HashMap<MultiLink, Output>()

        for ((key, value) in map) {
            val property = table.properties[key]
            when (property) {
                is Scalar -> if (value !is Boolean || value) scalars += property
                is Link -> links[property] = parseOutput(property.table, value as Map<String, Any?>)
                is MultiLink -> multilinks[property] = parseOutput(property.table, value as Map<String, Any?>)
                null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
            }
        }

        return Output(scalars, links, multilinks)
    }

    fun parseInput(table: Table, map: Map<String, Any?>): Input {
        val scalars = HashMap<Scalar, Any?>()
        val links = HashMap<Link, Input>()
        val multilinkReplacements = HashMap<MultiLink, List<Input>>()
        val multilinkAdditions = HashMap<MultiLink, List<Input>>()
        val multilinkSubtractions = HashMap<MultiLink, List<Input>>()

        for ((key, value) in map) {
            if (key.startsWith('+')) {
                val property = table.properties[key.substring(1)] as MultiLink
                multilinkAdditions[property] = (value as List<Map<String, Any?>>).map { item ->
                    parseInput(property.table, item)
                }
            } else if (key.startsWith('-')) {
                val property = table.properties[key.substring(1)] as MultiLink
                multilinkSubtractions[property] = (value as List<Map<String, Any?>>).map { item ->
                    parseInput(property.table, item)
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
                    is Link -> links[property] = parseInput(property.table, value as Map<String, Any?>)
                    is MultiLink -> multilinkReplacements[property] = (value as List<Map<String, Any?>>).map { item ->
                        parseInput(property.table, item)
                    }
                    null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
                }
            }
        }
        return Input(map["id"]?.toString(), scalars, links, multilinkReplacements, multilinkAdditions, multilinkSubtractions)
    }

    fun parseInput(table: Table, id: String?, map: Map<String, Any?>): Input {
        val scalars = HashMap<Scalar, Any?>()
        val links = HashMap<Link, Input>()
        val multilinkReplacements = HashMap<MultiLink, List<Input>>()
        val multilinkAdditions = HashMap<MultiLink, List<Input>>()
        val multilinkSubtractions = HashMap<MultiLink, List<Input>>()

        for ((key, value) in map) {
            if (key.startsWith('+')) {
                val property = table.properties[key.substring(1)] as MultiLink
                multilinkAdditions[property] = (value as List<Map<String, Any?>>).map { item ->
                    parseInput(property.table, item)
                }
            } else if (key.startsWith('-')) {
                val property = table.properties[key.substring(1)] as MultiLink
                multilinkSubtractions[property] = (value as List<Map<String, Any?>>).map { item ->
                    parseInput(property.table, item)
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
                    is Link -> links[property] = parseInput(property.table, value as Map<String, Any?>)
                    is MultiLink -> multilinkReplacements[property] = (value as List<Map<String, Any?>>).map { item ->
                        parseInput(property.table, item)
                    }
                    null -> throw IllegalArgumentException("Key '$key' not found in table $table.")
                }
            }
        }
        return Input(id, scalars, links, multilinkReplacements, multilinkAdditions, multilinkSubtractions)
    }

    fun serializeInstance(instance: Instance): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        for ((key, value) in instance.scalars) {
            val type = key.type
            result[key.name] = when (type) {
                ScalarType.Date -> (value as Date).time
                is ScalarType.Enum -> type.enum.indexedByValue[value as Byte]!!.name
                else -> value
            }
        }
        for ((key, value) in instance.links) {
            result[key.name] = value?.let { serializeInstance(it) }
        }
        for ((key, values) in instance.multilinks) {
            result[key.name] = values.map { serializeInstance(it) }
        }
        result["id"] = instance.id
        result["_type"] = instance.table.tableName
        return result
    }

}