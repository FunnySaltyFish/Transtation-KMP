package com.funny.translation.translate.tts

import androidx.annotation.IntRange
import com.funny.translation.database.TtsConf
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.kmp.NavController
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.TranslateScreen
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
    get() = findTTSProviderById(ttsProviderId).savedExtraConf.speed
val TTSConf.volume: Int
    get() = findTTSProviderById(ttsProviderId).savedExtraConf.volume

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
        return appDB.tTSConfQueries.getById(id).executeAsOne()
    }

    fun findByLanguage(language: Language): TTSConf {
        return appDB.tTSConfQueries.getByLanguage(language).executeAsOne()
    }

    fun createDefaultConf(language: Language): TTSConf {
        return TTSConf(
            language = language,
            ttsProviderId = BaiduTransTTSProvider.id,
            speaker = BaiduTransTTSProvider.DEFAULT_SPEAKERS.first(),
            extraConf = BaiduTransTTSProvider.defaultExtraConf
        )
    }

    fun createNewAndJump(navController: NavController, language: Language) {
        // 创建默认配置，并直接跳转到编辑页面
        createDefaultConf(language).let(appDB.tTSConfQueries::insert)

        navController.navigate(
            TranslateScreen.TTSEditConfScreen.route.formatBraceStyle(
                "id" to appDB.tTSConfQueries.getByLanguage(language).executeAsOne().id
            )
        )
    }
}