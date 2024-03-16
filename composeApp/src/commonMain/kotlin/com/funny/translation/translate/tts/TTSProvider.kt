package com.funny.translation.translate.tts

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LazyMutableState
import com.funny.translation.helper.get
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.ui.settings.Category
import kotlinx.collections.immutable.persistentListOf
import java.net.URLEncoder

abstract class TTSProvider {
    abstract fun getUrl(
        word: String,
        language: Language,
        voice: String,
        speed: Int = 100,
        volume: Int = 100
    ): String

    abstract suspend fun getSpeakers(gender: Gender, locale: String): List<Speaker>

    abstract val id: String
    abstract val name: String
    abstract val supportLanguages: Set<Language>

    var expanded by LazyMutableState {
        mutableDataSaverStateOf(DataSaverUtils, "TTSProvider_${id}_expanded", false)
    }

    @Composable
    abstract fun Settings()
}


object BaiduTransTTSProvider: TTSProvider() {
    override val id: String = "BaiduTrans"
    override val name: String = ResStrings.engine_baidu
    override val supportLanguages: Set<Language> = TextTranslationEngines.BaiduNormal.supportLanguages.toSet()

    override fun getUrl(word: String, language: Language, voice: String, speed: Int, volume: Int): String {
        return String.format(
            "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
            TextTranslationEngines.BaiduNormal.languageMapping[language] ?: "auto",
            URLEncoder.encode(word, "UTF-8")
        )
    }

    override suspend fun getSpeakers(gender: Gender, locale: String): List<Speaker> = DEFAULT_SPEAKER

    @Composable
    override fun Settings() {
        Category(
            title = ResStrings.speak_speed,
        ) {
            var speed by rememberDataSaverState("${id}_speed", initialValue = 3f)
            Slider(
                value = speed,
                onValueChange = { speed = it },
                valueRange = 1f..5f,
                steps = 5,
//                onValueChangeFinished = { speed = it }
            )
        }
    }

    internal val DEFAULT_SPEAKER by lazy {
        arrayListOf(
            Speaker(
                fullName = "Default",
                shortName = "Default",
                gender = Gender.Female,
                locale = "auto"
            )
        )
    }
}

object EdgeTTSProvider: ServerTTSProvider("edge") {
    // {'ru', 'ta', 'bn', 'lt', 'sw', 'lo', 'ur', 'da', 'zh', 'hi', 'az', 'so', 'su', 'mr', 'sv', 'th', 'bs', 'uk', 'sr', 'km', 'am', 'ka', 'ca', 'nl', 'tr', 'cy', 'ga', 'vi', 'bg', 'fi', 'he', 'hu', 'is', 'en', 'af', 'id', 'et', 'cs', 'mk', 'ms', 'uz', 'zu', 'gl', 'sk', 'my', 'ml', 'mt', 'fa', 'pl', 'ar', 'te', 'it', 'ja', 'gu', 'de', 'ps', 'si', 'ne', 'es', 'sl', 'ko', 'mn', 'jv', 'el', 'sq', 'fil', 'kk', 'lv', 'nb', 'hr', 'pt', 'kn', 'fr', 'ro'}
    private val localeMap = mapOf(
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
        Language.SPANISH to "es"
    )

    override val name: String = "Edge"
    override val supportLanguages: Set<Language> = localeMap.keys

    override fun languageToLocale(language: Language): String {
        return localeMap.get(language, "auto")
    }
}

object OpenAIProvider: ServerTTSProvider("openai") {
    override val name: String = "OpenAI"
    override val supportLanguages: Set<Language> = allLanguages.toSet()

    // OpenAI 智能判断语言
    override fun languageToLocale(language: Language): String = "all"
}

val ttsProviders: List<TTSProvider> = persistentListOf(
    BaiduTransTTSProvider,
    OpenAIProvider,
    EdgeTTSProvider
)

fun findTTSProviderById(id: String) = ttsProviders.find { it.id == id } ?: BaiduTransTTSProvider