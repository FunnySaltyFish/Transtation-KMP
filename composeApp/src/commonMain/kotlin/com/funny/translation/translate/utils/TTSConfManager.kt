package com.funny.translation.translate.utils

import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.tts.BaiduTransTTSProvider
import com.funny.translation.translate.tts.OpenAIProvider
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.findTTSProviderById
import com.funny.translation.translate.tts.speed
import com.funny.translation.translate.tts.volume
import com.funny.translation.translate.ui.TranslateScreen

/**
 * 加载、修改、保存 TTS 配置。维护内存中的配置与数据库中的配置同步。
 */
object TTSConfManager {
    private val confMap = hashMapOf<Language, TTSConf>()
    private var confProvider: (language: Language) -> TTSConf = ::findByLanguage
    private var speakingExample: Boolean = false

    fun getURL(word: String, playConf: TTSConf): String? {
        val speaker = playConf.speaker
        val language = playConf.language
        val provider = findTTSProviderById(playConf.ttsProviderId)
        if (!provider.supportLanguages.contains(language)) {
            appCtx.toastOnUi("此引擎（${provider.name}）不支持朗读 ${language.displayText}，请前往设置修改！")
            return null
        }
        appCtx.toastOnUi("${speaker.shortName} 正在为您朗读")
        val url = provider.getUrl(word, language, speaker.fullName, playConf.speed, playConf.volume)
        return if (speakingExample) {
            // 临时修改配置，播放示例音频。这个配置主要是让服务端缓存一下，节省一点点资源
            "$url&example=true"
        } else url
    }

    /**
     * 临时修改配置
     */
    fun withConf(newConf: TTSConf, block: (TTSConf) -> Unit) {
        val old = confProvider
        confProvider = { newConf }
        speakingExample = true
        block(newConf)
        speakingExample = false
        confProvider = old
    }

    fun updateConf(newConf: TTSConf) {
        confMap[newConf.language] = newConf
    }

    /**
     * 根据 ID 获取配置（内存 -> 数据库）
     * @param id Long
     * @return TTSConf
     */
    fun findById(id: Long): TTSConf {
        val memoryConf = confMap.values.find { it.id == id }
        return memoryConf ?:
            appDB.tTSConfQueries.getById(id).executeAsOne().also {
                confMap[it.language] = it
            }
    }


    /**
     * 根据语言获取配置（内存 -> 数据库），如果没有，则创建默认配置
     * @param language Language
     * @return TTSConf
     */
    fun findByLanguage(language: Language): TTSConf {
        return confMap.getOrPut(language) {
            appDB.tTSConfQueries.getByLanguage(language).executeAsOneOrNull() ?: createDefaultConf(language)
        }
    }

    /**
     * 创建新的配置并跳转到编辑页面
     * @param navController Navigator
     * @param language Language
     */
    fun createNewAndJump(navController: NavController, language: Language) {
        // 创建默认配置，并直接跳转到编辑页面
        createDefaultConf(language).let(appDB.tTSConfQueries::insert)

        val inserted = appDB.tTSConfQueries.getByLanguage(language).executeAsOne().also {
            confMap[language] = it
        }

        navController.navigate(
            TranslateScreen.TTSEditConfScreen.route.formatBraceStyle(
                "id" to inserted.id
            )
        )
    }

    /**
     * 跳转到编辑页面，如果没有，则直接新建并跳转
     * @param navController Navigator
     * @param language Language
     */
    fun jumpToEdit(navController: NavController, language: Language) {
        val conf = findByLanguage(language)
        navController.navigate(
            TranslateScreen.TTSEditConfScreen.route.formatBraceStyle(
                "id" to conf.id
            )
        )
    }

    fun createDefaultConf(language: Language): TTSConf {
        return if (language == Language.AUTO) {
            TTSConf(
                language = language,
                ttsProviderId = OpenAIProvider.id,
                speaker = OpenAIProvider.defaultSpeaker,
                extraConf = OpenAIProvider.defaultExtraConf
            )
        } else TTSConf(
            language = language,
            ttsProviderId = BaiduTransTTSProvider.id,
            speaker = BaiduTransTTSProvider.defaultSpeaker,
            extraConf = BaiduTransTTSProvider.defaultExtraConf
        )
    }
}