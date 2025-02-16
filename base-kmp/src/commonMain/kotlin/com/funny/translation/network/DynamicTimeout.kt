package com.funny.translation.network

import kotlin.reflect.KClass

// 动态设置超时时间，包括
// CONNECT_TIMEOUT: 连接超时时间
// READ_TIMEOUT: 读取超时时间
// WRITE_TIMEOUT: 写入超时时间
// 默认为 -1，表示不设置，用默认值
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DynamicTimeout(
    val connectTimeout: Int = -1,
    val readTimeout: Int = -1,
    val writeTimeout: Int = -1,

    // 参数提取策略
    val timeoutParamExtractor: KClass<out ParamExtractor> = DefaultModelExtractor::class,
)