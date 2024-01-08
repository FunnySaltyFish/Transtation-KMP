package com.funny.translation

import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.KMPContext


expect class WebViewActivity: KMPActivity {
    companion object {
        fun start(context: KMPContext, url: String)
    }
}