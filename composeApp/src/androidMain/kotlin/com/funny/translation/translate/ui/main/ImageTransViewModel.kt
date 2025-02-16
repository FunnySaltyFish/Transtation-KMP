package com.funny.translation.translate.ui.main

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.askAndParseStream
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.extractJSON
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.ImageTranslationPart
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.ui.ai.ModelViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.LinkedList

enum class ImageTransPage {
    Main, ResultList
}

class MultiIndexedImageTranslationPart(val indexes: IntArray, val part: ImageTranslationPart)

typealias SingleIndexedImageTranslationPart = Pair<Int, ImageTranslationPart>

class ImageTransViewModel : ModelViewModel() {
    var imageUri: Uri? by mutableStateOf(null)
    var translateEngine: ImageTranslationEngine by mutableStateOf(ImageTranslationEngines.Baidu)
    private var translateJob: Job? = null
    var translateState: LoadingState<ImageTranslationResult> by mutableStateOf(LoadingState.Loading)

    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_target_lang", Language.CHINESE)

    var imgWidth = 0
    var imgHeight = 0

    var allEngines = arrayListOf(ImageTranslationEngines.Baidu, ImageTranslationEngines.Tencent)

    var showResultState = mutableStateOf(true)
    var showTranslateButton by mutableStateOf(true)

    // 下面是处理结果的相关
    var selectedResultParts = mutableStateListOf<SingleIndexedImageTranslationPart>()
    var optimizeByAITask: OptimizeByAITask? by mutableStateOf(null)

    init {
        translateEngine = DefaultData.bindImageEngines.firstOrNull {
            DataSaverUtils.readData(it.selectKey, false)
        } ?: ImageTranslationEngines.Baidu

        // mock data
//        translateState = LoadingState.Success(
//            ImageTranslationResult(
//                source = "source",
//                target = "target",
//                content = listOf(
//                    ImageTranslationPart("白日依山尽", "The white sun sets behind the mountains"),
//                    ImageTranslationPart("黄河入海流", "The Yellow River flows into the sea"),
//                    ImageTranslationPart("欲穷千里目", "If you want to see a thousand miles"),
//                    ImageTranslationPart("更上一层楼", "Climb to a higher level")
//                )
//            )
//        )
    }

    // 翻译相关
    fun translate() {
        imageUri ?: return
        translateJob?.cancel()
        Log.d(TAG, "translate: start")
        showTranslateButton = false
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            translateState = LoadingState.Loading
            kotlin.runCatching {
                val bytes = BitmapUtil.getBitmapFromUri(
                    appCtx,
                    4096,
                    4096,
                    2 * 1024 * 1024,
                    imageUri!!.toString()
                )
                bytes ?: return@launch
                Log.d(TAG, "translate: imageSize: ${bytes.size}")
                with(GlobalTranslationConfig) {
                    this.sourceLanguage = this@ImageTransViewModel.sourceLanguage
                    this.targetLanguage = this@ImageTransViewModel.targetLanguage
                    this.sourceString = ""
                }
                translateEngine.createTask(bytes, sourceLanguage, targetLanguage).apply {
                    this.translate()
                }.result
            }.onSuccess {
                val user = AppConfig.userInfo.value
                if (user.img_remain_points > 0)
                    AppConfig.userInfo.value =
                        user.copy(img_remain_points = user.img_remain_points - translateEngine.getPoint())
                translateState = LoadingState.Success(it)
                showTranslateButton = false
                // 翻译成功了，把结果展示出来
                showResultState.value = true
            }.onFailure {
                it.printStackTrace()
                translateState = LoadingState.Failure(it)
                appCtx.toastOnUi("翻译错误！原因是：${it.message}")
                showTranslateButton = true
            }
        }
    }

    fun cancelTranslateJob() {
        translateJob?.cancel()
    }

    // 处理结果相关
    fun updateSelectedResultParts(index: Int, part: ImageTranslationPart, newSelectState: Boolean) {
        if (newSelectState) {
            selectedResultParts.add(index to part)
        } else {
            selectedResultParts.remove(index to part)
        }
    }

    fun clearSelectedResultParts() {
        selectedResultParts.clear()
    }

    fun selectAllResultParts() {
        selectedResultParts.clear()
        selectedResultParts.addAll(
            translateState.getOrNull<ImageTranslationResult>()?.content?.mapIndexed { i, v -> i to v }
                ?: emptyList()
        )
    }

    fun optimizeByAI() {
        val selectedParts = selectedResultParts.toList()
        if (selectedParts.isEmpty()) {
            appCtx.toastOnUi("请先选择需要优化的部分")
            return
        }
        optimizeByAITask = OptimizeByAITask(chatBot.model, selectedParts, viewModelScope)
        optimizeByAITask?.start()
    }

    fun cancelOptimizeByAI() {
        optimizeByAITask?.cancel()
        optimizeByAITask = null
    }

    /**
     * 替换选中的部分
     * @param newParts 新的部分，key 是原始的 id 数组，value 是新的 ImageTranslationPart
     */
    fun replaceSelectedParts(newParts: List<MultiIndexedImageTranslationPart>) {
        val oldResult = translateState.getOrNull<ImageTranslationResult>() ?: return
        val oldResultList = oldResult.content.toMutableList()
        val result = LinkedList<ImageTranslationPart>()
        var newSource = ""
        var newTarget = ""

        val needToRemove = mutableSetOf<ImageTranslationPart>()
        newParts.forEach { newPart ->
            var addPart: ImageTranslationPart? = null
            for (idx in newPart.indexes) {
                val oldPart = oldResultList[idx]
                needToRemove.add(oldPart)
                // 不断合并新的部分，得到新的矩形框
                addPart = addPart?.combineWith(oldPart) ?: oldPart
            }
            addPart?.let {
                result.add(it.copy(source = newPart.part.source, target = newPart.part.target))
            }
        }

        // 加上那些没有被替换的部分
        result.addAll(oldResultList - needToRemove)

        // 按从坐上到右下的顺序排列
        result.sortBy { it.y * imgWidth + it.x }

        // 重新生成 source 和 target
        newSource = result.joinToString("\n") { it.source }
        newTarget = result.joinToString("\n") { it.target }

        translateState = LoadingState.Success(
            oldResult.copy(content = result, source = newSource, target = newTarget)
        )
        // 优化任务完成，清空
        optimizeByAITask = null
    }

    // 一些状态
    fun isTranslating() = translateJob?.isActive == true
    fun isOptimizing() = optimizeByAITask?.job?.isActive == true

    fun updateImageUri(uri: Uri?) {
        imageUri = uri
    }

    fun updateSourceLanguage(language: Language) {
        sourceLanguage = language
    }

    fun updateTargetLanguage(language: Language) {
        targetLanguage = language
    }

    fun updateImgSize(w: Int, h: Int) {
        imgWidth = w; imgHeight = h
    }

    fun updateShowTranslateButton(show: Boolean) {
        showTranslateButton = show
    }

    fun updateTranslateEngine(new: ImageTranslationEngine) {
        if (translateEngine != new) {
            DataSaverUtils.saveData(translateEngine.selectKey, false)
            translateEngine = new
            DataSaverUtils.saveData(new.selectKey, true)
        }
    }

    companion object {
        private const val TAG = "ImageTransVM"
    }
}

