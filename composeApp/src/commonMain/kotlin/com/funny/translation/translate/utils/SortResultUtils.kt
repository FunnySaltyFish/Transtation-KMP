package com.funny.translation.translate.utils

import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.ai.utils.ModelManager.getList
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.JsonX
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.jsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.set

typealias TranslationEngineName = String

object SortResultUtils {
    private val _mapping = MutableStateFlow<HashMap<TranslationEngineName, Int>>(hashMapOf())
    val mapping = _mapping.asStateFlow()

    // 定义三个数据源的Flow
    private val defaultEnginesFlow = MutableStateFlow(DefaultData.bindEngines.map { it.name })
    private val jsEnginesFlow = appDB.jsDao.getAllJsFlow().map { list -> list.map { it.fileName } }
    private val modelEnginesFlow = ModelManager.modelState.map { it.getList().map { it.name } }

    // 合并三个数据源，并自动更新localEngines
    val localEngines = combine(
        defaultEnginesFlow,
        jsEnginesFlow,
        modelEnginesFlow
    ) { defaultEngines, jsEngines, modelEngines ->
        defaultEngines + jsEngines + modelEngines
    }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // 排序函数现在作为Flow的变换
    val defaultSort = { engine: String -> mapping.value.get(engine, Int.MAX_VALUE) }
    val defaultEngineSort = { engine: TranslationEngine -> mapping.value.get(engine.name, Int.MAX_VALUE) }
    val defaultResultSort = { result: TranslationResult -> mapping.value.get(result.engineName, Int.MAX_VALUE) }

    // 排序后的引擎列表
    val sortedEngines = combine(localEngines, mapping) { engines, mappingData ->
        engines.sortedBy { engine -> mappingData.get(engine, Int.MAX_VALUE) }
    }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            // 监听本地引擎列表变化，如果是首次加载则初始化mapping
            localEngines.collect { engines ->
                if (_mapping.value.isEmpty()) {
                    val savedMapping = DataSaverUtils.readData(Consts.KEY_SORT_RESULT, "")
                    if (savedMapping.isEmpty()) {
                        initMapping(engines)
                    } else {
                        readMapping(savedMapping)
                    }
                }
            }
        }
    }

    private fun initMapping(engines: List<TranslationEngineName>) {
        val newMapping = HashMap<TranslationEngineName, Int>()
        engines.forEachIndexed { i, engine ->
            newMapping[engine] = i
        }
        _mapping.value = newMapping
        saveMapping()
    }

    private fun readMapping(json: String) {
        try {
            _mapping.value = JsonX.fromJson(json)
        } catch (e: Exception) {
            // 解析失败时初始化一个新的映射
            initMapping(localEngines.value)
        }
    }

    private fun saveMapping() {
        DataSaverUtils.saveData(Consts.KEY_SORT_RESULT, JsonX.toJson(_mapping.value))
    }

    fun checkEquals(list: List<TranslationEngineName>): Boolean {
        val currentEngines = sortedEngines.value
        if (currentEngines.size != list.size) return false

        return currentEngines.zip(list).all { (a, b) -> a == b }
    }

    fun resetMappingAndSave(list: List<TranslationEngineName>) {
        val newMapping = HashMap<TranslationEngineName, Int>()
        list.forEachIndexed { i, engine ->
            newMapping[engine] = i
        }
        _mapping.value = newMapping
        saveMapping()
    }

    fun addNew(name: TranslationEngineName) {
        val currentMapping = _mapping.value.toMutableMap()
        val maxValue = if (currentMapping.isEmpty()) 0 else currentMapping.maxOf { it.value } + 1
        currentMapping[name] = maxValue // 默认排最后一个

        _mapping.value = HashMap(currentMapping)
        saveMapping()
    }

    fun remove(name: TranslationEngineName) {
        val currentMapping = _mapping.value.toMutableMap()
        currentMapping.remove(name)

        _mapping.value = HashMap(currentMapping)
        saveMapping()
    }

    fun <K, V> HashMap<K, V>.get(key: K, default: V) = try {
        get(key) ?: default
    } catch (e: Exception) {
        default
    }
}