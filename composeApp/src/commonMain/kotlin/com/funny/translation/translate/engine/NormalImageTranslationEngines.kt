package com.funny.translation.translate.engine

import com.funny.compose.ai.bean.Model
import com.funny.translation.helper.SimpleAction
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.NormalImageTranslationTask
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.task.ImageTranslationBaidu
import com.funny.translation.translate.task.ImageTranslationTencent
import com.funny.translation.translate.task.ModelImageTranslationTask
import com.funny.translation.translate.task.modelLanguageMapping
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


interface ImageTranslationEngine: TranslationEngine {
    override val taskClass: KClass<out ImageTranslationTask>
    fun getPoint(): Float
}

sealed class NormalImageTranslationEngines(
    override val taskClass: KClass<out NormalImageTranslationTask>
): ImageTranslationEngine {
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    fun createTask(
        sourceImage: ByteArray,
        sourceLanguage: Language,
        targetLanguage: Language
    ) : ImageTranslationTask {
        val instance = taskClass.createInstance()
        instance.sourceImg = sourceImage
        instance.sourceLanguage = sourceLanguage
        instance.targetLanguage = targetLanguage
        return instance
    }

    object Baidu: NormalImageTranslationEngines(
        ImageTranslationBaidu::class
    ) {
        override val name: String = ResStrings.engine_baidu
        override val supportLanguages: List<Language> = TextTranslationEngines.BaiduNormal.supportLanguages
        override val languageMapping: Map<Language, String> = TextTranslationEngines.BaiduNormal.languageMapping

        override fun getPoint() = 1.0f
    }


    object Tencent: NormalImageTranslationEngines(
        ImageTranslationTencent::class
    ) {
        override val name: String = ResStrings.engine_tencent
        override val languageMapping: Map<Language, String> = hashMapOf(
            Language.AUTO to "auto",
            Language.CHINESE to "zh",
            Language.ENGLISH to "en",
            Language.JAPANESE to "ja",
            Language.KOREAN to "ko",
            Language.FRENCH to "fr",
            Language.RUSSIAN to "ru",
            Language.GERMANY to "de",
            Language.THAI to "th",
            Language.PORTUGUESE to "pt",
            Language.VIETNAMESE to "vi",
            Language.ITALIAN to "it",
        )
        override val supportLanguages: List<Language> = languageMapping.keys.toList()

        override fun getPoint() = 1.0f
    }
}

class ModelImageTranslationEngine(
    val model: Model
): ImageTranslationEngine {
    override val name: String = model.name
    override val taskClass: KClass<out ImageTranslationTask> = ModelImageTranslationTask::class
    override val supportLanguages: List<Language> = allLanguages
    override val languageMapping: Map<Language, String> = modelLanguageMapping

    override fun getPoint() = 1.0f

    fun createTask(
        imageUri: String,
        sourceLanguage: Language,
        targetLanguage: Language,
        coroutineScope: CoroutineScope,
        onFinish: SimpleAction
    ): ImageTranslationTask {
        return ModelImageTranslationTask(
            model = model,
            fileUri = imageUri,
            systemPrompt = PROMPT_TEMPLATE.format(targetLanguage.displayText),
            coroutineScope = coroutineScope,
            onFinish = onFinish
        )
    }

    companion object {
        private val PROMPT_TEMPLATE = """
            You're an excellent translator, translate the image to %s. Your output should be clear and concise, and in Markdown format. I'll display your output over the image to help people to read. Output only the result directly, do not wrap with ```markdown```
        """.trim()
    }
}