package com.funny.translation.translate.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.helper.Log
import com.funny.translation.translate.Language
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import java.net.URL

actual object AudioPlayer {
    private const val TAG = "AudioPlayer"

    private var player: AdvancedPlayer? = null

    actual var currentPlayingText by mutableStateOf("")
    actual var playbackState by mutableStateOf(PlaybackState.IDLE)

    actual fun playOrPause(
        word: String,
        language: Language,
        onStartPlay: () -> Unit,
        onComplete: () -> Unit,
        onInterrupt: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            if (player != null && playbackState == PlaybackState.PLAYING) {
                pause()
                onInterrupt()
            } else {
                val urlStr = TTSConfManager.getURL(word, language) ?: return
                val url = URL(urlStr)
                val inputStream = url.openStream()
                player = AdvancedPlayer(inputStream)

                player?.playBackListener = object : PlaybackListener() {
                    override fun playbackStarted(evt: PlaybackEvent?) {
                        super.playbackStarted(evt)
                        Log.d(TAG, "playbackStarted: ${evt?.id}")
                        playbackState = PlaybackState.PLAYING
                    }

                    override fun playbackFinished(evt: PlaybackEvent?) {
                        Log.d(TAG, "playbackFinished: ${evt?.id}")
                        if (evt?.id == PlaybackEvent.STOPPED) {
                            currentPlayingText = ""
                            playbackState = PlaybackState.IDLE
                            onComplete()
                        }
                    }
                }

                Thread {
                    try {
                        onStartPlay()
                        currentPlayingText = word
                        playbackState = PlaybackState.LOADING
                        player?.play()
                    } catch (e: JavaLayerException) {
                        onError(e)
                        currentPlayingText = ""
                        playbackState = PlaybackState.IDLE
                    }
                }.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }

    actual fun pause() {
        if (player != null && playbackState == PlaybackState.PLAYING) {
            player?.close()
            player = null
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }

    actual fun stop() {
        if (player != null) {
            player?.close()
            player = null
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }
}
