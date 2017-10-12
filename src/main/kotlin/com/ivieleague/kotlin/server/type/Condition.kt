package com.ivieleague.kotlin.server.type

import com.ivieleague.kotlin.server.model.Read
import com.ivieleague.kotlin.server.model.Write

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


    data class ScalarEqual(val path: List<Link> = listOf(), val primitive: Primitive, val value: Any?) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = get(path, instance)?.scalars?.get(primitive) == value
        override fun invoke(write: Write): Boolean = get(path, write)?.scalars?.get(primitive) == value
    }

    data class ScalarNotEqual(val path: List<Link> = listOf(), val primitive: Primitive, val value: Any?) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = get(path, instance)?.scalars?.get(primitive) != value
        override fun invoke(write: Write): Boolean = get(path, write)?.scalars?.get(primitive) != value
    }

    @Suppress("UNCHECKED_CAST")
    data class ScalarLessThanOrEqual<T : Comparable<T>>(val path: List<Link> = listOf(), val primitive: Primitive, val upper: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(primitive) as T) <= upper
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(primitive) as T) <= upper
    }

    @Suppress("UNCHECKED_CAST")
    data class ScalarGreaterThanOrEqual<T : Comparable<T>>(val path: List<Link> = listOf(), val primitive: Primitive, val lower: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(primitive) as T) >= lower
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(primitive) as T) >= lower
    }

    @Suppress("UNCHECKED_CAST")
    data class ScalarLessThan<T : Comparable<T>>(val path: List<Link> = listOf(), val primitive: Primitive, val upper: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(primitive) as T) < upper
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(primitive) as T) < upper
    }

    @Suppress("UNCHECKED_CAST")
    data class ScalarGreaterThan<T : Comparable<T>>(val path: List<Link> = listOf(), val primitive: Primitive, val lower: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(primitive) as T) > lower
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(primitive) as T) > lower
    }

    @Suppress("UNCHECKED_CAST")
    data class ScalarBetween<T : Comparable<T>>(val path: List<Link> = listOf(), val primitive: Primitive, val lower: T, val upper: T) : Condition() {
        override fun dependencies(modify: Read) = getScalarDependencies(path, primitive, modify)
        override fun invoke(instance: Instance): Boolean = (get(path, instance)?.scalars?.get(primitive) as T) in (lower..upper)
        override fun invoke(write: Write): Boolean = (get(path, write)?.scalars?.get(primitive) as T) in (lower..upper)
    }


    data class IdEquals(val path: List<Link> = listOf(), val id: String) : Condition() {
        override fun dependencies(modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
        }

        override fun invoke(instance: Instance): Boolean = get(path, instance)?.id == id
        override fun invoke(write: Write): Boolean = get(path, write)?.id == id
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

        private fun getScalarDependencies(path: List<Link>, primitive: Primitive, modify: Read) {
            var current = modify
            for (link in path) {
                current = current.links.getOrPut(link) { Read() }
            }
            current.primitives += primitive
        }
    }
}