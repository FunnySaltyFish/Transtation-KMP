package com.funny.translation.bean

import com.funny.translation.kmp.base.strings.ResStrings

enum class ClickClipboardHintAction(val displayText: String) {
    InputText(ResStrings.click_clipboard_hint_action_input_text),
    Translate(ResStrings.click_clipboard_hint_action_translate);

    override fun toString(): String {
        return displayText
    }
}