package com.funny.translation.translate.tts

import androidx.annotation.IntRange
import com.funny.translation.database.TtsConf
import com.funny.translation.translate.Language
import kotlinx.serialization.Serializable

//@Serializable
//data class TTSConf(
//    val id: Long = 0,
//    // Language.AUTO 对应为 all
//    val language: Language,
//    val ttsProviderId: String,
//    val speaker: Speaker,
//    val extraConf: TTSExtraConf = EmptyExtraConf,
//) {
//    val speed get() = if (extraConf.speed < 0) findTTSProviderById(ttsProviderId).defaultExtraConf.speed else extraConf.speed
//    val volume get() = if (extraConf.volume < 0) findTTSProviderById(ttsProviderId).defaultExtraConf.volume else extraConf.volume
//}

typealias TTSConf = TtsConf

// 模拟其它构造函数
fun TTSConf(
    language: Language,
    ttsProviderId: String,
    speaker: Speaker,
    extraConf: TTSExtraConf = EmptyExtraConf,
) = TtsConf(
    id = 0,
    language = language,
    ttsProviderId = ttsProviderId,
    speaker = speaker,
    extraConf = extraConf,
)

val TTSConf.speed: Int
    get() = if (extraConf.speed < 0) findTTSProviderById(ttsProviderId).defaultExtraConf.speed else extraConf.speed
val TTSConf.volume: Int
    get() = if (extraConf.volume < 0) findTTSProviderById(ttsProviderId).defaultExtraConf.volume else extraConf.volume

@Serializable
data class TTSExtraConf(
    // 速度
    @IntRange(50, 200) val speed: Int = 100,
    @IntRange(50, 200) val volume: Int = 100,
)

// 默认配置，不应该被使用
private val EmptyExtraConf = TTSExtraConf(-1, -1)

object TTSConfManager {
    fun findById(id: Long): TTSConf {
        return TTSConf(
            language = Language.AUTO,
            ttsProviderId = "baidu",
            speaker = BaiduTransTTSProvider.DEFAULT_SPEAKERS.first(),
        )
    }

    fun findByLanguage(language: Language): TTSConf {
        return TTSConf(
            language = language,
            ttsProviderId = "baidu",
            speaker = BaiduTransTTSProvider.DEFAULT_SPEAKERS.first(),
        )
    }
}