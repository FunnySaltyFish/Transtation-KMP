package com.funny.translation.translate.utils

import com.funny.translation.network.ServiceCreator.BASE_URL
import com.funny.translation.translate.Language
import com.funny.translation.translate.engine.TextTranslationEngines
import java.net.URLEncoder

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

internal fun getUrl(word: String, language: Language) =
//    String.format(
//        "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
//        languageMapping[language] ?: "auto",
//        java.net.URLEncoder.encode(word, "UTF-8")
//    )
    /**
    args = request.args
    model_name = args.get("model_name")
    if not model_name:
    return await make_response("model_name is required", 400)
    model = TTS_MODELS.get(model_name)
    if not model:
    return await make_response(
    _("Model with name = %s does not exist").format(model_name), 400
    )
    text: str = args.get("text")
    voice: str = args.get("voice")
    if not voice or not text:
    return await make_response("voice and text is required", 400)

    speed: int = args.get("speed", 100, type=int)
    volume: int = args.get("volume", 100, type=int)
    args = args.get("args", {}, type=dict)
     */
    String.format(
        BASE_URL + "ai/tts/generate_stream?model_name=%s&text=%s&voice=%s&speed=%d&volume=%d",
        "opeanai",
        URLEncoder.encode(word, "UTF-8"),
        "alloy",
        100,
        100
    )
