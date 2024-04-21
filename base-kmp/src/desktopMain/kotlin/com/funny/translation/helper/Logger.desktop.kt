package com.funny.translation.helper

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker

actual object Logger {
    private val logDir = CacheManager.baseDir.resolve("logs")

    init {
        // kotlin-logging-to-jul
//        System.setProperty("kotlin-logging-to-jul", "true")
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG")
        System.setProperty("org.slf4j.simpleLogger.log.TranstationLog", "DEBUG")
        // 输出到文件
        System.setProperty("org.slf4j.simpleLogger.logFile", logDir.resolve("logs/log_${System.currentTimeMillis()}.out").toString())
    }

    // KotlinLogging.logger {} 在控制台看不到输出啊，只好改成最朴素的 System.out.println 了
    private val logger = object :KLogger {

        private val fileOutputStream by lazy {
            logDir.mkdirs()
            logDir.resolve("log_${System.currentTimeMillis()}.log").outputStream().writer()
        }

        override val name: String = "TranstationLog"

        override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
            KLoggingEventBuilder().apply(block).run {
                println("[$level] $message")
                fileOutputStream.write("[$level] $message\n")
                fileOutputStream.flush()
            }
        }

        override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
            return true
        }
    }

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