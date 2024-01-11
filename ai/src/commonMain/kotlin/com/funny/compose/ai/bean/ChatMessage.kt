package com.funny.compose.ai.bean

import com.funny.translation.database.ChatHistory
import com.funny.translation.helper.now
import java.util.UUID

const val SENDER_ME = "Me"

typealias ChatMessage = ChatHistory

// 自定义 constructor
fun ChatMessage(
    botId: Int,
    conversationId: String,
    sender: String,
    content: String,
    type: Int = ChatMessageTypes.TEXT,
    error: String = "",
): ChatMessage {
    return ChatMessage(
        id = UUID.randomUUID().toString(),
        botId = botId,
        conversationId = conversationId,
        sender = sender,
        content = content,
        type = type,
        error = error,
        timestamp = now()
    )
}

val ChatMessage.sendByMe: Boolean
    get() = sender == SENDER_ME

object ChatMessageTypes {
    const val TEXT = 0
    const val IMAGE = 1
    const val ERROR = 99
}