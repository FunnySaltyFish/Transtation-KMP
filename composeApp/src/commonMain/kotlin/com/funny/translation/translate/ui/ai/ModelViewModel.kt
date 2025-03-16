package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.Log
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val TAG = "ModelViewModel"

/**
 * 需要用到 Model 的 ViewModel
 */
abstract class ModelViewModel: BaseViewModel() {
    var modelList by mutableStateOf<ImmutableList<Model>>(persistentListOf())
    var chatBot by mutableStateOf<ModelChatBot>(ModelChatBot.Empty)

    open fun onModelListLoaded(currentSelectBotId: Int, models: List<Model>) {
        Log.d(TAG, "onModelListLoaded: size = ${models.size}")

        modelList = models.toImmutableList()
        if (currentSelectBotId >= 0) {
            chatBot = (modelList.find { it.chatBotId == currentSelectBotId } ?: modelList[0]).let {
                ModelChatBot(it)
            }
        }
    }

    fun updateChatBot(model: Model) {
        chatBot = ModelChatBot(model)
    }
}