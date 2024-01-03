package com.funny.compose.ai.bean

import com.funny.translation.database.ChatHistory

const val SENDER_ME = "Me"

typealias ChatMessage = ChatHistory

val ChatMessage.sendByMe: Boolean
    get() = sender == SENDER_ME

object ChatMessageTypes {
    const val TEXT = 0
    const val IMAGE = 1
    const val ERROR = 99
}