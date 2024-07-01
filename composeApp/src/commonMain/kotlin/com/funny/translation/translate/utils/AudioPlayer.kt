package com.funny.translation.translate.utils

import com.funny.translation.translate.tts.TTSConf

enum class PlaybackState {
    IDLE, PLAYING, LOADING
}

expect object AudioPlayer {
    var currentPlayingText: String
    var playbackState: PlaybackState

    fun playOrPause(
        word: String,
        conf: TTSConf,
        onStartPlay: () -> Unit = {},
        onComplete: () -> Unit = {},
        onInterrupt: () -> Unit = {},
        onError: (Exception) -> Unit
    )

    fun pause()

    fun stop()
}