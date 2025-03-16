package com.funny.compose.ai.utils

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.aiService
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ModelSortType {
    DEFAULT,
    NAME_ASC,
    NAME_DESC,
    COST_ASC,
    COST_DESC
}

typealias ModelList = List<Model>
// int: 用于触发流更新
typealias ModelLoadingState = LoadingState<Pair<Int, List<Model>>>

object ModelManager {
    private const val TAG = "ModelManager"
    // 单一状态源，这里面的数据已经排序过
    private val _modelState: MutableStateFlow<ModelLoadingState> = MutableStateFlow(LoadingState.Loading)
    val modelState = _modelState.asStateFlow()

    private val _modelsIndex: HashMap<Model, Int> = hashMapOf()

    private var sortType by mutableDataSaverStateOf(
        DataSaverUtils,
        key = "model_sort_type",
        initialValue = ModelSortType.DEFAULT
    )

    val firstLoadChannel = Channel<List<Model>>()

    // 已启用模型的快捷访问
    @OptIn(ExperimentalCoroutinesApi::class)
    val enabledModels = _modelState.mapLatest { state ->
        val list = state.getOrNull()?.second ?: return@mapLatest emptyList()
        Log.d(TAG, "enabledModels triggered ${list.size}")
        list.filterEnabled()
    }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // 加载函数
    fun loadModels() {
        if (_modelState.value.getList().isNotEmpty()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            _modelState.update { LoadingState.Loading }
            try {
                val models = aiService.getChatModels()
                _modelsIndex.clear()
                models.forEachIndexed { index, model ->
                    _modelsIndex[model] = index
                }
                _modelState.update {
                    LoadingState.Success(Random.nextInt() to models.sort(sortType))
                }
            } catch (e: Exception) {
                _modelState.update {
                    LoadingState.Failure(e)
                }
            } finally {
                val list = _modelState.value.getList()
                Log.d(TAG, "loadModels: ${list.size}, enabledModels=${enabledModels.value.size}")
                firstLoadChannel.send(list)
            }
        }
    }

    /**
     * 强制触发 enabledModels 刷新
     *
     */
    fun refresh() {
        updateModels(_modelState.value.getList())
    }

    fun updateSort(sortType: ModelSortType) {
        updateModels(_modelState.value.getList().sort(sortType))
    }

    fun updateModels(newList: List<Model>) {
        _modelState.update {
            if (newList.isEmpty()) LoadingState.Loading else LoadingState.Success(Random.nextInt() to newList)
        }
    }

    // 重试
    fun retry() = loadModels()

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

    fun List<Model>.filterEnabled() = filter { DataSaverUtils.readData(it.enableKey, true) }
    fun ModelLoadingState.getList() = getOrNull()?.second ?: emptyList()

    // 模型启用键
    val Model.enableKey get() = "llm_model_${name}_enabled"

    // 初始化
    init {
        loadModels()
    }
}