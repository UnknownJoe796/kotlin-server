package com.ivieleague.kotlin.server.model

sealed class Condition {

    abstract fun dependencies(modify: Read)
    abstract operator fun invoke(instance: Instance): Boolean
    abstract operator fun invoke(write: Write): Boolean


    object Always : Condition() {
        override fun dependencies(modify: Read) {}
        override fun invoke(instance: Instance): Boolean = true
        override fun invoke(write: Write): Boolean = true
    }

    object Never : Condition() {
        override fun dependencies(modify: Read) {}
        override fun invoke(instance: Instance): Boolean = false
        override fun invoke(write: Write): Boolean = false
    }


    data class AllConditions(val conditions: List<Condition>) : Condition() {
        override fun dependencies(modify: Read) {
            conditions.forEach {
                it.dependencies(modify)
            }
        }

        override fun invoke(instance: Instance): Boolean = conditions.all { it.invoke(instance) }
        override fun invoke(write: Write): Boolean = conditions.all { it.invoke(write) }
    }

    data class AnyConditions(val conditions: List<Condition>) : Condition() {
        override fun dependencies(modify: Read) {
            conditions.forEach {
                it.dependencies(modify)
            }
        }

        override fun invoke(instance: Instance): Boolean = conditions.any { it.invoke(instance) }
        override fun invoke(write: Write): Boolean = conditions.any { it.invoke(write) }
    }


    data class ScalarEqual(val path: List<Link> = listOf(), val scalar: Scalar, val equals: Any?) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, scalar, modify)
        override fun invoke(instance: Instance): Boolean = get(path, instance)?.scalars?.get(scalar) == equals
        override fun invoke(write: Write): Boolean = get(path, write)?.scalars?.get(scalar) == equals
    }

    data class ScalarNotEqual(val path: List<Link> = listOf(), val scalar: Scalar, val doesNotEqual: Any?) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, scalar, modify)
        override fun invoke(instance: Instance): Boolean = get(path, instance)?.scalars?.get(scalar) != doesNotEqual
        override fun invoke(write: Write): Boolean = get(path, write)?.scalars?.get(scalar) != doesNotEqual
    }

    data class ScalarBetween<T : Comparable<T>>(val path: List<Link> = listOf(), val scalar: Scalar, val lower: T, val upper: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, scalar, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(scalar) as T) in (lower..upper)
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(scalar) as T) in (lower..upper)
    }


    data class IdEquals(val path: List<Link> = listOf(), val equals: String) : Condition() {
        override fun dependencies(modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
        }

        override fun invoke(instance: Instance): Boolean = get(path, instance)?.id == equals
        override fun invoke(write: Write): Boolean = get(path, write)?.id == equals
    }

    data class MultilinkContains(val path: List<Link> = listOf(), val multilink: Multilink, val id: String) : Condition() {
        override fun dependencies(modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
            current.multilinks.getOrPut(multilink) { Read() }
        }

        override fun invoke(instance: Instance): Boolean = get(path, instance)?.multilinks?.get(multilink)?.any { it.id == id } ?: false
        override fun invoke(write: Write): Boolean = get(path, write)?.multilinks?.get(multilink)?.let {
            (it.additions?.any { it.id == id } ?: false) && (it.replacements?.any { it.id == id } ?: false)
        } ?: false
    }

    data class MultilinkDoesNotContain(val path: List<Link> = listOf(), val multilink: Multilink, val id: String) : Condition() {
        override fun dependencies(modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
            current.multilinks.getOrPut(multilink) { Read() }
        }

        override fun invoke(instance: Instance): Boolean = get(path, instance)?.multilinks?.get(multilink)?.none { it.id == id } ?: true
        override fun invoke(write: Write): Boolean = get(path, write)?.multilinks?.get(multilink)?.let {
            (it.additions?.none { it.id == id } ?: false) || (it.replacements?.none { it.id == id } ?: false)
        } ?: false
    }

    companion object {
        private fun get(path: List<Link>, instance: Instance): Instance? {
            var current = instance
            for (link in path) {
                current = current.links[link] ?: return null
            }
            return current
        }

        private fun get(path: List<Link>, write: Write): Write? {
            var current = write
            for (link in path) {
                current = current.links[link] ?: return null
            }
            return current
        }

        private fun getScalarDependencies(path: List<Link>, scalar: Scalar, modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
            current.scalars += scalar
        }
    }
}