class OptimizeByAITask(
    private val model: Model,
    private val selectedParts: List<SingleIndexedImageTranslationPart>,
    private val scope: CoroutineScope
) {
    var job: Job? = null
    // 优化结果: key 是原始的 id 数组，value 是优化后的 ImageTranslationPart
    val loadingState = mutableStateOf<LoadingState<List<MultiIndexedImageTranslationPart>>>(LoadingState.Loading)
    var aiJobGeneratedText by mutableStateOf("")

    fun cancel() {
        job?.cancel()
    }

    fun start() {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            val input = selectedParts.map { (i, part) ->
                mapOf(
                    "id" to i,
                    "source" to part.source,
                    "target" to part.target
                )
            }.run {
                JSONArray(this)
            }

            val req = AskStreamRequest(
                modelId = model.chatBotId,
                prompt = SYSTEM_PROMPT,
                messages = listOf(
                    ChatMessageReq.text(
                        input.toString()
                    )
                )
            )
            aiService.askAndParseStream(req, model).onStart {
                loadingState.value = LoadingState.Loading
            }.collect { it ->
                when (it) {
                    is StreamMessage.Error -> {
                        loadingState.value = LoadingState.Failure(Exception(it.error))
                    }
                    is StreamMessage.Part -> {
                        aiJobGeneratedText += it.part
                    }
                    is StreamMessage.End -> {
                        loadingState.value = runCatching {
                            val result = JSONArray(aiJobGeneratedText.extractJSON())
                            val list = mutableListOf<MultiIndexedImageTranslationPart>()
                            for (i in 0 until result.length()) {
                                val each = result.getJSONObject(i)
                                val ids = each.getJSONArray("id").run {
                                    IntArray(length()).also { arr ->
                                        for (j in 0 until length()) {
                                            arr[j] = getInt(j)
                                        }
                                    }
                                }
                                val source = each["source"] as String
                                val target = each["target"] as String
                                list.add(MultiIndexedImageTranslationPart(ids, ImageTranslationPart(source, target)))
                            }
                            list
                        }.fold(
                            onSuccess = { map -> LoadingState.Success(map) },
                            onFailure = { err -> LoadingState.Failure(err) }
                        )
                    }
                    else -> Unit
                }
            }

        }
    }

    companion object {
        private const val TAG = "OptimizeByAIJob"

        @org.intellij.lang.annotations.Language("Markdown")
        private const val SYSTEM_PROMPT = """You're doing some post-processing for image translation. Given a list of text boxes containing both original and translated text, your task is to correct any errors in text recognition and translation, you should also combine incorrect split text boxes if necessary. Give only the changed parts.

**Input Format:**
[
    {
        "id": 1,
        "source": "You're my dea r est friend",
        "target": "你是我dea r的朋友"
    },
    {
        "id": 2,
        "source": "I'm going to the sto",
        "target": "我要去sto"
    },
    {
        "id": 3,
        "source": "re to buy some milk",
        "target": "买一些牛奶"
    },
    {
        "id": 4,
        "source": "This is correct",
        "target": "这是正确的"
    },
    ...
]

**Output Format:**
[
    {
        "id": [1], // array of original text box ids
        "source": "You're my dearest friend", // corrected original text
        "target": "你是我最好的朋友" // corrected translated text
    },
    {
        "id": [2, 3], // combined original text box ids
        "source": "I'm going to the store to buy some milk",
        "target": "我要去商店买一些牛奶"
    },
    // 4 is correct, so it's not included in the output
    ...
]

You output must be a valid JSON array.
"""
    }
}