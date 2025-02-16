package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.funny.compose.ai.bean.Model
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings

@Composable
fun MaxSegmentSettingsPart(
    initialValue: Int?,
    model: Model,
    onUpdate: (Int) -> Unit
) {
    var segmentLength: Int? by rememberStateOf(initialValue)

    Category(
        title = ResStrings.long_text_max_seg_length,
        helpText = ResStrings.long_text_max_seg_length_help,
        expandable = false
    ) {
        TextField(
            value = segmentLength?.toString() ?: "",
            onValueChange = { value ->
                segmentLength = value.toIntOrNull() ?: segmentLength
                segmentLength?.let(onUpdate)
            },
            label = { Text(ResStrings.long_text_max_seg_length) },
            placeholder = {
                Text(ResStrings.long_text_max_seg_length_placeholder.format(model.maxOutputTokens.toString()))
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}