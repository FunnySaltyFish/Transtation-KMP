package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.compose.loading.LoadingState
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.Speaker
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.tts.ttsProviders
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.TTSManager

class TTSConfEditViewModel(
    private val initialConf: TTSConf
): BaseViewModel() {
    var conf by mutableStateOf(initialConf)
        private set

    var gender by mutableStateOf(Gender.All)
    val speaker by derivedStateOf {  conf.speaker }
    var speaking by mutableStateOf(false)

    private val locale = conf.speaker.locale

    val filteredTTSProviders = ttsProviders.filter { it.supportLanguages.contains(conf.language) }

    val providerListMap: Map<TTSProvider, MutableState<LoadingState<List<Speaker>>>> = filteredTTSProviders.associateWith {
        mutableStateOf(LoadingState.Loading)
    }

    fun updateGenderChecked(newValue: Gender, checked: Boolean) {
        if (!checked) { // 取消选择
            if (gender == newValue) return // 不允许取消唯一的选择
            gender -= newValue
        } else {
            gender += newValue
        }

        queryAllSpeakers()
    }

    fun updateSpeakerChecked(ttsProvider: TTSProvider, newSpeaker: Speaker) {
        conf = conf.copy(ttsProviderId = ttsProvider.id, speaker = newSpeaker, extraConf = ttsProvider.savedExtraConf)
        speakExampleText()
    }

    fun updateSpeed(speed: Int) {
        conf = conf.copy(extraConf = conf.extraConf.copy(speed = speed))
        speakExampleText()
    }

    fun queryAllSpeakers() {
        filteredTTSProviders.forEach {
            loadProviderList(it)
        }
    }

    fun loadProviderList(provider: TTSProvider) {
        submit {
            Log.d(TAG, "Start to get speakers of provider: ${provider.name}")
            runCatching {
                val res = provider.getSpeakers(
                    gender, locale
                )
                providerListMap[provider]?.value = LoadingState.Success(res)
            }.onFailure {
                Log.e(TAG, "Failed to get speakers of provider: ${provider.name}", it)
                providerListMap[provider]?.value = LoadingState.Failure(it)
                appCtx.toastOnUi(ResStrings.loading_error)
            }
        }
    }

    fun save() {
        if (initialConf.speaker != speaker || initialConf.extraConf != conf.extraConf) {
            TTSManager.updateConf(conf)
            appDB.tTSConfQueries.updateById(
                id = conf.id,
                ttsProviderId = conf.ttsProviderId,
                speaker = speaker,
                extraConf = conf.extraConf
            )
        }
    }

    private fun speakExampleText() {
        TTSManager.withConf(
            newConf = conf,
        ) { conf ->
            Log.d(TAG, "Start to speak example text with conf = $conf")
            speaking = true
            AudioPlayer.playOrPause(
                exampleSpeakText[conf.language] ?: "",
                conf.language,
                onStartPlay = {

                },
                onComplete = {
                    speaking = false
                },
                onInterrupt = {
                    speaking = false
                },
                onError = {
                    speaking = false
                    Log.e(TAG, "Failed to play example text", it)
                    appCtx.toastOnUi(ResStrings.snack_speak_error)
                }
            )
        }
    }

    companion object {
        private const val TAG = "TTSConfEditViewModel"
        private val exampleSpeakText by lazy {
            mapOf(
                Language.AUTO to "你好，这是示例朗读文本。Hello, this is an example text to be spoken.",
                Language.CHINESE to "你好，这是示例朗读文本。",
                Language.ENGLISH to "Hello, this is an example text to be spoken.",
                Language.JAPANESE to "こんにちは、これは朗読の例文です。",
                Language.KOREAN to "안녕하세요, 이것은 읽어 들일 예제 텍스트입니다.",
                Language.FRENCH to "Bonjour, ceci est un exemple de texte à lire.",
                Language.RUSSIAN to "Привет, это пример текста для озвучивания.",
                Language.GERMANY to "Hallo, das ist ein Beispieltext zum Vorlesen.",
                Language.WENYANWEN to "文言文示例朗读文本。",
                Language.THAI to "สวัสดี นี่คือตัวอย่างข้อความที่จะพูด.",
                Language.PORTUGUESE to "Olá, este é um exemplo de texto para ser falado.",
                Language.VIETNAMESE to "Xin chào, đây là một ví dụ văn bản để đọc.",
                Language.ITALIAN to "Ciao, questo è un esempio di testo da leggere.",
                Language.CHINESE_YUE to "粤语示例朗读文本。",
                Language.SPANISH to "Hola, este es un ejemplo de texto para ser hablado."
            )
        }

    }
}
