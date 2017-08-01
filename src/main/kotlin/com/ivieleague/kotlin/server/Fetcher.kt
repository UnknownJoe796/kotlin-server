package com.ivieleague.kotlin.server

interface Fetcher<K, V> {
    operator fun get(key: K): V
}