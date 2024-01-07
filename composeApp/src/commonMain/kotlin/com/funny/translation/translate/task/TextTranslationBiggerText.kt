package com.funny.translation.translate.task

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.utils.FunnyBiggerText

class TextTranslationBiggerText() :
    BasicTextTranslationTask(), TranslationEngine by TextTranslationEngines.BiggerText{

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val str = FunnyBiggerText.drawMiddleString(basicText)
        result.setBasicResult(str)
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
}