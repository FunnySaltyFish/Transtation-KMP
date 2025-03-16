package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.funny.compose.ai.utils.ModelManager
import com.funny.translation.helper.BaseViewModel

private const val TAG = "ModelViewModel"

/**
 * 需要用到 Model 的 ViewModel
 */
abstract class ModelViewModel: BaseViewModel() {
    var chatBot by ModelManager.chatBot
}