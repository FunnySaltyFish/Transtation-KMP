package com.funny.translation.helper

import android.util.Log as AndroidLog

actual object Logger {
    private const val TAG = "DefaultLog"
    // i, d, w, e, v
    actual fun d(msg: String) { AndroidLog.d(TAG, msg) }
    actual fun d(tag: String, msg: String) { AndroidLog.d(tag, msg) }
    actual fun d(tag: String, msg: String, throwable: Throwable) { AndroidLog.d(tag, msg, throwable) }

    actual fun i(msg: String) { AndroidLog.i(TAG, msg) }
    actual fun i(tag: String, msg: String) { AndroidLog.i(tag, msg) }
    actual fun i(tag: String, msg: String, throwable: Throwable) { AndroidLog.i(tag, msg, throwable) }

    actual fun w(msg: String) { AndroidLog.w(TAG, msg) }
    actual fun w(tag: String, msg: String) { AndroidLog.w(tag, msg) }
    actual fun w(tag: String, msg: String, throwable: Throwable) { AndroidLog.w(tag, msg, throwable) }

    actual fun e(msg: String) { AndroidLog.e(TAG, msg) }
    actual fun e(tag: String, msg: String) { AndroidLog.e(tag, msg) }
    actual fun e(tag: String, msg: String, throwable: Throwable) { AndroidLog.e(tag, msg, throwable) }

    actual fun v(msg: String) { AndroidLog.v(TAG, msg) }
    actual fun v(tag: String, msg: String) { AndroidLog.v(tag, msg) }
    actual fun v(tag: String, msg: String, throwable: Throwable) { AndroidLog.v(tag, msg, throwable) }

    // wtf
    actual fun wtf(msg: String) { AndroidLog.wtf(TAG, msg) }
    actual fun wtf(tag: String, msg: String) { AndroidLog.wtf(tag, msg) }
    actual fun wtf(tag: String, msg: String, throwable: Throwable) { AndroidLog.wtf(tag, msg, throwable) }
}