package com.funny.translation.translate

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
     * 合并产生新的矩形框，其中，使用 newSource 和 newTarget 作为新的 source 和 target
     * @param other ImageTranslationPart 只使用 x, y, width, height
     * @param newSource String
     * @param newTarget String
     * @return ImageTranslationPart
     */
    fun combineWith(other: ImageTranslationPart, newSource: String, newTarget: String): ImageTranslationPart {
        val x1 = x.coerceAtMost(other.x)
        val y1 = y.coerceAtMost(other.y)
        val x2 = (x + width).coerceAtLeast(other.x + other.width)
        val y2 = (y + height).coerceAtLeast(other.y + other.height)
        return ImageTranslationPart(
            source = newSource,
            target = newTarget,
            x = x1,
            y = y1,
            width = x2 - x1,
            height = y2 - y1
        )
    }

}

@kotlinx.serialization.Serializable
data class ImageTranslationResult(
    @SerialName("erased_img")
    val erasedImgBase64: String? = null,
    val source: String = "",
    val target: String = "",
    val content: List<ImageTranslationPart> = arrayListOf()
)

abstract class ImageTranslationTask(
    var sourceImg: ByteArray = byteArrayOf(),
) : CoreTranslationTask() {
    var result = ImageTranslationResult()

    @Throws(TranslationException::class)
    open suspend fun translate(){
        if (!supportLanguages.contains(sourceLanguage) || !supportLanguages.contains(targetLanguage)){
            throw TranslationException(ResStrings.unsupported_language)
        }
        if (sourceLanguage == targetLanguage) return
    }

    abstract val isOffline: Boolean
}