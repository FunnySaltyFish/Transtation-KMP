package com.funny.translation.translate.ui.settings

import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.TTSConf
import moe.tlaster.precompose.viewmodel.ViewModel

class TTSConfEditViewModel(
    val conf: TTSConf
): ViewModel() {
    fun updateGenderChecked(newValue: Gender) {

    }
}