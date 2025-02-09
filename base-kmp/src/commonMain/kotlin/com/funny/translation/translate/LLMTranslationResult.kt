package com.funny.translation.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.helper.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal

@Serializable
enum class TranslationStage {
    @Transient
    IDLE,
    @SerialName("selecting_prompt")
    SELECTING_PROMPT,
    @SerialName("selected_prompt")
    SELECTED_PROMPT,
    @SerialName("starting_translation")
    STARTING_TRANSLATION,
    @SerialName("partial_translation")
    PARTIAL_TRANSLATION,
    @SerialName("final_extra")
    FINAL_EXTRA,
    @SerialName("error")
    ERROR,
    @Transient
    FINISH;
}

@Serializable
data class StreamingTranslation(val stage: TranslationStage, val message: String)

@Serializable
data class Cost(
    val input_tokens: Int = 0,
    val output_tokens: Int = 0,
    @Serializable(with = BigDecimalSerializer::class)
    val consumption: BigDecimal = BigDecimal.ZERO
)

@Serializable
data class LLMTransCost(
    @SerialName("selecting_prompt")
    val selectingPrompt: Cost = Cost(),
    @SerialName("actual_trans")
    val actualTrans: Cost = Cost()
) {
    val total get() = selectingPrompt.consumption + actualTrans.consumption
}

class LLMTranslationResult: TranslationResult() {
    var cost: LLMTransCost by mutableStateOf(LLMTransCost())

    override fun reset(sourceString: String, name: String) {
        super.reset(sourceString, name)
        cost = LLMTransCost()
    }
}