package com.funny.translation.bean

import kotlinx.serialization.Serializable

@Serializable
data class EditablePrompt(val prefix: String, val suffix: String) {
    fun toPrompt(): String {
        return prefix + suffix
    }
}