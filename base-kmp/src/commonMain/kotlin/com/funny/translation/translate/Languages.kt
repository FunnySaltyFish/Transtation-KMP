package com.funny.translation.translate

import androidx.annotation.Keep
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.kmp.base.strings.ResStrings
import kotlinx.coroutines.flow.MutableStateFlow

@Keep
@kotlinx.serialization.Serializable
enum class Language(val id : Int,var displayText : String = "") {
    AUTO(0),
    CHINESE(1),
    ENGLISH(2),
    JAPANESE(3),
    KOREAN(4),
    FRENCH(5),
    RUSSIAN(6),
    GERMANY(7),
    WENYANWEN(8),
    THAI(9),
    PORTUGUESE(10),
    VIETNAMESE(11),
    ITALIAN(12),
    CHINESE_YUE(13), // 粤语
    // 西班牙语
    SPANISH(14),
    ;

    val selectedKey get() = this.name + "_selected"
    val imgSelectedKey get() = this.name + "_img_selected"

    companion object {
        fun fromId(id: String?) = id?.toIntOrNull()?.let { findLanguageById(it) }

    }

}

fun findLanguageById(id : Int, default: Language = Language.AUTO) = if(id in allLanguages.indices) {
    allLanguages[id]
} else {
    default
}


val allLanguages = Language.entries
val enabledLanguages = MutableStateFlow(allLanguages.filter { DataSaverUtils.readData(it.selectedKey, true) })

fun initLanguageDisplay(){
    Language.AUTO.displayText = ResStrings.language_auto
    Language.CHINESE.displayText = ResStrings.language_chinese
    Language.ENGLISH.displayText = ResStrings.language_english
    Language.JAPANESE.displayText = ResStrings.language_japanese
    Language.KOREAN.displayText = ResStrings.language_korean
    Language.FRENCH.displayText = ResStrings.language_french
    Language.RUSSIAN.displayText = ResStrings.language_russian
    Language.GERMANY.displayText = ResStrings.language_germany
    Language.WENYANWEN.displayText = ResStrings.language_wenyanwen
    Language.THAI.displayText = ResStrings.language_thai
    Language.PORTUGUESE.displayText = ResStrings.language_portuguese
    Language.VIETNAMESE.displayText = ResStrings.language_vietnamese
    Language.ITALIAN.displayText = ResStrings.language_italian
    Language.CHINESE_YUE.displayText = ResStrings.language_chinese_yue
    Language.SPANISH.displayText = ResStrings.language_spanish
}


