package com.funny.translation.translate.ui.main

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.cash.sqldelight.paging3.QueryPagingSource
import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.ai.utils.ModelManager.enableKey
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.now
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.CoreTextTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.ThinkingStage
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.TranslationStage
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.fromTransResult
import com.funny.translation.translate.database.jsDao
import com.funny.translation.translate.database.transFavoriteDao
import com.funny.translation.translate.database.transHistoryDao
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.task.ModelTranslationTask
import com.funny.translation.translate.ui.engineselect.EnginePreset
import com.funny.translation.translate.utils.EngineManager
import com.funny.translation.translate.utils.ModelManagerAction
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.LinkedList

class MainViewModel : BaseViewModel() {
    // 全局UI状态
    var currentState: MainScreenState by mutableStateOf(MainScreenState.Normal)

    var translateText by mutableStateOf("")
    val actualTransText: String
        get() = translateText.trim()

    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SOURCE_LANGUAGE, Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_TARGET_LANGUAGE, Language.CHINESE)
    val resultList = mutableStateListOf<TranslationResult>()

    private var totalTaskNum: Int by mutableIntStateOf(1)
    private var startedTaskNum by mutableIntStateOf(0)
    val startedProgress by derivedStateOf { if (totalTaskNum == 0) 1f else startedTaskNum.toFloat() / totalTaskNum }
    private var finishedTaskNum by mutableIntStateOf(0)
    val finishedProgress by derivedStateOf { if (totalTaskNum == 0) 1f else finishedTaskNum.toFloat() / totalTaskNum }

    var selectedEngines: MutableList<TranslationEngine> = mutableStateListOf()
    var translating by mutableStateOf(false)

    // 一些私有变量
    private var translateJob: Job? = null
    private val eachTranslateJobs = mutableListOf<Pair<Job, CoreTextTranslationTask>>()
    private var engineInitialized = false
    private val evalJsMutex by lazy(LazyThreadSafetyMode.PUBLICATION) { Mutex() }

    // 下面是一些需要计算的变量，比如流和列表

    val transHistories by lazy {
        Pager(PagingConfig(pageSize = 10)) {
            val queries = appDB.transHistoryQueries
            QueryPagingSource(
                countQuery = queries.countHistory(),
                transacter = queries,
                context = Dispatchers.IO,
                queryProvider = queries::queryAllPaging,
            )
        }.flow.cachedIn(viewModelScope)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (init) {
                engineInitialized = true
                return@launch
            }

            // 随应用升级，有一些插件可能后续转化为内置引擎，旧的插件需要删除
            appDB.jsDao.getAllJs().forEach { jsBean ->
                if (DefaultData.isPluginBound(jsBean)) {
                    appDB.jsDao.deleteJsByName(jsBean.fileName)
                }
            }

            launch {
                ModelManager.modelState.collect {
                    it.getOrNull()?.second?.forEach { model ->
                        val enabled = DataSaverUtils.readData(model.enableKey, true)
                        if (!enabled) {
                            val current = selectedEngines.firstOrNull { engine ->
                                engine is ModelTranslationTask && engine.model.name == model.name
                            } ?: return@forEach
                            selectedEngines.remove(current)
                            DataSaverUtils.saveData(current.selectKey, false)
                            Log.d(TAG, "remove model: ${model.name} because it is not enabled")
                        }
                    }
                }
            }

            EngineManager.addObserver { action ->
                Log.d(TAG, "EngineManager action: $action")
                when (action) {
                    is ModelManagerAction.OneEngineInitialized -> {
                        // 读取持久数据，如果为 true 则保留选中状态
                        if (DataSaverUtils.readData(action.engine.selectKey, false)) {
                            withContext(Dispatchers.Main) {
                                addSelectedEngines(action.engine)
                            }
                        }
                    }

                    is ModelManagerAction.AllEnginesInitialized -> {
                        Log.d(
                            TAG,
                            "All engines initialized. Current selectedEngines size: ${selectedEngines.size}"
                        )
                        // 只有当持久化中没有任何选中引擎时，才添加默认引擎
                        if (selectedEngines.isEmpty()) {
                            addDefaultEngines(
                                TextTranslationEngines.BaiduNormal,
                                TextTranslationEngines.Tencent
                            )
                        }
                        engineInitialized = true
                    }
                }
            }
            init = true
        }
    }

    // 下面是各种配套的 update 方法
    fun updateTranslateText(text: String) { translateText = text }
    fun updateSourceLanguage(language: Language) { sourceLanguage = language }
    fun updateTargetLanguage(language: Language) { targetLanguage = language }
    fun updateMainScreenState(state: MainScreenState) { currentState = state }

    fun tryToPasteAndTranslate() {
        if (translateText.isNotEmpty()) return
        val clipboardText = ClipBoardUtil.read()
        if (clipboardText.isNotEmpty()) {
            translateText = clipboardText
            translate()
        }
    }

    // 下面是各种函数

    /**
     * 当什么都不选时，添加默认的引擎
     * @param engines Array<out TextTranslationEngines>
     */
    private fun addDefaultEngines(vararg engines: TextTranslationEngines) {
        selectedEngines.addAll(engines)
        engines.forEach {
            DataSaverUtils.saveData(it.selectKey, true)
        }
    }

    // 收藏与取消收藏，参数 favourited 为 false 时收藏，为 true 时取消收藏
    fun doFavorite(favourited: Boolean, result: TranslationResult){
        viewModelScope.launch(Dispatchers.IO) {
            val favoriteBean = fromTransResult(result, translateText, sourceLanguage.id)
            if(favourited){
                appDB.transFavoriteDao.deleteTransFavorite(favoriteBean.id)
            }else{
                appDB.transFavoriteDao.insertTransFavorite(favoriteBean)
            }
        }
    }

    fun removeOneResult(result: TranslationResult) {
        eachTranslateJobs.find { it.second.result == result }?.let {
            if (it.first.isActive) it.first.cancel()
            eachTranslateJobs.remove(it)
        }
        resultList.remove(result)
        totalTaskNum -= 1
        startedTaskNum -= 1
        finishedTaskNum -= 1
        if (totalTaskNum == 0 || totalTaskNum == finishedTaskNum) translating = false
        Log.d(TAG, "removeResult: ${result.engineName}, startedNum: $startedTaskNum, finishedTaskNum: $finishedTaskNum, totalTaskNum: $totalTaskNum")
    }

    fun stopOneJob(result: TranslationResult) {
        eachTranslateJobs.find { it.second.result == result }?.let {
            if (it.first.isActive) it.first.cancel()
        }
//        if (finishedTaskNum == totalTaskNum) translating = false
        Log.d(TAG, "stopOneJob: ${result.engineName}, startedNum: $startedTaskNum, finishedTaskNum: $finishedTaskNum, totalTaskNum: $totalTaskNum")
    }

    fun addSelectedEngines(vararg engines: TranslationEngine) {
        Log.d(TAG, "addSelectedEngines: ${engines.joinToString{it.name}}")
        selectedEngines.addAll(engines)
        engines.forEach { DataSaverUtils.saveData(it.selectKey, true) }
    }

    fun removeSelectedEngine(engine: TranslationEngine) {
        selectedEngines.remove(engine)
        DataSaverUtils.saveData(engine.selectKey, false)
    }

    fun updateEngineByPreset(previousSelect: EnginePreset?, currentSelected: EnginePreset?) {
//        if (previousSelect != null) {
//            selectedEngines.removeAll(previousSelect.engines.toSet())
//        }
        selectedEngines.forEach { DataSaverUtils.saveData(it.selectKey, false) }
        selectedEngines.clear()
        if (currentSelected != null) {
            addSelectedEngines(*currentSelected.engines.toSet().toTypedArray())
        }
    }

    fun cancel() {
        translateJob?.cancel()
        startedTaskNum = totalTaskNum
        finishedTaskNum = totalTaskNum
        translating = false
    }

    fun translate() {
        if (translateJob?.isActive == true) return
        if (actualTransText.isEmpty()) return

        resultList.clear()
        eachTranslateJobs.clear()
        startedTaskNum = 0
        finishedTaskNum = 0
        totalTaskNum = selectedEngines.size
        addTransHistory(actualTransText, sourceLanguage, targetLanguage)
        updateMainScreenState(MainScreenState.Translating)
        translating = true
        translateJob = viewModelScope.launch {
            // 延时，等待插件加载完
            while (!engineInitialized) {
                delay(100)
            }

            GlobalTranslationConfig.sourceLanguage = sourceLanguage
            GlobalTranslationConfig.targetLanguage = targetLanguage
            GlobalTranslationConfig.sourceString =   actualTransText
            if (AppConfig.sParallelTrans.value) {
                translateInParallel()
                Log.d(TAG, "translate: translateInParallel finished")
            } else {
                translateInSequence()
            }
            translating = false
        }
    }

    private fun addTransHistory(sourceString: String, sourceLanguage: Language, targetLanguage: Language){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transHistoryDao.insertTransHistory(
                TransHistoryBean(0, sourceString, sourceLanguage.id, targetLanguage.id, selectedEngines.map { it.name }, now())
            )
        }
    }

    fun deleteTransHistory(sourceString: String){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transHistoryDao.deleteTransHistoryByContent(sourceString)
        }
    }

    private suspend fun translateInSequence(){
        createTasks().forEach { task ->
            val job = viewModelScope.launch {
                startedTaskNum += 1
                addTranslateResultItem(task.result)
                actualTranslateTask(task)
                finishedTaskNum += 1
            }
            eachTranslateJobs.add(job to task)
            job.join()
            eachTranslateJobs.remove(job to task)
        }
    }

    private suspend fun translateInParallel() {
        val tasks: ArrayList<Deferred<*>> = arrayListOf()
        createTasks(true).also { newTasks ->
            resultList.addAll(newTasks.map { it.result })
            totalTaskNum = newTasks.size
            startedTaskNum = totalTaskNum
        }.forEach { task ->
            tasks.add(viewModelScope.async {
                actualTranslateTask(task)
                finishedTaskNum += 1
                Log.d(TAG, "translateInParallel: task ${task.result.engineName} finished, total: $finishedTaskNum/$totalTaskNum")
            })
            eachTranslateJobs.add(tasks.last() to task)
        }
        // 等待所有任务完成再返回，使对翻译状态的判断正常
        tasks.joinAll()
    }

    private suspend fun actualTranslateTask(task: CoreTextTranslationTask) {
        try {
            task.result.targetLanguage = targetLanguage
            withContext(Dispatchers.IO) {
                task.translate()
            }
            Log.d(TAG, "translate : $finishedProgress ${task.result}")
        } catch (e: TranslationException) {
            e.printStackTrace()
            task.result.error = "${ResStrings.error_result}\n${e.message}"
        } catch (e: CancellationException) {
            task.result.error = ResStrings.cancel_current_translation
            if (task.result.thinkStage == ThinkingStage.THINKING) {
                task.result.thinkStage = ThinkingStage.CANCELED
            }
            Log.d(TAG, "cancel translate ${task.result.engineName}")
            return
        } catch (e: Exception) {
            e.printStackTrace()
            task.result.error = "${ResStrings.error_result}\n${e.message}"
        } finally {
            if (task.result.stage != TranslationStage.ERROR) {
                task.result.stage = TranslationStage.FINAL_EXTRA
            }
        }
    }

    private suspend fun createTasks(withMutex: Boolean = false): List<CoreTextTranslationTask> {
        val res = LinkedList<CoreTextTranslationTask>()
        selectedEngines.sortedBy(SortResultUtils.defaultEngineSort).forEach {
            if (support(it.supportLanguages)) {
                val task = when (it) {
                    is TextTranslationEngines -> {
                        it.createTask(
                            actualTransText,
                            sourceLanguage,
                            targetLanguage
                        )
                    }

                    is ModelTranslationTask -> {
                        val modelTask = ModelTranslationTask(it.model)
                        modelTask.result.engineName = modelTask.name
                        modelTask.sourceString = actualTransText
                        modelTask.sourceLanguage = sourceLanguage
                        modelTask.targetLanguage = targetLanguage
                        modelTask
                    }

                    else -> {
                        val jsTask = it as JsTranslateTaskText
                        jsTask.result.engineName = jsTask.name
                        jsTask.sourceString = actualTransText
                        jsTask.sourceLanguage = sourceLanguage
                        jsTask.targetLanguage = targetLanguage
                        jsTask
                    }
                }
                if (withMutex) task.mutex = evalJsMutex
                res.add(task)
            } else {
                val result = TranslationResult(it.name).apply {
                    setBasicResult("当前引擎暂不支持该语种！")
                }
                addTranslateResultItem(result)
            }
        }
        return res
    }

    private suspend fun addTranslateResultItem(result: TranslationResult) {
        withContext(Dispatchers.Main) {
            resultList.let {
                val currentKey = it.find { r -> r.engineName == result.engineName }
                // 绝大多数情况下应该是没有的
                // 但是线上的报错显示有时候会有，所以判断一下吧
                if (currentKey != null) it.remove(currentKey)
                it.add(result)
                Log.d(TAG, "addTranslateResultItem: ${result.engineName}, now size: ${it.size}")
            }
        }
    }

    private fun support(supportLanguages: List<Language>) =
        supportLanguages.contains(sourceLanguage) && supportLanguages.contains(targetLanguage)


    companion object {
        private const val TAG = "MainVM"
        private var init: Boolean = false
    }
}
