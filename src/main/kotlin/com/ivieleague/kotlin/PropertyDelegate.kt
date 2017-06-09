package com.ivieleague.kotlin

import kotlin.reflect.KProperty

interface PropertyDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

class SimplePropertyDelegate<T>(start: T) : PropertyDelegate<T> {
    var value: T = start
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}