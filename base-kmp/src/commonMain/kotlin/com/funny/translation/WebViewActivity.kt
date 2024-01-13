package com.funny.translation

import com.funny.translation.kmp.KMPContext


expect class WebViewActivity: BaseActivity {
    companion object {
        fun start(context: KMPContext, url: String)
    }
}