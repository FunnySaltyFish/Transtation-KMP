package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModelScope

private const val TAG = "ChatViewModel"

class ChatViewModel: ModelViewModel() {
    private val dao = appDB.chatHistoryDao

    val inputText = mutableStateOf("")
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
    var currentMessage: ChatMessage? by mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    var systemPrompt by mutableDataSaverStateOf(DataSaverUtils, "key_chat_base_prompt", ResStrings.chat_system_prompt)
    val memory = ChatMemoryFixedMsgLength(3)

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

    fun ask(message: String){
        if (message.isEmpty()) return
        convId.value ?: return
        addMessage(SENDER_ME, message)
        inputText.value = ""
        startAsk(message)
    }

    private fun startAsk(message: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            chatBot.chat(convId.value, message, messages, systemPrompt, memory).collect {
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
                        listOf(ChatMessageReq("user", newPrompt)),
                        CHECK_PROMPT_PROMPT,
                    )
                ).lowercase()

                when (txt) {
                    "true" -> systemPrompt = newPrompt
                    "false" -> appCtx.toastOnUi(ResStrings.not_correct_prompt)
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
        val lastMyMsg = messages.last()
        startAsk(lastMyMsg.content)
    }

    
    fun updateInputText(text: String) { inputText.value = text }
    fun updateSystemPrompt(prompt: String) { systemPrompt = prompt }

    companion object {
        private const val BASE_PROMPT = "You're ChatGPT, a helpful AI assistant."
        private const val CHECK_PROMPT_PROMPT = "Please determine whether the following Prompt is related to foreign language learning, translation, or other language-related topics. You should only return a boolean value, true or false. You must return precisely."
            // "请判定下面的Prompt是否和外语类学习、翻译类主题相关，你应该只返回一个布尔值，true或者false。必须精准的返回："
    }
}