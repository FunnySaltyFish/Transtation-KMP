package com.funny.compose.ai.service

import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.token.DefaultTokenCounter
import com.funny.translation.AppConfig
import com.funny.translation.helper.JSONObjectSerializer
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.getLanguageCode
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.network.CommonData
import com.funny.translation.network.DefaultModelExtractor.Companion.HEADER_BASE_READ_TIMEOUT
import com.funny.translation.network.DefaultModelExtractor.Companion.HEADER_MODEL_ID
import com.funny.translation.network.DefaultModelExtractor.Companion.HEADER_PER_CHAR_TIMEOUT
import com.funny.translation.network.DefaultModelExtractor.Companion.HEADER_TEXT_LENGTH
import com.funny.translation.network.DynamicTimeout
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

private const val TAG = "AIService"
val EmptyJsonObject = JSONObject()

@Serializable
data class AskStreamRequest(
    @SerialName("model_id") val modelId: Int,
    @SerialName("messages") val messages: List<ChatMessageReq>,
    @SerialName("prompt") val prompt: String,
    // args
    @Serializable(with = JSONObjectSerializer::class)
    @SerialName("args") val args: JSONObject = EmptyJsonObject,
)

@Serializable
data class CountTokenMessagesRequest(
    @SerialName("token_counter_id") val tokenCounterId: String,
    @SerialName("messages") val messages: List<ChatMessageReq>,
)

interface AIService {
    /**
     * 会返回流，包括开始的 <<start>> 、<<error>> 和结束的 <<end>>
     * @param req AskStreamRequest
     * @return ResponseBody
     */
    @POST("ai/ask_stream")
    // 设置超时时间
    @Streaming
    @DynamicTimeout(connectTimeout = 20, readTimeout = 45, writeTimeout = 30)
    suspend fun askStream(
        @Body req: AskStreamRequest,
        @Header(HEADER_MODEL_ID) modelId: Int,
        @Header(HEADER_TEXT_LENGTH) textLength: Int,
        @Header(HEADER_BASE_READ_TIMEOUT) baseReadTimeout: Int = 60,
        @Header(HEADER_PER_CHAR_TIMEOUT) perCharTimeoutMillis: Int = 5
    ): ResponseBody

    /**
     * 流式翻译
     */
    @POST("api/translate_streaming")
    @FormUrlEncoded
    @Streaming
    @DynamicTimeout(connectTimeout = 20, readTimeout = 45, writeTimeout = 30)
    suspend fun translateStream(
        @Field("text") text: String,
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("model_id") modelId: Int,
        @Field("explain") explain: Boolean = AppConfig.sAITransExplain.value,
        @Header(HEADER_MODEL_ID) headerModelId: Int = modelId,
        @Header(HEADER_TEXT_LENGTH) textLength: Int = text.length,
        @Header(HEADER_BASE_READ_TIMEOUT) baseReadTimeout: Int = 60,
        @Header(HEADER_PER_CHAR_TIMEOUT) perCharTimeoutMillis: Int = 5
    ): ResponseBody


    @GET("ai/get_models")
    suspend fun getChatModels(
        // 这个 lang 并不实际使用，主要是区分 url，避免 nginx 缓存带来的问题
        @Query("lang") lang: String = LocaleUtils.getLanguageCode(),
        @Query("uid") uid: Int = AppConfig.uid
    ) : List<Model>

    @POST("ai/count_tokens_text")
    @FormUrlEncoded
    suspend fun countTokensText(
        @Field("token_counter_id") tokenCounterId: String,
        @Field("text") text: String,
        @Field("max_length") maxLength: Int? = null,
    ): CommonData<Int>

    @POST("ai/count_tokens_messages")
    suspend fun countTokensMessages(
        @Body req: CountTokenMessagesRequest
    ): CommonData<Int>

    @GET("ai/truncate_text")
    suspend fun truncateText(
        @Query("token_counter_id") tokenCounterId: String,
        @Query("text") text: String,
        @Query("max_length") maxLength: Int,
    ): CommonData<String>

    // 根据模型id和原始图片尺寸，获取到压缩后的图片大小
    // 格式为 Pair<width, height>
    @GET("ai/get_image_compressed_size")
    suspend fun getImageCompressedSize(
        @Query("model_id") modelId: Int,
        @Query("width") width: Int,
        @Query("height") height: Int,
        @Query("args") args: JSONObject = EmptyJsonObject,
    ): CommonData<List<Int>>
}

val aiService by lazy {
    ServiceCreator.create(AIService::class.java)
}

suspend fun AIService.askAndParseStream(req: AskStreamRequest, model: Model): Flow<StreamMessage> {
    return askStream(req, modelId = model.chatBotId,
        textLength = DefaultTokenCounter.countMessages(req.messages),
        baseReadTimeout = model.baseTimeout,
        perCharTimeoutMillis = model.perCharTimeoutMillis).asFlow().asStreamMessageFlow()
}

/**
问一个问题并直接返回结果，默认处理掉 `<<start>>` 和 `<<end>>`，返回中间的部分，并且自动完成扣费
 */
suspend fun AIService.askAndProcess(
    req: AskStreamRequest,
    model: Model,
    onError: (String) -> Unit = { appCtx.toastOnUi(it) },
): String {
    val output = StringBuilder()
    askAndParseStream(req, model).collect {
        when (it) {
            is StreamMessage.Error -> {
                onError(it.error)
            }
            is StreamMessage.Part -> {
                output.append(it.part)
            }
            else -> Unit
        }
    }
    return output.toString()
}

suspend fun ResponseBody.asFlow() = withContext(Dispatchers.IO) {
    flow {
        val response = this@asFlow
        response.source().use { inputStream ->
            val buffer = ByteArray(1024)
            try {
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    emit(String(buffer, 0, read))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit("<<error>>" + e.message)
            }
        }
    }
}

suspend fun ResponseBody.asFlowByLines() = withContext(Dispatchers.IO) {
    flow {
        val response = this@asFlowByLines
        response.source().use { inputStream ->
            try {
                val buffer = ByteArray(1024)
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    val str = String(buffer, 0, read)
                    val arr = str.split("\n")
                    arr.forEach {
                        if (it.isNotEmpty()) emit(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit("<<error>>" + e.message)
            }
        }
    }
}

/**
 * 将一个字符串流转换为 StreamMessage 的流，并自动完成扣费
 * @receiver Flow<String>
 * @return Flow<StreamMessage>
 */
suspend fun Flow<String>.asStreamMessageFlow() = map {
    when {
        it.startsWith("<<error>>") -> {
            val remaining = it.removePrefix("<<error>>")
            StreamMessage.Error(remaining)
        }
        it.startsWith("<<start>>") -> StreamMessage.Start
        it.startsWith("<<end>>") -> {
            val remaining = it.removePrefix("<<end>>")
            if (remaining != "") {
                JsonX.fromJson<StreamMessage.End>(remaining).also {
                    AppConfig.subAITextPoint(it.consumption)
                }
            } else {
                StreamMessage.End()
            }
        }
        else -> StreamMessage.Part(it)
    }
}.onCompletion { err ->
    if (err != null) {
        emit(StreamMessage.Error(err.message ?: "Unknown error"))
    }
}