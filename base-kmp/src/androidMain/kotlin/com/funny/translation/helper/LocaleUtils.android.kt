package com.funny.translation.helper

import android.content.SharedPreferences
import com.funny.translation.Consts
import com.funny.translation.bean.AppLanguage
import java.util.Locale

actual object LocaleUtils {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appLanguage: AppLanguage

    actual fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("shared_pref", android.content.Context.MODE_PRIVATE)
    }

    actual fun getWarpedContext(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        Locale.setDefault(locale)
        Log.d("LocaleUtils", "getWarpedContext: ${locale.language}")
        return context.createConfigurationContext(configuration)
    }

    actual fun saveAppLanguage(appLanguage: AppLanguage) {
        this.appLanguage = appLanguage
        sharedPreferences.edit().putInt(Consts.KEY_APP_LANGUAGE, appLanguage.ordinal).apply()

        Log.d("LocaleUtils", "saveAppLanguage: ${appLanguage.description}")
    }

    actual fun getAppLanguage(): AppLanguage {
        if (!this::appLanguage.isInitialized) {
            appLanguage = kotlin.runCatching {
                AppLanguage.values()[sharedPreferences.getInt(Consts.KEY_APP_LANGUAGE, 0)]
            }.onFailure { it.printStackTrace() }.getOrDefault(AppLanguage.FOLLOW_SYSTEM)
        }
        Log.d("LocaleUtils", "getAppLanguage: ${appLanguage.description}")
        return this.appLanguage
    }

    // 不经过获取 AppLanguage -> Locale 的过程，直接获取 Locale
    // 这个方法会在 attachBaseContext() 里调用，所以不能使用 AppLanguage 这个类
    actual fun getLocaleDirectly(): Locale {
        val id = sharedPreferences.getInt(Consts.KEY_APP_LANGUAGE, 0)
        return when(id) {
            0 -> Locale.getDefault()
            1 -> Locale.ENGLISH
            2 -> Locale.CHINESE
            else -> Locale.getDefault()
        }
    }
}
