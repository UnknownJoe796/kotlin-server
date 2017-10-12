package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.exceptionBadRequest
import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Table
import java.util.*

private fun <T : Comparable<T>> createSortComparator(sort: Sort): SortComparator<T> {
    return if (sort.ascending) {
        if (sort.nullsLast) {
            object : SortComparator<T>(sort) {
                override fun compare(a: T?, b: T?): Int = if (a == null) {
                    if (b == null) {
                        0
                    } else {
                        1
                    }
                } else {
                    if (b == null) {
                        -1
                    } else {
                        a.compareTo(b)
                    }
                }
            }
        } else {
            object : SortComparator<T>(sort) {
                override fun compare(a: T?, b: T?): Int = if (a == null) {
                    if (b == null) {
                        0
                    } else {
                        -1
                    }
                } else {
                    if (b == null) {
                        1
                    } else {
                        a.compareTo(b)
                    }
                }
            }
        }
    } else {
        if (sort.nullsLast) {
            object : SortComparator<T>(sort) {
                override fun compare(a: T?, b: T?): Int = if (a == null) {
                    if (b == null) {
                        0
                    } else {
                        1
                    }
                } else {
                    if (b == null) {
                        -1
                    } else {
                        -a.compareTo(b)
                    }
                }
            }
        } else {
            object : SortComparator<T>(sort) {
                override fun compare(a: T?, b: T?): Int = if (a == null) {
                    if (b == null) {
                        0
                    } else {
                        -1
                    }
                } else {
                    if (b == null) {
                        1
                    } else {
                        -a.compareTo(b)
                    }
                }
            }
        }
    }
}

abstract class SortComparator<T>(val sort: Sort) : kotlin.Comparator<T?>

fun Sort.comparator(): SortComparator<*> = when (this.primitive.type) {
    PrimitiveType.Boolean -> createSortComparator<Boolean>(this)
    PrimitiveType.Byte -> createSortComparator<Byte>(this)
    PrimitiveType.Short -> createSortComparator<Short>(this)
    PrimitiveType.Int -> createSortComparator<Int>(this)
    PrimitiveType.Long -> createSortComparator<Long>(this)
    PrimitiveType.Float -> createSortComparator<Float>(this)
    PrimitiveType.Double -> createSortComparator<Double>(this)
    PrimitiveType.ShortString -> createSortComparator<String>(this)
    PrimitiveType.LongString -> createSortComparator<String>(this)
    PrimitiveType.JSON -> throw exceptionBadRequest("No sorting exists for JSON objects.")
    PrimitiveType.Date -> createSortComparator<Date>(this)
    is PrimitiveType.Enum -> createSortComparator<String>(this)
}

fun Table.comparator(sort: List<Sort>): Comparator<Instance> = object : kotlin.Comparator<Instance> {

    @Suppress("UNCHECKED_CAST")
    val comparators = sort.map { it.comparator() as SortComparator<Any?> }

    override fun compare(a: Instance, b: Instance): Int {
        for (comp in comparators) {
            val result = comp.compare(a.scalars[comp.sort.primitive], b.scalars[comp.sort.primitive])
            if (result != 0) return result
        }
        return a.id.compareTo(b.id)
    }
}