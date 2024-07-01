package com.funny.translation.translate.tts

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.Price
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LazyMutableState
import com.funny.translation.helper.get
import com.funny.translation.kmp.currentPlatform
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.tts.ui.SpeedSettings
import com.funny.translation.translate.tts.ui.VolumeSettings
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

    /**
     * 语言到 Locale 字段的映射
     */
    abstract fun languageToLocale(language: Language): String

    abstract val id: String
    abstract val name: String
    abstract val languageMapping: Map<Language, String>
    open val supportLanguages: Set<Language> get() = languageMapping.keys
    abstract val defaultExtraConf: TTSExtraConf
    open val price1kChars: Price = Price.ZERO

    var expanded by LazyMutableState {
        mutableDataSaverStateOf(DataSaverUtils, "TTSProvider_${id}_expanded", true)
    }

    var savedExtraConf by LazyMutableState {
        mutableDataSaverStateOf(DataSaverUtils, "TTSProvider_${id}_extraConf", defaultExtraConf)
    }

    @Composable
    abstract fun ColumnScope.Settings(
        conf: TTSConf,
        onSettingSpeedFinish: (Float) -> Unit,
        onSettingVolumeFinish: (Float) -> Unit
    )
}


object BaiduTransTTSProvider: TTSProvider() {
    override val languageMapping: Map<Language, String> by lazy {
        hashMapOf<Language, String>().apply {
            putAll(TextTranslationEngines.BaiduNormal.languageMapping)
            this[Language.CHINESE_YUE] = "cte"
            remove(Language.AUTO)
        }
    }

    override val id: String = "BaiduTrans"
    override val name: String = ResStrings.engine_baidu
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf(3, 100)

    override fun getUrl(word: String, language: Language, voice: String, speed: Int, volume: Int): String {
        return String.format(
            "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=%d&source=wise",
            languageMapping[language] ?: "auto",
            URLEncoder.encode(word, "UTF-8"),
            speed.coerceIn(1, 5)
        )
    }

    override fun languageToLocale(language: Language): String {
        return "all"
    }

    override suspend fun getSpeakers(gender: Gender, locale: String): List<Speaker> = arrayListOf(defaultSpeaker)

    @Composable
    override fun ColumnScope.Settings(
        conf: TTSConf,
        onSettingSpeedFinish: (Float) -> Unit,
        onSettingVolumeFinish: (Float) -> Unit
    ) {
        SpeedSettings(conf = conf, valueRange = 1f..5f, steps = 3, onFinish = onSettingSpeedFinish)
    }

    internal val defaultSpeaker by lazy {
        Speaker(
            fullName = "Default",
            shortName = ResStrings.default_str,
            gender = Gender.Female,
            locale = "auto"
        )
    }
}

object EdgeTTSProvider: ServerTTSProvider("edge") {
    // {'ru', 'ta', 'bn', 'lt', 'sw', 'lo', 'ur', 'da', 'zh', 'hi', 'az', 'so', 'su', 'mr', 'sv', 'th', 'bs', 'uk', 'sr', 'km', 'am', 'ka', 'ca', 'nl', 'tr', 'cy', 'ga', 'vi', 'bg', 'fi', 'he', 'hu', 'is', 'en', 'af', 'id', 'et', 'cs', 'mk', 'ms', 'uz', 'zu', 'gl', 'sk', 'my', 'ml', 'mt', 'fa', 'pl', 'ar', 'te', 'it', 'ja', 'gu', 'de', 'ps', 'si', 'ne', 'es', 'sl', 'ko', 'mn', 'jv', 'el', 'sq', 'fil', 'kk', 'lv', 'nb', 'hr', 'pt', 'kn', 'fr', 'ro'}
    override val languageMapping by lazy {
        mapOf(
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
    }

    override val name: String = "Edge"
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf()

    override fun languageToLocale(language: Language): String {
        return languageMapping.get(language, "auto")
    }
}

object OpenAIProvider: ServerTTSProvider("openai") {
    override val languageMapping: Map<Language, String> = emptyMap()
    override val name: String = "OpenAI"
    override val supportLanguages: Set<Language> = allLanguages.toSet()
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf(speed = 80)
    override val price1kChars = Price("0.03")


    // OpenAI 智能判断语言
    override fun languageToLocale(language: Language): String = "all"

    @Composable
    override fun ColumnScope.Settings(
        conf: TTSConf,
        onSettingSpeedFinish: (Float) -> Unit,
        onSettingVolumeFinish: (Float) -> Unit
    ) {
        SpeedSettings(conf = conf, valueRange = 50f..200f, steps = 12, onFinish = onSettingSpeedFinish)
    }

    override fun getUrl(
        word: String,
        language: Language,
        voice: String,
        speed: Int,
        volume: Int
    ): String {
        val url = super.getUrl(word, language, voice, speed, volume)
        // 由于 Desktop 使用的播放器不支持默认的 opus 格式，转为使用 mp3 格式响应
        return if (currentPlatform.isDesktop) "$url&response_format=mp3" else url
    }

    val defaultSpeaker = Speaker(
        fullName = "nova",
        shortName = "nova",
        gender = Gender.Female,
        locale = "all"
    )
}

object SambertProvider: ServerTTSProvider("sambert") {
    // {'美式英文', '印尼语', '法语', '英文', '德语', '中文', '西班牙语', '泰语', '意大利语'}
    override val price1kChars = Price("0.03")
    override val languageMapping by lazy {
        mapOf(
            Language.CHINESE to "中文",
            Language.ENGLISH to "美式英文",
            Language.FRENCH to "法语",
            Language.GERMANY to "德语",
            Language.SPANISH to "西班牙语",
            Language.ITALIAN to "意大利语",
            Language.THAI to "泰语",
        )
    }

    override fun languageToLocale(language: Language): String {
        return languageMapping.get(language, "英文")
    }

    override fun getUrl(
        word: String,
        language: Language,
        voice: String,
        speed: Int,
        volume: Int
    ): String {
        val url = super.getUrl(word, language, voice, speed, volume)
        return "$url&response_format=mp3"
    }

    @Composable
    override fun ColumnScope.Settings(
        conf: TTSConf,
        onSettingSpeedFinish: (Float) -> Unit,
        onSettingVolumeFinish: (Float) -> Unit
    ) {
        SpeedSettings(
            conf = conf,
            valueRange = 50f..200f,
            steps = 12,
            onFinish = onSettingSpeedFinish
        )
        VolumeSettings(
            conf = conf,
            valueRange = 50f..200f,
            steps = 20,
            onFinish = onSettingVolumeFinish
        )
    }

    override val name: String = "Sambert"
    override val defaultExtraConf: TTSExtraConf = TTSExtraConf(volume = 125)
}

val ttsProviders: List<TTSProvider> = persistentListOf(
    BaiduTransTTSProvider,
    OpenAIProvider,
    SambertProvider,
//    EdgeTTSProvider
)

fun findTTSProviderById(id: String) = ttsProviders.find { it.id == id } ?: BaiduTransTTSProvider