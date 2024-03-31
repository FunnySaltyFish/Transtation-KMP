package com.funny.translation.translate.utils

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.Language
import java.io.IOException

actual object AudioPlayer {
    private const val TAG = "AudioPlayer"

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setOnPreparedListener {
                it.seekTo(0)
                it.start()
            }
        }
    }

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
        if (DeviceUtils.isMute()) {
            appCtx.toastOnUi(ResStrings.speak_device_mute_tip)
            return
        }
        val url = TTSConfManager.getURL(word, language) ?: return
        Log.d(TAG, "play: url:$url")
        try {
            mediaPlayer.setOnErrorListener { _, _, _ ->
                onError(IOException("Load internet media error!"))
                currentPlayingText = ""
                false
            }
            mediaPlayer.setOnCompletionListener {
                currentPlayingText = ""
                onComplete()
                playbackState = PlaybackState.IDLE
            }
            mediaPlayer.setOnPreparedListener {
                it.start()
                onStartPlay()
                playbackState = PlaybackState.PLAYING
            }
            if(mediaPlayer.isPlaying) {
                // 点两次当做暂停
                if (currentPlayingText == word) {
                    appCtx.toastOnUi(ResStrings.speak_stopped)
                    pause()
                    onComplete()
                    return
                } else {
                    pause()
                    onInterrupt()
                }
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(appCtx, Uri.parse(url))
            mediaPlayer.prepareAsync()
            currentPlayingText = word
            playbackState = PlaybackState.LOADING
        }catch (e : Exception){
            e.printStackTrace()
            onError(e)
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }

    actual fun pause(){
        if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            currentPlayingText = ""
            playbackState = PlaybackState.IDLE
        }
    }

    actual fun stop() {
        mediaPlayer.stop()
        currentPlayingText = ""
        playbackState = PlaybackState.IDLE
    }
}
