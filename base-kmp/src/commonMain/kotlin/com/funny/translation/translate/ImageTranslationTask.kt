package com.funny.translation.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.kmp.base.strings.ResStrings
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ImageTranslationPart(
    val source: String,
    var target: String,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 100,
    val height: Int = 100
) {

    /**
     * 合并产生新的矩形框
     * @param other ImageTranslationPart 只使用 x, y, width, height
     * @param newSource String
     * @param newTarget String
     * @return ImageTranslationPart
     */
    fun combineWith(other: ImageTranslationPart): ImageTranslationPart {
        val x1 = minOf(x, other.x)
        val y1 = minOf(y, other.y)
        val x2 = maxOf(x + width, other.x + other.width)
        val y2 = maxOf(y + height, other.y + other.height)
        return this.copy(
            x = x1,
            y = y1,
            width = x2 - x1,
            height = y2 - y1
        )
    }

}

sealed interface ImageTranslationResult {
    val source: String
    val target: String

    @kotlinx.serialization.Serializable
    data class Normal(
        @SerialName("erased_img")
        val erasedImgBase64: String? = null,
        override val source: String = "",
        override val target: String = "",
        val content: List<ImageTranslationPart> = arrayListOf()
    ): ImageTranslationResult

    class Model(): ImageTranslationResult {

        override var source: String = ""
        var streamingResult by mutableStateOf("")
        var error by mutableStateOf("")

        override val target: String get() = streamingResult.trim()
    }
}

abstract class ImageTranslationTask: CoreTranslationTask() {
    @Throws(TranslationException::class)
    open suspend fun translate(){
        if (!supportLanguages.contains(sourceLanguage) || !supportLanguages.contains(targetLanguage)){
            throw TranslationException(ResStrings.unsupported_language)
        }
        if (sourceLanguage == targetLanguage) return
    }

    abstract val isOffline: Boolean
    abstract val result: ImageTranslationResult
}

abstract class NormalImageTranslationTask(
    var sourceImg: ByteArray = byteArrayOf()
) : ImageTranslationTask() {
    override val isOffline: Boolean = false
    override var result = ImageTranslationResult.Normal()
}