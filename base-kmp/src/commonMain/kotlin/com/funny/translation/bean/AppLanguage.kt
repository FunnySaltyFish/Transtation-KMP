package com.funny.translation.bean

import com.funny.translation.kmp.base.strings.ResStrings
import java.util.Locale

enum class AppLanguage(val description: String) {
    FOLLOW_SYSTEM(ResStrings.follow_system),
    ENGLISH("English"),
    CHINESE("简体中文");

    fun toLocale(): Locale = when (this) {
        FOLLOW_SYSTEM -> Locale.getDefault()
        ENGLISH -> Locale.ENGLISH
        CHINESE -> Locale.CHINESE
    }

    override fun toString(): String {
        return description
    }

}