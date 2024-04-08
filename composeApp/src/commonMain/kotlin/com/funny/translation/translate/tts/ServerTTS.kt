package com.funny.translation.translate.tts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.funny.translation.AppConfig
import com.funny.translation.network.CommonData
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import com.funny.translation.translate.Language
import com.funny.translation.translate.network.TransNetwork
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder

interface TTSService {
    @GET("ai/tts/generate_stream")
    suspend fun generateStream(
        @Query("model_name") modelName: String,
        @Query("text") text: String,
        @Query("voice") voice: String,
        @Query("speed") speed: Int,
        @Query("volume") volume: Int
    ): String
    
    @GET("ai/tts/get_speakers")
    suspend fun getSpeakers(
        @Query("model_name") modelName: String,
        @Query("gender") gender: Gender,
        @Query("locale") locale: String
    ): CommonData<List<Speaker>>
}

abstract class ServerTTSProvider(private val modelName: String) : TTSProvider() {
    override val id: String = modelName

    /**
     * 语言到 Locale 字段的映射
     */
    abstract fun languageToLocale(language: Language): String
    
    override fun getUrl(word: String, language: Language, voice: String, speed: Int, volume: Int): String =
        String.format(
            ServiceCreator.BASE_URL + "ai/tts/generate_stream?model_name=%s&text=%s&voice=%s&speed=%d&volume=%d&uid=%d",
            modelName,
            URLEncoder.encode(word, "UTF-8"),
            voice,
            speed,
            volume,
            AppConfig.uid
        )

    override suspend fun getSpeakers(gender: Gender, locale: String): List<Speaker> {
        return api(TransNetwork.ttsService::getSpeakers, modelName, gender, locale, rethrowErr = true) {
            success {  }
        } ?: emptyList()
    }

    @Composable
    override fun Settings(conf: TTSConf, onSettingSpeedFinish: (Float) -> Unit) {
        Text("Settings Area for $id")
    }
}