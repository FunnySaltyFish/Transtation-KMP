package com.funny.translation.translate.tts

import com.funny.translation.translate.Language
import kotlinx.serialization.Serializable

@Serializable
data class TTSConf(
    val id: Long = 0,
    // Language.AUTO 对应为 all
    val language: Language,
    val ttsProviderId: String,
    val speaker: Speaker,
    // 一个 JSON，配置自己的
    val conf: String
) {
}