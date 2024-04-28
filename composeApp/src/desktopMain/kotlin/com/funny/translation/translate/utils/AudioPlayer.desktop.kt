package com.funny.translation.translate.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.helper.Log
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.Language
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import kotlin.concurrent.thread

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
                thread {
                    val urlStr = TTSConfManager.getURL(word, language) ?: return@thread
                    currentPlayingText = word
                    playbackState = PlaybackState.LOADING
                    Log.d(TAG, "playOrPause: $urlStr")

                    val resp = OkHttpUtils.getResponse(urlStr)
                    val stream = resp.body?.byteStream() ?: return@thread

                    player = AdvancedPlayer(stream)

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

                    try {
                        onStartPlay()

                        player?.play()
                    } catch (e: JavaLayerException) {
                        e.printStackTrace()
                        onError(e)
                        currentPlayingText = ""
                        playbackState = PlaybackState.IDLE
                    }
                }
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
