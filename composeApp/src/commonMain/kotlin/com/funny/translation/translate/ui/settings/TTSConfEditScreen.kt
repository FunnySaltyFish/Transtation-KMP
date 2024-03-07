package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.ui.CommonPage
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
fun TTSConfEditScreen(
    conf: TTSConf
) {
    val vm: TTSConfEditViewModel = viewModel(keys = listOf(conf)) {
        TTSConfEditViewModel(conf)
    }

    CommonPage(
        title = conf.language.displayText
    ) {
        Category(
            title = ResStrings.gender,
        ) {
            Row {

            }
        }
    }
}

@Composable
private fun RowScope.SelectGender(
    vm: TTSConfEditViewModel,
    gender: Gender,
) {
    var checked by rememberStateOf(vm.conf.speaker.gender.contains(gender))
    Checkbox(
        checked = checked,
        onCheckedChange = {
            checked = it
            vm.updateGenderChecked(newValue = gender)
        }
    )
    Text(gender.name)
}