package com.funny.translation.translate.tts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.speed
import com.funny.translation.translate.tts.volume
import com.funny.translation.translate.utils.TTSManager

@Composable
internal fun SpeedSettings(
    conf: TTSConf,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onFinish: (Float) -> Unit
) {
    Category(
        title = ResStrings.speak_speed,
    ) {
        var speed by rememberStateOf(conf.speed.toFloat())
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = {
                TTSManager.updateConf(conf.copy(extraConf = conf.extraConf.copy(speed = speed.toInt())))
                onFinish(speed)
            }
        )
    }
}

@Composable
internal fun VolumeSettings(
    conf: TTSConf,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    playExampleAction: SimpleAction
) {
    Category(
        title = ResStrings.speak_volume,
    ) {
        var volume by rememberDataSaverState("tts_${conf.id}_volume", initialValue = conf.volume.toFloat())
        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = {
                TTSManager.updateConf(conf.copy(extraConf = conf.extraConf.copy(volume = volume.toInt())))
                playExampleAction()
            }
        )
    }
}

@Composable
private fun Category(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Normal
        )
        content()
    }
}