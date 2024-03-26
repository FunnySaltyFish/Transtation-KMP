package com.funny.translation.translate.utils

import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.Language
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSConfManager
import com.funny.translation.translate.tts.findTTSProviderById
import com.funny.translation.translate.tts.speed
import com.funny.translation.translate.tts.volume

enum class PlaybackState {
    IDLE, PLAYING, PAUSED
}

expect object AudioPlayer {
    var currentPlayingText: String
    var playbackState: PlaybackState

    fun playOrPause(
        word: String,
        language: Language,
        onStartPlay: () -> Unit = {},
        onComplete: () -> Unit = {},
        onInterrupt: () -> Unit = {},
        onError: (Exception) -> Unit
    )

    fun pause()
}


object TTSManager {
    private val confMap = hashMapOf<Language, TTSConf>()
    private var confProvider: (language: Language) -> TTSConf = ::getDefaultConf
    private var speakingExample: Boolean = false

    private fun getDefaultConf(language: Language): TTSConf {
        return confMap.getOrPut(language) { TTSConfManager.findByLanguage(language) }
    }

    fun getURL(word: String, language: Language): String {
        val playConf = confProvider(language)
        val speaker = playConf.speaker
        appCtx.toastOnUi("${speaker.shortName} 正在为您朗读")
        val provider = findTTSProviderById(playConf.ttsProviderId)
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
}