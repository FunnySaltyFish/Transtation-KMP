package com.funny.translation.translate.tts

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.ui.long_text.Category
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

    @Composable
    abstract fun Settings()
}


object BaiduTransTTSProvider: TTSProvider() {
    override val id: String = "BaiduTrans"
    override val name: String = ResStrings.engine_baidu

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
            expandable = false,
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
                locale = "common"
            )
        )
    }
}

object OpenAIProvider: ServerTTSProvider("openai") {
    override val name: String = "OpenAI"
}

val ttsProviders: List<TTSProvider> = persistentListOf(
    BaiduTransTTSProvider,
    OpenAIProvider
)

fun findTTSProviderById(id: String) = ttsProviders.find { it.id == id } ?: BaiduTransTTSProvider