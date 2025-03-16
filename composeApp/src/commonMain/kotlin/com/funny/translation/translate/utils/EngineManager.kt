package com.funny.translation.translate.utils

import androidx.compose.runtime.Stable
import com.funny.compose.ai.utils.ModelManager
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.jsDao
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.task.ModelTranslationTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

sealed class ModelManagerAction {
    /**
     * 初始化引擎
     * @property engine TranslationEngine
     * @constructor
     */
    data class OneEngineInitialized(val engine: TranslationEngine) : ModelManagerAction() {
        override fun toString(): String {
            return "OneEngineInitialized(engine=${engine.name})"
        }
    }
    data object AllEnginesInitialized : ModelManagerAction()
}


enum class EngineType {
    JS,
    MODEL,
    BIND,
}

interface LoadOneTypeScope {
    fun finishLoad()
}

/**
 * 统一管理各引擎的加载、更新
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Stable
object EngineManager {
    private const val TAG = "EngineManager"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val actionFlow: MutableSharedFlow<ModelManagerAction> = MutableSharedFlow(replay = Int.MAX_VALUE)
    private val loadedModels = ConcurrentHashMap<String, TranslationEngine>()

    private val initializedTypes = ConcurrentLinkedDeque<EngineType>()

    // 下面是一些需要计算的变量，比如流和列表
    val jsEnginesStateFlow: MutableStateFlow<List<JsTranslateTaskText>> = MutableStateFlow(emptyList())
    val bindEnginesStateFlow: MutableStateFlow<List<TranslationEngine>> = MutableStateFlow(emptyList())
    val modelEnginesState = ModelManager.enabledModels.mapLatest { list ->
        list.map {
            ModelTranslationTask(it).also(::updateLoadedModelsAndNotify)
        }
    }

    var floatWindowTranslateEngineStateFlow: MutableStateFlow<TranslationEngine> = MutableStateFlow(TextTranslationEngines.BaiduNormal)

    fun loadEngines() {
        loadOneType(EngineType.BIND) {
            Log.d(TAG, "bindEnginesStateFlow was re-triggered")
            DefaultData.bindEngines.map {
                updateLoadedModelsAndNotify(it)
                it
            }.sortedBy(SortResultUtils.defaultEngineSort).also {
                bindEnginesStateFlow.value = it
                finishLoad()
            }
        }

        loadOneType(EngineType.JS) {
            appDB.jsDao.getEnabledJs().distinctUntilChanged().collect { list ->
                Log.d(TAG, "jsEngineFlow was re-triggered")
                list.map { jsBean ->
                    JsTranslateTaskText(jsEngine = JsEngine(jsBean = jsBean)).also(::updateLoadedModelsAndNotify)
                }.sortedBy(SortResultUtils.defaultEngineSort).also {
                    jsEnginesStateFlow.value = it
                }
                finishLoad()
            }
        }

        loadOneType(EngineType.MODEL) {
            val modelList = ModelManager.firstLoadChannel.receive()
            Log.d(TAG, "modelEnginesState was re-triggered, received ${modelList.size} models")
            modelList.map {
                ModelTranslationTask(it).also(::updateLoadedModelsAndNotify)
            }
            finishLoad()
        }
    }

    fun addObserver(collector: suspend (ModelManagerAction) -> Unit) {
        scope.launch {
            actionFlow.collect(collector)
        }
    }

    fun updateFloatWindowTranslateEngine(engineName: TranslationEngineName) {
        floatWindowTranslateEngineStateFlow.value = loadedModels[engineName] ?: TextTranslationEngines.BaiduNormal
    }

    private inline fun <R> loadOneType(type: EngineType, crossinline  block: suspend LoadOneTypeScope.() -> R) {
        val loadOneTypeScope = object : LoadOneTypeScope {
            override fun finishLoad() {
                Log.d(TAG, "Type $type finished loading")
                if (!initializedTypes.contains(type)) {
                    initializedTypes.add(type)
                }
                val count = initializedTypes.size
                Log.d(TAG, "Type $type finished loading, count=$count")
                if (count == EngineType.entries.size) {
                    notifyListeners(ModelManagerAction.AllEnginesInitialized)
                    // 所有引擎加载完后，更新悬浮窗翻译引擎
                    updateFloatWindowTranslateEngine(DataSaverUtils.readData(Consts.KEY_FLOAT_WINDOW_ENGINE, TextTranslationEngines.BaiduNormal.name))
                }
            }
        }
        if (initializedTypes.contains(type)) {
            Log.d(TAG, "Type $type already initialized")
            return
        }
        scope.launch { block(loadOneTypeScope) }
    }


    private fun updateLoadedModelsAndNotify(engine: TranslationEngine) {
        if (loadedModels.containsKey(engine.name)) {
            Log.d(TAG, "Engine ${engine.name} already loaded")
            return
        }
        loadedModels[engine.name] = engine
        notifyListeners(ModelManagerAction.OneEngineInitialized(engine))
    }

    private fun notifyListeners(action: ModelManagerAction) {
        scope.launch {
            actionFlow.emit(action)
        }
    }
}