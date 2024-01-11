package com.funny.translation.kmp

import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.funny.translation.helper.readText
import com.funny.translation.helper.writeText

actual fun Uri.writeText(text: String) {
    this.toAndroidUri().writeText(appCtx, text)
}

actual fun Uri.readText(): String {
    return this.toAndroidUri().readText(appCtx)
}