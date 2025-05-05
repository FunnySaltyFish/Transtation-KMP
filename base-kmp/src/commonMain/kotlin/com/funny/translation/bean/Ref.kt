package com.funny.translation.bean

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Ref<T>(var value: T): ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

@Composable
fun <T> rememberRef(
    initialValue: T
): Ref<T> {
    return remember { Ref(initialValue) }
}