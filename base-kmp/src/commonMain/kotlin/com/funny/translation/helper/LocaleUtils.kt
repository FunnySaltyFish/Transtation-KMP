package com.funny.translation.helper

import com.funny.translation.bean.AppLanguage
import com.funny.translation.kmp.KMPContext
import java.util.Locale

expect object LocaleUtils {
    
    fun init(context: KMPContext) 

    fun getWarpedContext(context: KMPContext, locale: Locale): KMPContext

    fun saveAppLanguage(appLanguage: AppLanguage) 

    fun getAppLanguage(): AppLanguage

    // 不经过获取 AppLanguage -> Locale 的过程，直接获取 Locale
    // 这个方法会在 attachBaseContext() 里调用，所以不能使用 AppLanguage 这个类
    fun getLocaleDirectly(): Locale
}