package com.funny.translation.kmp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// JVM 平台没有 Main Dispatcher
val Dispatchers.KMPMain: CoroutineDispatcher
    get() = if (currentPlatform == Platform.Android) Main else Default