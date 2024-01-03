package com.funny.translation.helper

import com.funny.translation.bean.AppLanguage
import com.funny.translation.kmp.KMPContext
import java.util.Locale

actual object LocaleUtils {
    private const val KEY_LOCALE = "locale"
    private lateinit var appLanguage: AppLanguage

    actual fun init(context: KMPContext) {
        appLanguage = kotlin.runCatching {
            AppLanguage.entries[DataSaverUtils.readData(KEY_LOCALE, 0)]
        }.onFailure { it.printStackTrace() }.getOrDefault(AppLanguage.FOLLOW_SYSTEM)
    }

    actual fun getWarpedContext(
        context: KMPContext,
        locale: Locale
    ): KMPContext {
        return context
    }

    actual fun saveAppLanguage(appLanguage: AppLanguage) {
        this.appLanguage = appLanguage
        DataSaverUtils.saveData(KEY_LOCALE, appLanguage.ordinal)
    }

    actual fun getAppLanguage(): AppLanguage {
        return appLanguage
    }

    actual fun getLocaleDirectly(): Locale {
        return DataSaverUtils.readData(KEY_LOCALE, 0).let {
            when(it) {
                0 -> Locale.getDefault()
                1 -> Locale.ENGLISH
                2 -> Locale.CHINESE
                else -> Locale.getDefault()
            }
        }
    }

}