package com.funny.compose.ai.bean

import com.funny.translation.database.ChatHistory
import com.funny.translation.helper.now
import java.util.UUID

const val SENDER_ME = "Me"

/**
 * 一条聊天消息
 * 特殊规则：
 * content:
 * - 当 Type 为 Text 时，content 为文本内容
 * - 当 Type 为 Image 时，content 为图片的 Base84@width*height
 * -
 */
typealias ChatMessage = ChatHistory

/* 自定义 constructor
 */
fun ChatMessage(
    botId: Int,
    conversationId: String,
    sender: String,
    content: String,
    type: Int = ChatMessageTypes.TEXT,
    error: String? = null,
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

fun ChatMessage.toReq(): ChatMessageReq {
    val role = if (sendByMe) "user" else "assistant"
    return when (type) {
        ChatMessageTypes.TEXT, ChatMessageTypes.ERROR -> ChatMessageReq.text(
            role = role,
            content = content
        )
        ChatMessageTypes.IMAGE -> ChatMessageReq.vision(
            content = ChatMessageReq.Vision(
                content = listOf(
                    ChatMessageReq.Vision.Content(
                        type = "image_url",
                        image_url = ChatMessageReq.Vision.Content.ImageUrl(
                            url = content
                        )
                    )
                ),
                role = role
            ),
        )

        else -> error("Invalid type of ChatMessage: $type")
    }

}

val ChatMessage.sendByMe: Boolean
    get() = sender == SENDER_ME

object ChatMessageTypes {
    const val TEXT = 0
    const val IMAGE = 1
    const val ERROR = 99
}