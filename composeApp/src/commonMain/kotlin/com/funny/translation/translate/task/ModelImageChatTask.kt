package com.funny.translation.translate.task

import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ModelChatBot
import kotlinx.coroutines.CoroutineScope

class ModelImageChatTask(
    chatBot: ModelChatBot,
    fileUri: String,
    otherHistoryMessages: List<ChatMessage>,
    systemPrompt: String,
    coroutineScope: CoroutineScope,
    args: Map<String, Any?> = emptyMap()
) : ModelImageTranslationTask(
    chatBot, fileUri, otherHistoryMessages, systemPrompt, coroutineScope, args
) {
    override fun onStreamEnd(end: StreamMessage.End) {

    }
}