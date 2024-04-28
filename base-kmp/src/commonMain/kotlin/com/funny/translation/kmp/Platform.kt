package com.funny.translation.kmp

enum class Platform {
    Android, Desktop;

    val isAndroid get() = this == Android
    val isDesktop get() = this == Desktop
}

expect fun getPlatform(): Platform

val currentPlatform: Platform by lazy { getPlatform() }