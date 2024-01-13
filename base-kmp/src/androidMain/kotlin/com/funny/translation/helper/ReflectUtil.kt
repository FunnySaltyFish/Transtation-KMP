/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.funny.translation.helper

import java.lang.reflect.Field

/**
 * @author RePlugin Team
 */
// https://github.com/Qihoo360/RePlugin
object ReflectUtil {
    private const val TAG = "ReflectUtil"
    fun invokeStaticMethod(
        clzName: String?,
        methodName: String?,
        methodParamTypes: Array<Class<*>?>?,
        vararg methodParamValues: Any?
    ): Any? {
        try {
            val clz = Class.forName(clzName)
            if (clz != null && methodParamTypes != null) {
                val med = clz.getDeclaredMethod(methodName, *methodParamTypes)
                if (med != null) {
                    med.isAccessible = true
                    return med.invoke(null, *methodParamValues)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun invokeMethod(
        clzName: String?,
        methodName: String?,
        methodReceiver: Any?,
        methodParamTypes: Array<Class<*>?>,
        vararg methodParamValues: Any?
    ): Any? {
        try {
            if (methodReceiver == null) {
                return null
            }
            val clz = Class.forName(clzName)
            if (clz != null) {
                val med = clz.getDeclaredMethod(methodName, *methodParamTypes)
                if (med != null) {
                    med.isAccessible = true
                    return med.invoke(methodReceiver, *methodParamValues)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getStaticField(clzName: String?, filedName: String?): Any? {
        try {
            var field: Field? = null
            val clz = Class.forName(clzName)
            if (clz != null) {
                field = clz.getField(filedName)
                if (field != null) {
                    return field[""]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getField(clzName: String?, obj: Any?, filedName: String?): Any? {
        try {
            if (obj == null) {
                return null
            }
            val clz = Class.forName(clzName)
            if (clz != null) {
                val field = clz.getField(filedName)
                if (field != null) {
                    return field[obj]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}