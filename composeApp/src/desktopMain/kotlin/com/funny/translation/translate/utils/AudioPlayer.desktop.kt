package com.funny.translation.translate.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.translate.Language
import java.io.BufferedInputStream
import java.net.URL
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual object AudioPlayer {
    private const val TAG = "AudioPlayer"

    private var clip: Clip? = null

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
            if (clip != null && clip?.isRunning == true) {
                clip?.stop()
                clip?.close()
                currentPlayingText = ""
                playbackState = PlaybackState.PAUSED
                onComplete()
            } else {
                val url = URL(getUrl(word, language))
                clip = AudioSystem.getClip()
                val inputStream = AudioSystem.getAudioInputStream(BufferedInputStream(url.openStream()))
                clip?.open(inputStream)
                clip?.addLineListener {
                    if (it.type == LineEvent.Type.STOP) {
                        currentPlayingText = ""
                        playbackState = PlaybackState.IDLE
                        onComplete()
                    }
                }
                clip?.start()
                currentPlayingText = word
                playbackState = PlaybackState.PLAYING
                onStartPlay()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }

    actual fun pause() {
        if (clip != null && clip?.isRunning == true) {
            clip?.stop()
            clip?.close()
            currentPlayingText = ""
            playbackState = PlaybackState.PAUSED
        }
    }
}
