package com.funny.translation.helper

fun <K, V> Map<K, V>.get(key: K, default: V): V {
    return get(key) ?: default
}