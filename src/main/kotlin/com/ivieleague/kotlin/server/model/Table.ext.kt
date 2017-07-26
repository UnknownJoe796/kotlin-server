package com.ivieleague.kotlin.server.model

import java.util.*

private val Table_properties = WeakHashMap<Table, Map<String, Property>>()
val Table.properties: Map<String, Property>
    get() = Table_properties.getOrPut(this) {
        HashMap<String, Property>().also {
            for (scalar in this.scalars)
                it[scalar.key] = scalar
            for (link in this.links)
                it[link.key] = link
            for (multilink in this.multilinks)
                it[multilink.key] = multilink
        }
    }

abstract class SortComparator<T>(val sort: Sort) : kotlin.Comparator<T?>

fun Sort.comparator(): SortComparator<*> = when (this.scalar.type) {
    ScalarType.Boolean -> createSortComparator<Boolean>(this)
    ScalarType.Byte -> createSortComparator<Byte>(this)
    ScalarType.Short -> createSortComparator<Short>(this)
    ScalarType.Int -> createSortComparator<Int>(this)
    ScalarType.Long -> createSortComparator<Long>(this)
    ScalarType.Float -> createSortComparator<Float>(this)
    ScalarType.Double -> createSortComparator<Double>(this)
    ScalarType.ShortString -> createSortComparator<String>(this)
    ScalarType.LongString -> createSortComparator<String>(this)
    ScalarType.Date -> createSortComparator<Date>(this)
    is ScalarType.Enum -> createSortComparator<String>(this)
}

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

fun Table.comparator(sort: List<Sort>): Comparator<Instance> = object : kotlin.Comparator<Instance> {

    @Suppress("UNCHECKED_CAST")
    val comparators = sort.map { it.comparator() as SortComparator<Any?> }

    override fun compare(a: Instance, b: Instance): Int {
        for (comp in comparators) {
            val result = comp.compare(a.scalars[comp.sort.scalar], b.scalars[comp.sort.scalar])
            if (result != 0) return result
        }
        return a.id.compareTo(b.id)
    }
}