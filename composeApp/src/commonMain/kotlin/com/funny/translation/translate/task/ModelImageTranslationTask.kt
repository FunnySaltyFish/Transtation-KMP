package com.funny.translation.translate.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.bean.toCost
import com.funny.compose.ai.bean.toReq
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.EmptyJsonObject
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.askAndParseStream
import com.funny.translation.AppConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.Log
import com.funny.translation.helper.extractSuffix
import com.funny.translation.kmp.appCtx
import com.funny.translation.network.api
import com.funny.translation.translate.CoreTranslationTask
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.ImageTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Base64
import java.util.UUID
import kotlin.reflect.KClass

internal val modelLanguageMapping by lazy {
    allLanguages.associateWith { it.displayText }
}

open class ModelImageTranslationTask(
    protected val model: Model,
    protected val fileUri: String,
    protected val otherHistoryMessages: List<ChatMessage> = emptyList(),
    protected val systemPrompt: String = "",
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    protected val args: Map<String, Any?> = emptyMap(),
    private val onFinish: () -> Unit = { }
): ImageTranslationTask() {
    companion object {
        private const val TAG = "ModelImageTranslationTask"
    }

    override val name: String = model.name
    override val supportLanguages: List<Language> = allLanguages
    override val languageMapping: Map<Language, String> = modelLanguageMapping
    override val taskClass: KClass<out CoreTranslationTask> = this::class
    override val isOffline: Boolean = false

    private var job: Job? = null

    var compressedBase64Data: String? = null
    var generating by mutableStateOf(false)

    override val result = ImageTranslationResult.Model()

    override suspend fun translate() {
        super.translate()
        job?.cancel()
        job = coroutineScope.launch(Dispatchers.IO) {
            try {
                if (compressedBase64Data == null) processImage()
                val messages = otherHistoryMessages + asImageChatMessage()
                aiService.askAndParseStream(
                    AskStreamRequest(
                        modelId = model.chatBotId,
                        messages = messages.map(ChatMessage::toReq),
                        prompt = systemPrompt.ifBlank {
                            AppConfig.sAIImageTransSystemPrompt.value.toPrompt()
                        },
                        args = JSONObject(args)
                    ),
                    model = model
                ).catch { e ->
                    onStreamError(StreamMessage.Error(e.message ?: "Unknown error"))
                }.collect { part ->
                    when (part) {
                        is StreamMessage.Part -> {
                            result.streamingResult += part.part
                        }

                        is StreamMessage.End -> {
                            onStreamEnd(part)
                            generating = false
                            result.cost = part.toCost()
                            Log.d(TAG, "cost: ${result.cost}, result: ${result.streamingResult}")
                        }

                        is StreamMessage.Error -> {
                            onStreamError(part)
                            generating = false
                        }

                        StreamMessage.Start -> {
                            generating = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onStreamError(StreamMessage.Error(e.message ?: "Unknown error"))
                generating = false
            } finally {
                onFinish()
            }
        }
    }

    fun asImageChatMessage(
        conversationId: String = UUID.randomUUID().toString(),
        sender: String = SENDER_ME
    ): ChatMessage {
        if (compressedBase64Data == null) runBlocking {
            processImage()
        }
        return ChatMessage(
            botId = model.chatBotId,
            conversationId = conversationId,
            sender = sender,
            content = compressedBase64Data ?: "",
            type = ChatMessageTypes.IMAGE
        )
    }

    open fun onStreamEnd(end: StreamMessage.End) {

    }

    open fun onStreamError(error: StreamMessage.Error) {
        result.error = error.error
    }


    /**
     * 处理图片，在此逻辑中，将获取到的图片进行压缩，保存到 [sourceImg]，并转换为 Base64 编码，保存在 [compressedBase64Data] 中
     */
    suspend fun processImage() = withContext(Dispatchers.IO) {
        val (width, height) = BitmapUtil.getImageSizeFromUri(appCtx, fileUri)
        val compressedSize =
            api(aiService::getImageCompressedSize, model.chatBotId, width, height, EmptyJsonObject, rethrowErr = true) { success {  } } ?: throw Exception("Failed to get compressed size")
        val compressedWidth = compressedSize[0]
        val compressedHeight = compressedSize[1]
        val compressedBytes: ByteArray = if (compressedWidth < width || compressedHeight < height) {
            BitmapUtil.getBitmapFromUri(
                appCtx,
                compressedWidth,
                compressedHeight,
                model.inputFileTypes.maxSingleImageSize.toLong(),
                fileUri,
            ) ?: throw Exception("Failed to compress image")
        } else {
            BitmapUtil.getBitmapFromUri(fileUri) ?: throw Exception("Failed to get image bytes")
        }
        val suffix = fileUri.extractSuffix().ifBlank { "jpg" }
        val base64 = Base64.getEncoder().encodeToString(compressedBytes)
        val data = "data:image/$suffix;base64,$base64@$compressedWidth*$compressedHeight"
        compressedBase64Data = data
        data
    }


}