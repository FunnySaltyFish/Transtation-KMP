package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.strings.ResStrings

@Composable
@ExperimentalComposeUiApi
actual fun InputText(
    modifier: Modifier,
    textProvider: () -> String,
    updateText: (String) -> Unit,
    shouldRequest: Boolean,
    updateFocusRequest: (Boolean) -> Unit,
    translateAction: (() -> Unit)?
) {
    val text = textProvider()
    val enterToTranslate by AppConfig.sEnterToTranslate
    BasicTextField(
        modifier = modifier.padding(8.dp),
        value = text,
        onValueChange = updateText,
        maxLines = 6,
        decorationBox = { innerTextField ->
            if (text == "") Text(text = ResStrings.trans_text_input_hint, color = LocalContentColor.current.copy(0.8f))
            innerTextField()
        },
        keyboardActions = KeyboardActions(onDone = {
            if (enterToTranslate) translateAction?.invoke()
        }),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp),
        keyboardOptions = if (enterToTranslate) KeyboardOptions(imeAction = ImeAction.Done) else KeyboardOptions.Default
    )
}