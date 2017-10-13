package com.ivieleague.kotlin.server.old.model

import com.ivieleague.kotlin.server.old.type.Instance
import com.ivieleague.kotlin.server.old.type.Link
import com.ivieleague.kotlin.server.old.type.Multilink
import com.ivieleague.kotlin.server.old.type.Primitive

class Read(
        primitives: Collection<Primitive> = listOf(),
        links: Map<Link, Read> = mapOf(),
        multilinks: Map<Multilink, Read> = mapOf()
) {
    var condition: Condition = Condition.Always
    var sort: List<Sort> = listOf()
    var startAfter: Instance? = null
    var count: Int = 100

    val primitives: HashSet<Primitive> = HashSet(primitives)
    val links: HashMap<Link, Read> = HashMap(links)
    val multilinks: HashMap<Multilink, Read> = HashMap(multilinks)

    companion object {
        val EMPTY = Read()
    }

    fun merge(other: Read) {
        for (scalar in other.primitives) {
            primitives += scalar
        }
        for ((link, read) in other.links) {
            val existing = links[link]
            if (existing == null) links[link] = read
            else existing.merge(read)
        }
        for ((multilink, read) in other.multilinks) {
            val existing = multilinks[multilink]
            if (existing == null) multilinks[multilink] = read
            else existing.merge(read)
        }
    }

    fun sortCondition(): Condition? {
        val startAfter = startAfter ?: return null
        return Condition.AnyConditions(sort.indices.map { index ->
            val equalConditions = sort.subList(0, index + 1).dropLast(1).map { Condition.ScalarEqual(primitive = it.primitive, value = startAfter) }
            val currentSort = sort[index]
            val compareCondition = if (currentSort.ascending) {
                Condition.ScalarGreaterThan(primitive = currentSort.primitive, lower = startAfter.scalars[currentSort.primitive] as Comparable<Comparable<*>>)
            } else {
                Condition.ScalarLessThan(primitive = currentSort.primitive, upper = startAfter.scalars[currentSort.primitive] as Comparable<Comparable<*>>)
            }
            Condition.AllConditions(equalConditions + compareCondition)
        })
    }

    fun isEmpty(): Boolean = primitives.isEmpty() && links.isEmpty() && multilinks.isEmpty()
}