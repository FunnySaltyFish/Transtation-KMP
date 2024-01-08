package com.funny.translation.translate.utils

import com.funny.translation.translate.Language
import com.funny.translation.translate.engine.TextTranslationEngines

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

private val languageMapping: Map<Language, String> by lazy {
    hashMapOf<Language, String>().apply {
        putAll(TextTranslationEngines.BaiduNormal.languageMapping)
        this[Language.CHINESE_YUE] = "cte"
    }
}

internal fun getUrl(word: String, language: Language) = String.format(
    "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
    languageMapping[language] ?: "auto",
    java.net.URLEncoder.encode(word, "UTF-8")
)