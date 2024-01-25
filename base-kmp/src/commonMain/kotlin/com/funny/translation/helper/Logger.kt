@file:Suppress("unused")
package com.funny.translation.helper

expect object Logger {
    fun d(msg: String) : Unit
    fun d(tag: String, msg: String) : Unit
    fun d(tag: String, msg: String, throwable: Throwable) : Unit

    fun i(msg: String) : Unit
    fun i(tag: String, msg: String) : Unit
    fun i(tag: String, msg: String, throwable: Throwable) : Unit

    fun e(msg: String) : Unit
    fun e(tag: String, msg: String) : Unit
    fun e(tag: String, msg: String, throwable: Throwable) : Unit

    fun w(msg: String) : Unit
    fun w(tag: String, msg: String) : Unit
    fun w(tag: String, msg: String, throwable: Throwable) : Unit

    fun v(msg: String) : Unit
    fun v(tag: String, msg: String) : Unit
    fun v(tag: String, msg: String, throwable: Throwable) : Unit

    fun wtf(msg: String) : Unit
    fun wtf(tag: String, msg: String) : Unit
    fun wtf(tag: String, msg: String, throwable: Throwable) : Unit
}

typealias Log = Logger