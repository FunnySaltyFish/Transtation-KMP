package com.funny.translation

import com.funny.translation.helper.openUrl
import com.funny.translation.kmp.KMPContext

actual class WebViewActivity : BaseActivity() {
    actual companion object {
        actual fun start(context: KMPContext, url: String) {
            context.openUrl(url)
        }
    }
}