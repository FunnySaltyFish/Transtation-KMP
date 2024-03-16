package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.Log
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.Speaker
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.tts.ttsProviders

class TTSConfEditViewModel(
    val conf: TTSConf
): BaseViewModel() {
    var gender by mutableStateOf(conf.speaker.gender)
    val locale = conf.speaker.locale

    val filteredTTSProviders = ttsProviders.filter { it.supportLanguages.contains(conf.language) }

    val providerListMap: Map<TTSProvider, SnapshotStateList<Speaker>> = ttsProviders.associateWith {
        mutableStateListOf()
    }

    fun updateGenderChecked(newValue: Gender, checked: Boolean) {
        if (!checked) { // 取消选择
            if (gender == newValue) return // 不允许取消唯一的选择
        } else {
            gender += newValue
        }
    }

    fun updateSpeakerChecked(newValue: Speaker) {

    }

    fun queryAllSpeakers() {
        filteredTTSProviders.forEach {
            loadProviderList(it)
        }
    }

    fun loadProviderList(provider: TTSProvider) {
        submit {
            Log.d(TAG, "Start to get speakers of provider: ${provider.name}")
            providerListMap[provider]?.apply {
                clear()
                addAll(provider.getSpeakers(
                    gender, locale
                ))
            }
        }
    }

    companion object {
        private const val TAG = "TTSConfEditViewModel"
    }
}