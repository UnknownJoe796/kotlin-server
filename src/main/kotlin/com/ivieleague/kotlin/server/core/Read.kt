package com.ivieleague.kotlin.server.core

class Read(
        val scalars: Collection<Scalar> = listOf(),
        val links: Map<Link, Read> = mapOf(),
        val multilinks: Map<Multilink, Read> = mapOf()
) {
    companion object {
        val EMPTY = Read()
    }
}

//class MutableRead(
//        val scalars: HashSet<Scalar> = HashSet(),
//        val links: HashMap<Link, MutableRead> = HashMap(),
//        val multilinks: HashMap<Multilink, MutableRead> = HashMap()
//){
//    fun merge(other: MutableRead){
//        for(scalar in other.scalars){
//            scalars += scalar
//        }
//        for((link, read) in other.links){
//            val existing = links[link]
//            if(existing == null) links[link] = read
//            else existing.merge(read)
//        }
//        for((multilink, read) in other.multilinks){
//            val existing = multilinks[multilink]
//            if(existing == null) multilinks[multilink] = read
//            else existing.merge(read)
//        }
//    }
//}