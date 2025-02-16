package com.funny.translation.translate

import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThinkingStage {
    IDLE, THINKING, FINISH
}

/**
 *
 * @property engineName String 翻译引擎
 * @property basic String 基本翻译结果，
 * @property sourceString String 源语言
 * @property detailText String? 详细翻译结果，Markdown形式
 * @property targetLanguage Language? 目标语言
 * @constructor
 */
@Keep
open class TranslationResult(
    var engineName: String = "",
    var sourceString: String = "",
    var targetLanguage: Language? = Language.AUTO
) {
    var basic: String by mutableStateOf("")
    var detailText: String? by mutableStateOf(null)
    var error: String by mutableStateOf("")
    var stage: TranslationStage by mutableStateOf(TranslationStage.IDLE)
    var message: String by mutableStateOf("")

    var think: String by mutableStateOf("")
    var thinkStage: ThinkingStage by mutableStateOf(ThinkingStage.IDLE)
    var thinkTime: Float by mutableStateOf(0f)

    var smartTransType: String? = null

    open fun setBasicResult(text: String) {
        basic = text
    }

    open fun reset(sourceString: String, name: String) {
        stage = TranslationStage.IDLE
        this.sourceString = sourceString
        engineName = name
        basic = ""
        think = ""
        thinkStage = ThinkingStage.IDLE
        thinkTime = 0f
        detailText = null
        smartTransType = null
    }

    fun updateThinkTime(deltaMills: Long) {
        thinkTime = (deltaMills / 1000f)
    }

    override fun toString(): String {
        return "TranslationResult(engineName='$engineName', basicResult=$basic, sourceString='$sourceString', detailText=$detailText, targetLanguage=$targetLanguage)"
    }

    companion object {
        private const val TAG = "TranslationResult"
    }
}
