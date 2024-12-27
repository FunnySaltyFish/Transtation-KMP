package com.funny.translation.translate.task

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.asFlowByLines
import com.funny.translation.AppConfig
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.Log
import com.funny.translation.helper.displayMsg
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.CoreTranslationTask
import com.funny.translation.translate.LLMTransCost
import com.funny.translation.translate.LLMTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.StreamingTranslation
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.TranslationStage
import com.funny.translation.translate.allLanguages
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlin.reflect.KClass


const val MODEL_NAME_PREFIX = "model_"


class ModelTranslationTask(val model: Model): ServerTextTranslationTask() {
    override val engineCodeName: String
        get() = MODEL_NAME_PREFIX + model.chatBotId

    override val name: String = model.name
    override val supportLanguages: List<Language> = allLanguages
    override val languageMapping: Map<Language, String> get() = englishNamesMapping
    override val result = LLMTranslationResult()

    override val taskClass: KClass<out CoreTranslationTask>
        get() = this::class

    override suspend fun translate() {
        result.reset(sourceString, name)
        try {
            val curText = StringBuilder()
            var generatingDetail = false

            aiService.translateStream(
                text = sourceString,
                source = languageMapping[sourceLanguage] ?: "English",
                target = languageMapping[targetLanguage] ?: "Simplified Chinese",
                modelId = model.chatBotId,
                explain = AppConfig.sAITransExplain.value
            ).asFlowByLines().map {
                try {
                    JsonX.fromJson<StreamingTranslation>(it)
                } catch (e: SerializationException) {
                    Log.d(TAG, "JSON: $it")
                    throw TranslationException(e.displayMsg("JSON解析"))
                }
            }.collect {
                Log.d(TAG, it.toString())
                result.message = it.message
                result.stage = it.stage
                when (it.stage) {
                    TranslationStage.SELECTING_PROMPT -> {
                        result.basic = it.message
                    }
                    TranslationStage.PARTIAL_TRANSLATION -> {
                        curText.append(it.message)
                        if (!generatingDetail) {
                            val idx = curText.indexOf(SEP)
                            if (idx < 0) {
                                result.basic = curText.toString()
                                return@collect
                            }
                            // 找到了 <|sep|>，开始分割
                            result.basic = curText.substring(0, idx)
                            curText.delete(0, idx + SEP.length)
                            result.detailText = curText.toString()
                            generatingDetail = true
                        } else {
                            // 目前生成的是详细翻译
                            result.detailText = curText.toString()
                        }
                    }
                    TranslationStage.FINAL_EXTRA -> {
                        result.cost = JsonX.fromJson<LLMTransCost>(result.message)
                    }
                    TranslationStage.SELECTED_PROMPT -> {
                        result.smartTransType = it.message
                    }
                    else -> {}
                }
            }
        } catch (e: TranslationException) {
            e.printStackTrace()
            throw e
        }
    }

    override fun madeURL(): String {
        return "${ServiceCreator.BASE_URL}/api/translate_streaming"
    }

    companion object {
        private const val TAG = "ModelTranslationTask"
        val englishNamesMapping by lazy(LazyThreadSafetyMode.PUBLICATION) {
            Language.entries.associateWith { lang ->
                lang.displayText
            }
        }
        private const val SEP = "<|sep|>"
    }
}