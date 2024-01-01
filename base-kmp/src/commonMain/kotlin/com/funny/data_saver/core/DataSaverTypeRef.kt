package com.funny.data_saver.core

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// 自定义的类，用来包装泛型
abstract class DataSaverTypeRef<T>

inline fun <reified T> getGenericType(): Type =
    object :
        DataSaverTypeRef<T>() {}::class.java
        .genericSuperclass
        .let { it as ParameterizedType }
        .actualTypeArguments
        .first()

