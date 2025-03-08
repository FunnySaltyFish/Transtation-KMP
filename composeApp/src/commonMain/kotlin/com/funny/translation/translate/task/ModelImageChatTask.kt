package com.funny.translation.translate.task

import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.StreamMessage
import kotlinx.coroutines.CoroutineScope

class ModelImageChatTask(
    model: Model,
    fileUri: String,
    otherHistoryMessages: List<ChatMessage>,
    systemPrompt: String,
    coroutineScope: CoroutineScope,
    args: Map<String, Any?> = emptyMap()
) : ModelImageTranslationTask(
    model, fileUri, otherHistoryMessages, systemPrompt, coroutineScope, args
) {
    override fun onStreamEnd(end: StreamMessage.End) {

    }
}