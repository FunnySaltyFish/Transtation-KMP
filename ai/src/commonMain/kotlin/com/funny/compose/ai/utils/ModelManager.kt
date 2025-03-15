package com.funny.compose.ai.utils

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.aiService
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

enum class ModelSortType {
    DEFAULT,
    NAME_ASC,
    NAME_DESC,
    COST_ASC,
    COST_DESC
}

object ModelManager {
    private var _models: List<Model> = emptyList()
    private val _modelsIndex: HashMap<Model, Int> = hashMapOf()

    private var sortType by mutableDataSaverStateOf(
        DataSaverUtils,
        key = "model_sort_type",
        initialValue = ModelSortType.DEFAULT
    )

    val models get() = CoroutineScope(Dispatchers.IO).async {
        if (_models.isNotEmpty()) {
            return@async _models
        }
        aiService.getChatModels().also {
            _modelsIndex.clear()
            it.forEachIndexed { index, model ->
                _modelsIndex[model] = index
            }
            _models = it.sort(sortType)
        }
    }

    suspend fun safeGetModels() = kotlin.runCatching { models.await() }.getOrDefault(emptyList())

    fun updateModels(models: List<Model>) {
        _models = models
    }

    fun List<Model>.sort(sortType: ModelSortType): List<Model> {
        val models = this
        return when (sortType) {
            // 依据 _models 的位置排序
            ModelSortType.DEFAULT -> models.sortedBy { _modelsIndex[it] }
            ModelSortType.NAME_ASC -> models.sortedBy { it.name }
            ModelSortType.NAME_DESC -> models.sortedByDescending { it.name }
            ModelSortType.COST_ASC -> models.sortedBy { it.cost1kTokens }
            ModelSortType.COST_DESC -> models.sortedByDescending { it.cost1kTokens }
        }
    }
}