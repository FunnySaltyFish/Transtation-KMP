package com.funny.compose.ai.bean

import com.funny.translation.helper.BigDecimalSerializer
import com.funny.translation.translate.Cost
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
sealed class StreamMessage(val type: Int = ChatMessageTypes.TEXT) {
    data object Start: StreamMessage()
    @Serializable
    data class End(
        val input_tokens: Int = 0,
        val output_tokens: Int = 0,
        @Serializable(with = BigDecimalSerializer::class)
        val consumption: BigDecimal = BigDecimal.ZERO
    ): StreamMessage()
    data class Part(val part: String): StreamMessage()
    data class Error(val error: String): StreamMessage()
}

fun StreamMessage.End.toCost() = Cost(this.input_tokens, this.output_tokens, this.consumption)