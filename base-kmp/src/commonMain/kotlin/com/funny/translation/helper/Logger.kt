@file:Suppress("unused")
package com.funny.translation.helper

expect object Log {
    fun d(msg: Any?) : Unit
    fun d(tag: String, msg: Any?) : Unit
    fun d(tag: String, msg: Any?, throwable: Throwable) : Unit

    fun i(msg: Any?) : Unit
    fun i(tag: String, msg: Any?) : Unit
    fun i(tag: String, msg: Any?, throwable: Throwable) : Unit

    fun e(msg: Any?) : Unit
    fun e(tag: String, msg: Any?) : Unit
    fun e(tag: String, msg: Any?, throwable: Throwable) : Unit

    fun w(msg: Any?) : Unit
    fun w(tag: String, msg: Any?) : Unit
    fun w(tag: String, msg: Any?, throwable: Throwable) : Unit

    fun v(msg: Any?) : Unit
    fun v(tag: String, msg: Any?) : Unit
    fun v(tag: String, msg: Any?, throwable: Throwable) : Unit

    fun wtf(msg: Any?) : Unit
    fun wtf(tag: String, msg: Any?) : Unit
    fun wtf(tag: String, msg: Any?, throwable: Throwable) : Unit
}
