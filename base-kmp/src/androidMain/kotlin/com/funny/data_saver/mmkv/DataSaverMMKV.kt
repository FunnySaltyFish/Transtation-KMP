package com.funny.data_saver.mmkv

import android.os.Parcelable
import com.funny.data_saver.core.DataSaverInterface
import com.tencent.mmkv.MMKV

open class DataSaverMMKV(
    val kv: MMKV,
    senseExternalDataChange: Boolean = false
) : DataSaverInterface(senseExternalDataChange) {
    // MMKV doesn't support listener, so we manually notify the listener
    private fun notifyExternalDataChanged(key: String, value: Any?) {
        if (senseExternalDataChange) externalDataChangedFlow?.tryEmit(key to value)
    }

    override fun <T> saveData(key: String, data: T) {
        if (data == null){
            remove(key)
            notifyExternalDataChanged(key, null)
            return
        }
        with(kv) {
            when (data) {
                is Long -> encode(key, data)
                is Int -> encode(key, data)
                is String -> encode(key, data)
                is Boolean -> encode(key, data)
                is Float -> encode(key, data)
                is Double -> encode(key, data)
                is Parcelable -> encode(key, data)
                is ByteArray -> encode(key, data)
                else -> throw IllegalArgumentException("Unable to save $data, this type(${data!!::class.java}) cannot be saved to MMKV, call [registerTypeConverters] to support it.")
            }
            notifyExternalDataChanged(key, data)
        }
    }

    override fun <T> readData(key: String, default: T): T = with(kv) {
        val res: Any = when (default) {
            is Long -> decodeLong(key, default)
            is Int -> decodeInt(key, default)
            is String -> decodeString(key, default)!!
            is Boolean -> decodeBool(key, default)
            is Float -> decodeFloat(key, default)
            is Double -> decodeDouble(key, default)
            is Parcelable -> decodeParcelable(key, (default as Parcelable)::class.java) ?: default
            is ByteArray -> decodeBytes(key, default)!!
            else -> throw IllegalArgumentException("Unable to read $default, this type(${default!!::class.java}) cannot be read from MMKV, call [registerTypeConverters] to support it.")
        }
        return@with res as T
    }

    override fun remove(key: String) {
        kv.removeValueForKey(key)
        notifyExternalDataChanged(key, null)
    }

    override fun contains(key: String) = kv.containsKey(key)
}

val DefaultDataSaverMMKV by lazy(LazyThreadSafetyMode.PUBLICATION) {
    DataSaverMMKV(MMKV.defaultMMKV())
}