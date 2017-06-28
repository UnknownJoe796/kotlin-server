package com.ivieleague.kotlin.server.model

class Read(
        scalars: Collection<Scalar> = listOf(),
        links: Map<Link, Read> = mapOf(),
        multilinks: Map<Multilink, Read> = mapOf()
) {
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

    fun isEmpty(): Boolean = scalars.isEmpty() && links.isEmpty() && multilinks.isEmpty()
}