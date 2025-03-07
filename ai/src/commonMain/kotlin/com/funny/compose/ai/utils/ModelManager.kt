package com.funny.compose.ai.utils

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.aiService
import com.funny.translation.helper.lazyPromise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

object ModelManager {
    private var _models: List<Model> = emptyList()

    val models = CoroutineScope(Dispatchers.IO).async {
        if (_models.isNotEmpty()) {
            return@async _models
        }
        aiService.getChatModels().also {
            _models = it
        }
    }
}