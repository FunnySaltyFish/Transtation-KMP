package com.funny.translation.kmp

enum class Platform {
    Android, Desktop
}

expect fun getPlatform(): Platform

val currentPlatform: Platform by lazy { getPlatform() }