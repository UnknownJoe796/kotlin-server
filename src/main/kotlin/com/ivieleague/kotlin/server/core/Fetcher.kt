package com.ivieleague.kotlin.server.core

interface Fetcher<K, V> {
    operator fun get(key: K): V
}