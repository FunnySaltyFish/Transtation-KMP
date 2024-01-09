package com.funny.translation.helper

import io.github.oshai.kotlinlogging.KotlinLogging

actual object Log {
    init {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG")
    }

    private val logger = KotlinLogging.logger {}

    actual fun d(msg: String) = logger.debug { msg }
    actual fun d(tag: String, msg: String) = logger.debug { "$tag $msg" }
    actual fun d(tag: String, msg: String, throwable: Throwable) = logger.debug(throwable) { "$tag $msg" }

    actual fun i(msg: String) = logger.info { msg }
    actual fun i(tag: String, msg: String) = logger.info { "$tag $msg" }
    actual fun i(tag: String, msg: String, throwable: Throwable) = logger.info(throwable) { "$tag $msg" }

    actual fun e(msg: String) = logger.error { msg }
    actual fun e(tag: String, msg: String) = logger.error { "$tag $msg" }
    actual fun e(tag: String, msg: String, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

    actual fun w(msg: String) = logger.warn { msg }
    actual fun w(tag: String, msg: String) = logger.warn { "$tag $msg" }
    actual fun w(tag: String, msg: String, throwable: Throwable) = logger.warn(throwable) { "$tag $msg" }

    actual fun v(msg: String) = logger.trace { msg }
    actual fun v(tag: String, msg: String) = logger.trace { "$tag $msg" }
    actual fun v(tag: String, msg: String, throwable: Throwable) = logger.trace(throwable) { "$tag $msg" }

    actual fun wtf(msg: String) = logger.error { msg }
    actual fun wtf(tag: String, msg: String) = logger.error { "$tag $msg" }
    actual fun wtf(tag: String, msg: String, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

}