package com.ivieleague.kotlin.server.old

interface Fetcher<K, V> {
    operator fun get(key: K): V
}