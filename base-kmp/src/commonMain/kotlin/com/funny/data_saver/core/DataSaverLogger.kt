package com.funny.data_saver.core

import com.funny.translation.helper.Log

class DataSaverLogger(private val tag: String) {
    fun d(msg: String) {
        if (DataSaverConfig.DEBUG) Log.d(tag, msg)
    }
    fun w(msg: String) {
        if (DataSaverConfig.DEBUG) Log.w(tag, msg)
    }
    fun e(msg: String) {
        if (DataSaverConfig.DEBUG) Log.e(tag, msg)
    }

    companion object {
        private const val TAG = "ComposeDataSaver"
        private val logger by lazy(LazyThreadSafetyMode.PUBLICATION) {
            DataSaverLogger(TAG)
        }

        fun log(msg: String) = Companion::d
        fun d(msg: String) { logger.d(msg) }
        fun w(msg: String) { logger.w(msg) }
        fun e(msg: String) { logger.e(msg) }
    }
}