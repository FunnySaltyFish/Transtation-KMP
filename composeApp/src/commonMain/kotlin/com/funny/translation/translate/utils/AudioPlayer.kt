package com.funny.translation.translate.utils

import com.funny.translation.translate.Language

enum class PlaybackState {
    IDLE, PLAYING, LOADING
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

    fun stop()
}