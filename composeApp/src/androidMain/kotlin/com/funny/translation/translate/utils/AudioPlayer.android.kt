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
//        if (language == Language.AUTO) {
//            appCtx.toastOnUi(ResStrings.speak_language_auto_not_supported)
//            return
//        }
        if (DeviceUtils.isMute()) {
            appCtx.toastOnUi(ResStrings.speak_device_mute_tip)
        }
        val url = TTSManager.getURL(word, language)
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
            }
            mediaPlayer.setOnPreparedListener {
                it.start()
                onStartPlay()
            }
            if(mediaPlayer.isPlaying) {
                // 点两次当做暂停
                if (currentPlayingText == word) {
                    appCtx.toastOnUi(ResStrings.speak_stopped)
                    mediaPlayer.pause()
                    currentPlayingText = ""
                    onComplete()
                    return
                } else {
                    mediaPlayer.pause()
                    onInterrupt()
                }
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(appCtx, Uri.parse(url))
            mediaPlayer.prepareAsync()
            currentPlayingText = word
        }catch (e : Exception){
            e.printStackTrace()
            onError(e)
            currentPlayingText = ""
        }
    }

    actual fun pause(){
        if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            currentPlayingText = ""
        }
    }
}
