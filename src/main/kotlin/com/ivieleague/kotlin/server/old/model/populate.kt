package com.ivieleague.kotlin.server.old.model

import com.ivieleague.kotlin.server.old.type.Instance
import com.ivieleague.kotlin.server.old.type.Link
import com.ivieleague.kotlin.server.old.type.Multilink
import com.ivieleague.kotlin.server.old.type.Primitive

fun String.populate(data: Map<String, Any?>) = if (this.startsWith("\${") && this.length > 1 && this.endsWith("}")) {
    val path = this.substring(2, this.length - 1).split('.')
    var current: Any? = data
    for (pathKey in path) {
        current = when (current) {
            is Map<*, *> -> current[pathKey]
            is Instance -> {
                val property = current.table.properties[pathKey]
                when (property) {
                    is Primitive -> current.scalars[property]
                    is Link -> current.links[property]
                    is Multilink -> current.multilinks[property]
                    else -> throw IllegalArgumentException()
                }
            }
            is List<*> -> current.getOrNull(pathKey.toIntOrNull() ?: throw IllegalArgumentException("Need index instead of argument"))
            else -> throw IllegalArgumentException()
        }
    }
    if (current is Instance)
        current.id
    else
        current
} else this

fun Request<*>.populate(data: Map<String, Any?>) = when (this) {
    is Request.Get -> this.populate(data)
    is Request.Update -> this.populate(data)
    is Request.Query -> this.populate(data)
}

fun Request.Get.populate(data: Map<String, Any?>) {
    id = id.populate(data) as String
}

fun Request.Query.populate(data: Map<String, Any?>) {
    read.condition = read.condition.populate(data)
}

fun Request.Update.populate(data: Map<String, Any?>) {
    write.populate(data)
}


fun Write.populate(data: Map<String, Any?>) {
    id = id?.let { it.populate(data) as String }

    val changes = ArrayList<Pair<Primitive, Any?>>()
    for ((scalar, value) in this.scalars) {
        changes += scalar to if (value is String) value.populate(data) else value
    }
    this.scalars += changes

    for ((_, write) in this.links) {
        write?.populate(data)
    }
    for ((_, modifications) in this.multilinks) {
        for (write in modifications.additions ?: listOf()) {
            write.populate(data)
        }
        for (write in modifications.removals ?: listOf()) {
            write.populate(data)
        }
        for (write in modifications.replacements ?: listOf()) {
            write.populate(data)
        }
    }
}

fun Condition.populate(data: Map<String, Any?>): Condition {
    return when (this) {
        Condition.Always -> this
        Condition.Never -> this
        is Condition.AllConditions -> Condition.AllConditions(conditions.map { it.populate(data) })
        is Condition.AnyConditions -> Condition.AnyConditions(conditions.map { it.populate(data) })
        is Condition.ScalarEqual -> Condition.ScalarEqual(path, primitive, if (value is String) value.populate(data) else value)
        is Condition.ScalarNotEqual -> Condition.ScalarNotEqual(path, primitive, if (value is String) value.populate(data) else value)
        is Condition.ScalarBetween<*> -> Condition.ScalarBetween<Comparable<Any?>>(path, primitive,
                if (lower is String) lower.populate(data) as Comparable<Any?> else lower as Comparable<Any?>,
                if (upper is String) upper.populate(data) as Comparable<Any?> else upper as Comparable<Any?>
        )
        is Condition.ScalarLessThanOrEqual<*> -> Condition.ScalarLessThanOrEqual<Comparable<Any?>>(path, primitive,
                if (upper is String) upper.populate(data) as Comparable<Any?> else upper as Comparable<Any?>
        )
        is Condition.ScalarGreaterThanOrEqual<*> -> Condition.ScalarGreaterThanOrEqual<Comparable<Any?>>(path, primitive,
                if (lower is String) lower.populate(data) as Comparable<Any?> else lower as Comparable<Any?>
        )
        is Condition.ScalarLessThan<*> -> Condition.ScalarLessThan<Comparable<Any?>>(path, primitive,
                if (upper is String) upper.populate(data) as Comparable<Any?> else upper as Comparable<Any?>
        )
        is Condition.ScalarGreaterThan<*> -> Condition.ScalarGreaterThan<Comparable<Any?>>(path, primitive,
                if (lower is String) lower.populate(data) as Comparable<Any?> else lower as Comparable<Any?>
        )
        is Condition.IdEquals -> Condition.IdEquals(path, id.populate(data) as String)
        is Condition.MultilinkContains -> Condition.MultilinkContains(path, multilink, id.populate(data) as String)
        is Condition.MultilinkDoesNotContain -> Condition.MultilinkDoesNotContain(path, multilink, id.populate(data) as String)
    }
}