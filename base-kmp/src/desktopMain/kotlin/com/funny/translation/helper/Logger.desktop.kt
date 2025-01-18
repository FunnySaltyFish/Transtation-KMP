package com.funny.translation.helper

import com.funny.translation.BuildConfig
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker

actual object Logger {
    private val logDir = CacheManager.baseDir.resolve("logs")
    private val fileWriter by lazy {
        logDir.mkdirs()
        logDir.resolve("log_${System.currentTimeMillis()}.log").writer()
    }

    // KotlinLogging.logger {} 在控制台看不到输出啊，只好改成最朴素的 System.out.println 了
    private val logger = object : KLogger {
        override val name: String = "TranstationLog"

        override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
            KLoggingEventBuilder().apply(block).run {
                if (BuildConfig.DEBUG && level >= Level.DEBUG) {
                    val line = "[$level] $message"
                    println(level.color + line + "\u001B[0m")
                    fileWriter.write(line + "\n")
                    fileWriter.flush()
                } else if (!BuildConfig.DEBUG){
                    // release 版本只输出 WARN 级别以上的日志
                    if (level >= Level.WARN) {
                        val line = "[$level] $message"
                        fileWriter.write(line + "\n")
                        fileWriter.flush()
                    }
                }
            }
        }

        override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
            return true
        }
    }

    private val Level.color get() = when (this) {
        Level.DEBUG -> "\u001B[36m"
        Level.INFO -> "\u001B[32m"
        Level.WARN -> "\u001B[33m"
        Level.ERROR -> "\u001B[31m"
        else -> ""
    }

    init {
        // kotlin-logging-to-jul
//        System.setProperty("kotlin-logging-to-jul", "true")
//        if (BuildConfig.DEBUG) {
//            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug")
//            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
//            System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss:SSS")
//            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_THREAD_NAME_KEY, "false")
//            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")
//            // 同时输出到控制台
//        } else {
//            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "warn")
//        }
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