package com.funny.translation.kmp

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.funny.translation.BaseApplication

actual typealias KMPContext = Context

actual val LocalKMPContext = LocalContext

actual val appCtx: KMPContext
    get() = BaseApplication.ctx