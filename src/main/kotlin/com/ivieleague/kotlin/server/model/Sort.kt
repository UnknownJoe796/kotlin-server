package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Primitive

data class Sort(val primitive: Primitive, val ascending: Boolean = true, val nullsLast: Boolean = true) {
}

fun List<Sort>.instanceComparator() = Comparator<Instance> { a, b ->
    for (sort in this) {
        val aValue = a.scalars[sort.primitive] as? Comparable<Any>
        val bValue = b.scalars[sort.primitive] as? Comparable<Any>
        val resultSub = if (aValue == null) {
            if (bValue == null) 0
            else if (sort.nullsLast) 1
            else -1
        } else if (bValue == null) {
            if (sort.nullsLast) -1
            else 1
        } else if (sort.ascending)
            aValue.compareTo(bValue)
        else
            bValue.compareTo(aValue)

        if (resultSub != 0)
            return@Comparator resultSub
    }

    return@Comparator a.id.compareTo(b.id)
}