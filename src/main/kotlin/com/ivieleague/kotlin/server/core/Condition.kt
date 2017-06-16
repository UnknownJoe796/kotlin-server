package com.ivieleague.kotlin.server.core

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

    data class AllCondition(val conditions: List<Condition>) : Condition() {
        override fun dependencies(modify: Read) {
            conditions.forEach {
                it.dependencies(modify)
            }
        }

        override fun invoke(instance: Instance): Boolean = conditions.all { it.invoke(instance) }
        override fun invoke(write: Write): Boolean = conditions.all { it.invoke(write) }
    }

    data class AnyCondition(val conditions: List<Condition>) : Condition() {
        override fun dependencies(modify: Read) {
            conditions.forEach {
                it.dependencies(modify)
            }
        }

        override fun invoke(instance: Instance): Boolean = conditions.any { it.invoke(instance) }
        override fun invoke(write: Write): Boolean = conditions.any { it.invoke(write) }
    }

    data class ScalarEqual(val scalar: Scalar, val equals: Any?) : Condition() {
        override fun dependencies(modify: Read) {
            modify.scalars += scalar
        }

        override fun invoke(instance: Instance): Boolean = instance.scalars[scalar] == equals
        override fun invoke(write: Write): Boolean = write.scalars[scalar] == equals
    }

    data class ScalarNotEqual(val scalar: Scalar, val doesNotEqual: Any?) : Condition() {
        override fun dependencies(modify: Read) {
            modify.scalars += scalar
        }

        override fun invoke(instance: Instance): Boolean = instance.scalars[scalar] != doesNotEqual
        override fun invoke(write: Write): Boolean = write.scalars[scalar] != doesNotEqual
    }
}