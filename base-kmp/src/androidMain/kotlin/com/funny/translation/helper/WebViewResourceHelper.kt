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

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import android.text.TextUtils
import java.lang.reflect.Method

/**
 * @author RePlugin Team
 */
// https://github.com/Qihoo360/RePlugin
object WebViewResourceHelper {
    private var sInitialed = false
    fun addChromeResourceIfNeeded(context: Context): Boolean {
        if (sInitialed) {
            return true
        }
        val dir = getWebViewResourceDir(context)
        if (TextUtils.isEmpty(dir)) {
            return false
        }
        try {
            val m = addAssetPathMethod
            if (m != null) {
                val ret = m.invoke(context.assets, dir) as Int
                sInitialed = ret > 0
                return sInitialed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private val addAssetPathMethod: Method?
        private get() {
            var m: Method? = null
            val c: Class<*> = AssetManager::class.java
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    m = c.getDeclaredMethod("addAssetPathAsSharedLibrary", String::class.java)
                    m.isAccessible = true
                } catch (e: NoSuchMethodException) {
                    // Do Nothing
                    e.printStackTrace()
                }
                return m
            }
            try {
                m = c.getDeclaredMethod("addAssetPath", String::class.java)
                m.isAccessible = true
            } catch (e: NoSuchMethodException) {
                // Do Nothing
                e.printStackTrace()
            }
            return m
        }

    private fun getWebViewResourceDir(context: Context): String? {
        val pkgName = webViewPackageName
        if (TextUtils.isEmpty(pkgName)) {
            return null
        }
        try {
            val pi = context.packageManager.getPackageInfo(
                webViewPackageName!!, PackageManager.GET_SHARED_LIBRARY_FILES
            )
            return pi.applicationInfo.sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            // Do Nothing
        }
        return null
    }

    private val webViewPackageName: String?
        private get() {
            val sdk = Build.VERSION.SDK_INT
            return if (sdk <= 20) {
                null
            } else when (sdk) {
                21, 22 -> webViewPackageName4Lollipop
                23 -> webViewPackageName4M
                24 -> webViewPackageName4N
                25 -> webViewPackageName4More
                else -> webViewPackageName4More
            }
        }
    private val webViewPackageName4Lollipop: String?
        private get() {
            try {
                return ReflectUtil.invokeStaticMethod(
                    "android.webkit.WebViewFactory",
                    "getWebViewPackageName",
                    null
                ) as String
            } catch (e: Throwable) {
                //
            }
            return "com.google.android.webview"
        }
    private val webViewPackageName4M: String?
        private get() = webViewPackageName4Lollipop
    private val webViewPackageName4N: String
        private get() {
            try {
                val c = ReflectUtil.invokeStaticMethod(
                    "android.webkit.WebViewFactory",
                    "getWebViewContextAndSetProvider",
                    null
                ) as Context
                return c.applicationInfo.packageName
            } catch (e: Throwable) {
                //
            }
            return "com.google.android.webview"
        }
    private val webViewPackageName4More: String
        private get() = webViewPackageName4N
}