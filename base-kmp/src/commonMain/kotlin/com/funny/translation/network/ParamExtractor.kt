package com.funny.translation.network

import androidx.annotation.Keep
import okhttp3.Request

@Keep
interface ParamExtractor {
    fun getModelId(request: Request): Int?
    fun getBaseReadTimeout(request: Request): Int?
    fun getPerCharTimeoutMillis(request: Request): Int?
    fun getTextLength(request: Request): Int?
}

// 默认从 Header 提取
@Keep
class DefaultModelExtractor : ParamExtractor {
    companion object {
        const val HEADER_MODEL_ID = "X-App-Model-Id"
        const val HEADER_TEXT_LENGTH = "X-App-Text-Length"
        const val HEADER_BASE_READ_TIMEOUT = "X-App-Base-Read-Timeout"
        const val HEADER_PER_CHAR_TIMEOUT = "X-App-Per-Char-Timeout"
    }

    override fun getModelId(request: Request): Int? {
        return request.header(HEADER_MODEL_ID)?.toIntOrNull()
    }

    override fun getBaseReadTimeout(request: Request): Int? {
        return request.header(HEADER_BASE_READ_TIMEOUT)?.toIntOrNull()
    }

    override fun getPerCharTimeoutMillis(request: Request): Int? {
        return request.header(HEADER_PER_CHAR_TIMEOUT)?.toIntOrNull()
    }

    override fun getTextLength(request: Request): Int? {
        return request.header(HEADER_TEXT_LENGTH)?.toIntOrNull()
    }
}
