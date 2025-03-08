package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.askAndProcess
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.chatHistoryDao
import com.funny.translation.translate.task.ModelImageChatTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ChatViewModel"

class ChatViewModel: ModelViewModel() {
    private val dao = appDB.chatHistoryDao

    val inputText = mutableStateOf("")
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
    var currentMessage: ChatMessage? by mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    var systemPrompt by mutableDataSaverStateOf(DataSaverUtils, "key_chat_base_prompt", ResStrings.chat_system_prompt)
    var maxHistoryMsgNum by mutableDataSaverStateOf(DataSaverUtils, "key_chat_max_history_msg_num", 3)
    private val memory get() = ChatMemoryFixedMsgLength(maxHistoryMsgNum)

    var checkingPrompt by mutableStateOf(false)
    
    private var job: Job? = null

    init {
        // TODO 更改为多个 ConvId 的支持
        convId.value = "convId"

        messages.addAll(dao.getMessagesByConversationId(convId.value!!))
    }

    private fun addMessage(chatMessage: ChatMessage) {
        messages.add(chatMessage)
        submit {
            dao.insert(chatMessage)
        }
    }

    private fun addMessage(sender: String, message: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                botId = chatBot.id,
                conversationId = convId,
                sender = sender,
                content = message,
                type = ChatMessageTypes.TEXT
            )
        )
    }

    private fun addErrorMessage(error: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                botId = chatBot.id,
                conversationId = convId,
                sender = chatBot.name,
                content = "",
                error = error,
                type = ChatMessageTypes.ERROR
            )
        )
    }

    fun ask(message: String, inputImageUriList: List<String>, onFinishPreprocessing: () -> Unit){
        if (message.isEmpty() && inputImageUriList.isEmpty()) return
        convId.value ?: return
        viewModelScope.launch {
            try {
                val processedImgList = preprocessImageUriList(inputImageUriList)
                processedImgList.forEach {
                    addMessage(
                        ChatMessage(
                            botId = chatBot.id,
                            conversationId = convId.value!!,
                            sender = SENDER_ME,
                            content = it,
                            type = ChatMessageTypes.IMAGE
                        )
                    )
                }
                if (message.isNotEmpty()) {
                    addMessage(SENDER_ME, message)
                }
                // 如果最大上下文长度甚至不够，那么扩充一下
                if (processedImgList.size > maxHistoryMsgNum) {
                    maxHistoryMsgNum = processedImgList.size + 1
                }
                inputText.value = ""
                startAsk()
            } catch (e: Exception) {
                e.printStackTrace()
                appCtx.toastOnUi(e.displayMsg())
            } finally {
                onFinishPreprocessing()
            }
        }
    }

    private suspend fun preprocessImageUriList(inputImageUriList: List<String>): List<String> = withContext(Dispatchers.IO){
        val tasks = inputImageUriList.map {
            viewModelScope.async {
                ModelImageChatTask(
                    model = chatBot.model,
                    fileUri = it,
                    otherHistoryMessages = emptyList(),
                    systemPrompt = systemPrompt,
                    coroutineScope = viewModelScope
                ).run {
                    processImage()
                }
            }
        }
        tasks.awaitAll()
    }

    private fun startAsk() {
        job = viewModelScope.launch(Dispatchers.IO) {
            chatBot.chat(messages, systemPrompt, memory).collect {
                Log.d(TAG, "received stream msg: $it")
                when (it) {
                    is StreamMessage.Start -> {
                        currentMessage = ChatMessage(
                            botId = chatBot.id,
                            conversationId = convId.value!!,
                            sender = chatBot.name,
                            content = "",
                            type = it.type
                        )
                    }
                    is StreamMessage.Part -> {
                        val msg = currentMessage
                        currentMessage = msg?.copy(content = msg.content + it.part)
                    }
                    is StreamMessage.End -> {
                        addMessage(currentMessage!!)
                        currentMessage = null
                    }
                    is StreamMessage.Error -> {
                        addErrorMessage(it.error.removePrefix("<<error>>"))
                        currentMessage = null
                    }
                }
            }
        }
    }

    fun checkPrompt(newPrompt: String) {
        if (checkingPrompt) return
        checkingPrompt = true
        submit {
            try {
                val txt = aiService.askAndProcess(
                    AskStreamRequest(
                        chatBot.id,
                        listOf(ChatMessageReq.text("user", newPrompt)),
                        CHECK_PROMPT_PROMPT,
                    ),
                    model = chatBot.model
                ).lowercase()

                when {
                    txt.contains("true", ignoreCase = true)-> systemPrompt = newPrompt
                    txt.contains("false", ignoreCase = true) -> appCtx.toastOnUi(ResStrings.not_correct_prompt)
                    else -> appCtx.toastOnUi(ResStrings.unparseable_prompt)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appCtx.toastOnUi(e.displayMsg())
            }
            checkingPrompt = false
        }
    }

    fun clearMessages() {
        messages.clear()
        submit {
            dao.clearMessagesByConversationId(convId.value!!)
        }
    }

    fun removeMessage(message: ChatMessage) {
        messages.remove(message)
        submit {
            dao.delete(message.id)
        }
    }

    fun doRefresh() {
        job?.cancel()
        if (currentMessage == null) {
            removeMessage(messages.last())
        } else {
            currentMessage = null
        }
        startAsk()
    }

    
    fun updateInputText(text: String) { inputText.value = text }
    fun updateSystemPrompt(prompt: String) { systemPrompt = prompt }

    companion object {
        private const val BASE_PROMPT = "You're ChatGPT, a helpful AI assistant."
        private const val CHECK_PROMPT_PROMPT = "Please determine whether the following Prompt is related to foreign language learning, translation, or other language-related topics. You should only return a boolean value, true or false. You must return precisely."
    }
}