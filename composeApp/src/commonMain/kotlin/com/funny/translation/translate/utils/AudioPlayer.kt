package com.funny.translation.translate.utils

import com.funny.translation.translate.Language
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.findByLanguage
import com.funny.translation.translate.tts.findTTSProviderById

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

    private fun getDefaultConf(language: Language): TTSConf {
        return confMap.getOrPut(language) { TTSConf.findByLanguage(language) }
    }

    fun getURL(word: String, language: Language): String {
        val playConf = confProvider(language)
        val speaker = playConf.speaker
        val provider = findTTSProviderById(playConf.ttsProviderId)
        return provider.getUrl(word, language, speaker.fullName, playConf.speed, playConf.volume)
    }

    /**
     * 临时修改配置
     */
    fun withConf(newConf: TTSConf, block: (TTSConf) -> Unit) {
        val old = confProvider
        confProvider = { newConf }
        block(newConf)
        confProvider = old
    }

    fun updateConf(newConf: TTSConf) {
        confMap[newConf.language] = newConf
    }
}