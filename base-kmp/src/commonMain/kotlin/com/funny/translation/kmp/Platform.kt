package com.funny.translation.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

val currentPlatform: Platform by lazy { getPlatform() }