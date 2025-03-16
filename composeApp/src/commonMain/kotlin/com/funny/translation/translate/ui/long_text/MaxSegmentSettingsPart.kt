package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.funny.compose.ai.bean.Model
import com.funny.translation.strings.ResStrings

@Composable
fun MaxSegmentSettingsPart(
    value: Int?,
    model: Model,
    onUpdate: (Int?) -> Unit
) {
    Category(
        title = ResStrings.long_text_max_seg_length,
        helpText = ResStrings.long_text_max_seg_length_help,
        expandable = false
    ) {
        TextField(
            value = value?.toString() ?: "",
            onValueChange = { value ->
                onUpdate(value.toIntOrNull())
            },
            placeholder = {
                Text(ResStrings.long_text_max_seg_length_placeholder.format(model.maxOutputTokens.toString()))
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}