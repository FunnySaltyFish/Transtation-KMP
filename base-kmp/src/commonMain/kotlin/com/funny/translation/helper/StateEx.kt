package com.funny.translation.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.StateFactoryMarker
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * equals to remember { mutableXxxStateOf(value) }
 * @param value T
 * @return MutableState<out Any?>
 */
@Composable
inline fun <reified T> rememberStateOf(value: T): MutableState<T> = remember {
    when (value) {
        is Int -> mutableIntStateOf(value)
        is Float -> mutableFloatStateOf(value)
        is Double -> mutableDoubleStateOf(value)
        is Long -> mutableLongStateOf(value)
        else -> mutableStateOf(value)
    } as MutableState<T>
}

@Composable
inline fun <reified T : Any> rememberSaveableStateOf(
    value: T,
    saver: Saver<T, out Any> = autoSaver(),
    key: String? = null,
) = rememberSaveable(stateSaver = saver, key = key) {
    when (value) {
        is Int -> mutableIntStateOf(value)
        is Float -> mutableFloatStateOf(value)
        is Double -> mutableDoubleStateOf(value)
        is Long -> mutableLongStateOf(value)
        else -> mutableStateOf(value)
    } as MutableState<T>
}

@StateFactoryMarker
@Composable
fun <T> rememberDerivedStateOf(calculation: () -> T) = remember {
    derivedStateOf(calculation)
}

@Composable
fun <T> rememberUpdatedMutableState(newValue: T): MutableState<T> = remember {
    mutableStateOf(newValue)
}.apply { value = newValue }


class LazyMutableState<T>(
    private val stateProvider: () -> MutableState<T>
): ReadWriteProperty<Any?, T> {
    private val lazyState by lazy { stateProvider() }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return lazyState.value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        lazyState.value = value
    }
}