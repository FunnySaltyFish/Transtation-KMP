package com.funny.translation.translate.task

import com.funny.compose.ai.bean.Model
import com.funny.translation.AppConfig
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.CoreTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.allLanguages
import kotlin.reflect.KClass


const val MODEL_NAME_PREFIX = "model_"

class ModelTranslationTask(val model: Model): ServerTextTranslationTask() {
    override val engineCodeName: String
        get() = MODEL_NAME_PREFIX + model.chatBotId

    override val name: String = model.name
    override val supportLanguages: List<Language> = allLanguages
    override val languageMapping: Map<Language, String> get() = englishNamesMapping

    override var selected: Boolean = false

    override val taskClass: KClass<out CoreTranslationTask>
        get() = this::class

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        val from = languageMapping[sourceLanguage] ?: "(automatic)"
        val to = languageMapping[targetLanguage] ?: "Chinese"
        val params = hashMapOf(
            "source" to from,
            "target" to to,
            "text" to sourceString,
            "engine" to engineCodeName,
            "explain" to AppConfig.sAITransExplain.value.toString()
        )
        return OkHttpUtils.get(url, params = params, timeout = intArrayOf(30, 600, 15))
    }

    companion object {
        private const val TAG = "ModelTranslationTask"
        val englishNamesMapping by lazy(LazyThreadSafetyMode.PUBLICATION) {
            Language.entries.associateWith { lang ->
                lang.displayText
            }
        }
    }
}