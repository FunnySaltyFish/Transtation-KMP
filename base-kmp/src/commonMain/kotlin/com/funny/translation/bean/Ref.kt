package com.funny.translation.bean

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun <T : Any> rememberSaveableRef(
    initialValue: T
): Ref<T> {
    return rememberSaveable(
        saver = Saver(save = { it.value }, restore = { Ref(it) })
    ) { Ref(initialValue) }
}