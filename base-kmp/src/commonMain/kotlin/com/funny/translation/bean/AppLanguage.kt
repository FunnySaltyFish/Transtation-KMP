package com.funny.translation.bean

import com.funny.translation.kmp.base.strings.ResStrings
import java.util.Locale

enum class AppLanguage(private val description: String) {
    FOLLOW_SYSTEM(ResStrings.follow_system),
    ENGLISH(ResStrings.language_english),
    CHINESE(ResStrings.language_chinese);

    fun toLocale(): Locale = when (this) {
        FOLLOW_SYSTEM -> Locale.getDefault()
        ENGLISH -> Locale.ENGLISH
        CHINESE -> Locale.CHINESE
    }

    override fun toString(): String {
        return description
    }

}