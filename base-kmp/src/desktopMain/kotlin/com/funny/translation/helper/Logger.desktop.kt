package com.funny.translation.helper

import io.github.oshai.kotlinlogging.KotlinLogging

actual object Log {
    private val logger = KotlinLogging.logger {}

    actual fun d(msg: Any?) = logger.debug { msg }
    actual fun d(tag: String, msg: Any?) = logger.debug { "$tag $msg" }
    actual fun d(tag: String, msg: Any?, throwable: Throwable) = logger.debug(throwable) { "$tag $msg" }

    actual fun i(msg: Any?) = logger.info { msg }
    actual fun i(tag: String, msg: Any?) = logger.info { "$tag $msg" }
    actual fun i(tag: String, msg: Any?, throwable: Throwable) = logger.info(throwable) { "$tag $msg" }

    actual fun e(msg: Any?) = logger.error { msg }
    actual fun e(tag: String, msg: Any?) = logger.error { "$tag $msg" }
    actual fun e(tag: String, msg: Any?, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

    actual fun w(msg: Any?) = logger.warn { msg }
    actual fun w(tag: String, msg: Any?) = logger.warn { "$tag $msg" }
    actual fun w(tag: String, msg: Any?, throwable: Throwable) = logger.warn(throwable) { "$tag $msg" }

    actual fun v(msg: Any?) = logger.trace { msg }
    actual fun v(tag: String, msg: Any?) = logger.trace { "$tag $msg" }
    actual fun v(tag: String, msg: Any?, throwable: Throwable) = logger.trace(throwable) { "$tag $msg" }

    actual fun wtf(msg: Any?) = logger.error { msg }
    actual fun wtf(tag: String, msg: Any?) = logger.error { "$tag $msg" }
    actual fun wtf(tag: String, msg: Any?, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

}