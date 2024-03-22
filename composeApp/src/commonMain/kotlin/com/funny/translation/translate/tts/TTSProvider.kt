package com.funny.translation.translate.tts

import androidx.compose.runtime.Composable
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.Price
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LazyMutableState
import com.funny.translation.helper.get
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.tts.ui.SpeedSettings
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
    abstract val defaultExtraConf: TTSExtraConf
    open val price1kChars: Price = Price.ZERO

    var expanded by LazyMutableState {
        mutableDataSaverStateOf(DataSaverUtils, "TTSProvider_${id}_expanded", false)
    }

    @Composable
    abstract fun Settings(conf: TTSConf, onSettingSpeedFinish: (Float) -> Unit)
}


object BaiduTransTTSProvider: TTSProvider() {
    private val languageMapping: Map<Language, String> by lazy {
        hashMapOf<Language, String>().apply {
            putAll(TextTranslationEngines.BaiduNormal.languageMapping)
            this[Language.CHINESE_YUE] = "cte"
            remove(Language.AUTO)
        }
    }

    override val id: String = "BaiduTrans"
    override val name: String = ResStrings.engine_baidu
    override val supportLanguages: Set<Language> = languageMapping.keys
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf(3, 100)

    override fun getUrl(word: String, language: Language, voice: String, speed: Int, volume: Int): String {
        return String.format(
            "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=%d&source=wise",
            languageMapping[language] ?: "auto",
            URLEncoder.encode(word, "UTF-8"),
            speed.coerceIn(1, 5)
        )
    }

    override suspend fun getSpeakers(gender: Gender, locale: String): List<Speaker> = DEFAULT_SPEAKERS

    @Composable
    override fun Settings(conf: TTSConf, onSettingSpeedFinish: (Float) -> Unit) {
        SpeedSettings(conf = conf, valueRange = 1f..5f, steps = 3, onFinish = onSettingSpeedFinish)
    }

    internal val DEFAULT_SPEAKERS by lazy {
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
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf()

    override fun languageToLocale(language: Language): String {
        return localeMap.get(language, "auto")
    }
}

object OpenAIProvider: ServerTTSProvider("openai") {
    override val name: String = "OpenAI"
    override val supportLanguages: Set<Language> = allLanguages.toSet()
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf(speed = 80)
    override val price1kChars = Price("0.03")


    // OpenAI 智能判断语言
    override fun languageToLocale(language: Language): String = "all"

    @Composable
    override fun Settings(conf: TTSConf, onSettingSpeedFinish: (Float) -> Unit) {
        SpeedSettings(conf = conf, valueRange = 50f..200f, steps = 12, onFinish = onSettingSpeedFinish)
    }
}

val ttsProviders: List<TTSProvider> = persistentListOf(
    BaiduTransTTSProvider,
    OpenAIProvider,
//    EdgeTTSProvider
)

fun findTTSProviderById(id: String) = ttsProviders.find { it.id == id } ?: BaiduTransTTSProvider