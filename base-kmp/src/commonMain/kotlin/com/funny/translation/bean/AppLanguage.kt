package com.funny.translation.bean

import com.funny.translation.core.R
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.base.strings.ResStrings
import java.util.*

enum class AppLanguage(private val descriptionId: Int) {
    FOLLOW_SYSTEM(R.string.follow_system),
    ENGLISH(R.string.language_english),
    CHINESE(R.string.language_chinese);

    val description = appCtx.getString(descriptionId)

    fun toLocale(): Locale = when (this) {
        FOLLOW_SYSTEM -> Locale.getDefault()
        ENGLISH -> Locale.ENGLISH
        CHINESE -> Locale.CHINESE
    }

    override fun toString(): String {
        ResStrings.my_string
        return description
    }

}