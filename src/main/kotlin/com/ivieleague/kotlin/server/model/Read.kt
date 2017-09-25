package com.ivieleague.kotlin.server.model

class Read(
        scalars: Collection<Scalar> = listOf(),
        links: Map<Link, Read> = mapOf(),
        multilinks: Map<Multilink, Read> = mapOf()
) {
    var condition: Condition = Condition.Always
    var sort: List<Sort> = listOf()
    var startAfter: Instance? = null
    var count: Int = 100

    val scalars: HashSet<Scalar> = HashSet(scalars)
    val links: HashMap<Link, Read> = HashMap(links)
    val multilinks: HashMap<Multilink, Read> = HashMap(multilinks)

    companion object {
        val EMPTY = Read()
    }

    fun merge(other: Read) {
        for (scalar in other.scalars) {
            scalars += scalar
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
            val equalConditions = sort.subList(0, index + 1).dropLast(1).map { Condition.ScalarEqual(scalar = it.scalar, value = startAfter) }
            val currentSort = sort[index]
            val compareCondition = if (currentSort.ascending) {
                Condition.ScalarGreaterThan(scalar = currentSort.scalar, lower = startAfter.scalars[currentSort.scalar] as Comparable<Comparable<*>>)
            } else {
                Condition.ScalarLessThan(scalar = currentSort.scalar, upper = startAfter.scalars[currentSort.scalar] as Comparable<Comparable<*>>)
            }
            Condition.AllConditions(equalConditions + compareCondition)
        })
    }

    fun isEmpty(): Boolean = scalars.isEmpty() && links.isEmpty() && multilinks.isEmpty()
}