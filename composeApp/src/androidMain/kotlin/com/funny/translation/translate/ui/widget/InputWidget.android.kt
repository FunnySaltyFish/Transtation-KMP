package com.funny.translation.translate.ui.widget

import android.content.Context
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doOnTextChanged
import com.funny.translation.AppConfig
import com.funny.translation.helper.Log
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import kotlin.math.roundToInt

private const val TAG = "InputWidget"

@Composable
@ExperimentalComposeUiApi
actual fun InputText(
    modifier: Modifier,
    textProvider: () -> String,
    updateText: (String) -> Unit,
    shouldRequest: Boolean,
    updateFocusRequest: (Boolean) -> Unit,
    translateAction: (() -> Unit)?,
) {
    val enterToTranslate by AppConfig.sEnterToTranslate
    // 因为 Compose 的 BasicTextField 下某些输入法的部分功能不能用，所以临时改回 EditText
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val inputMethodManager = appCtx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val density = LocalDensity.current.density
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            softwareKeyboardController?.hide()
        }
    }

    AndroidView(
        factory = {
            AlwaysActionDoneEditText(it).apply {
                // maxLines = 6
                hint = ResStrings.trans_text_input_hint
                background = null
                textSize = 20f
                gravity = Gravity.TOP
                setPaddingRelative(
                    (20 * density).roundToInt(),
                    (4 * density).roundToInt(),
                    (20 * density).roundToInt(),
                    (4 * density).roundToInt(),
                )
                setTextColor(textColor)

                if (enterToTranslate) {
                    inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                    setImeActionLabel(ResStrings.translate, EditorInfo.IME_ACTION_DONE)
                    setOnEditorActionListener { v, actionId, event ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            translateAction?.invoke()
                            true
                        } else {
                            false
                        }
                    }
                }

                setOnFocusChangeListener { v, hasFocus -> updateFocusRequest(hasFocus) }
                // 下面这一行的顺序被放在了最后
                // 起初它在最开始，但是 inputType = EditorInfo.TYPE_CLASS_TEXT 或导致触发 onTextChanged( text = "" )
                // 从而导致 updateText("")，从翻译结果页面返回后，输入框会被清空
                doOnTextChanged { text, start, before, count -> updateText(text.toString()) }
            }
        },
        modifier = modifier,
        update = {
            val text = textProvider()
            if (it.text.toString() != text) {
                it.setText(text)
                // 光标至于末尾
                it.setSelection(text.length)
            }
            if (shouldRequest && !it.isFocused) {
                it.requestFocus().also { Log.d(TAG, "InputText: requestFocus") }
                inputMethodManager.showSoftInput(it, 0)
            } else if (!shouldRequest && it.isFocused) {
                it.clearFocus().also { Log.d(TAG, "InputText: clearFocus") }
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
        },
    )
}

private class AlwaysActionDoneEditText(
    context: Context,
) : androidx.appcompat.widget.AppCompatEditText(context) {
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val onCreateInputConnection = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return onCreateInputConnection
    }
}
