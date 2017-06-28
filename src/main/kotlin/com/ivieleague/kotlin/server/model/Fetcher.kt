package com.ivieleague.kotlin.server.model

interface Fetcher<K, V> {
    operator fun get(key: K): V
}