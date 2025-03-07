package com.funny.translation.translate.ui.long_text

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.Log
import com.funny.translation.helper.TextSplitter
import com.funny.translation.helper.createFileIfNotExist
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.now
import com.funny.translation.helper.safeSubstring
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.database.LongTextTransTask
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.longTextTransDao
import com.funny.translation.translate.ui.ai.ModelViewModel
import com.funny.translation.translate.ui.long_text.bean.TermList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


val DEFAULT_PROMPT_PREFIX = ResStrings.default_long_text_trans_prompt_prefix
val DEFAULT_PROMPT_SUFFIX = ResStrings.default_long_text_prompt_suffix

private val DEFAULT_PROMPT = EditablePrompt(DEFAULT_PROMPT_PREFIX, DEFAULT_PROMPT_SUFFIX)

private const val SEP = "||sep||"


internal enum class ScreenState {
    Init, Translating, Result
}

class LongTextTransViewModel: ModelViewModel() {
    private val dao = appDB.longTextTransDao
    internal var task: LongTextTransTask? by mutableStateOf(null)
    internal var screenState by mutableStateOf(ScreenState.Init)

    private var totalLength = 0
    var translatedLength by mutableIntStateOf(0)
    var maxSegmentLength: Int? by mutableDataSaverStateOf(DataSaverUtils, key = "long_text_max_segment_length", initialValue = null)

    val progress by derivedStateOf {  if (translatedLength == 0) 0f else (translatedLength.toFloat() / totalLength).coerceIn(0f, 1f) }
    val startedProgress by derivedStateOf {  if (translatedLength == 0) 0f else ((translatedLength + currentTransPartLength).toFloat() / totalLength).coerceIn(0f, 1f) }

    private var translateJob: Job? = null
    private var dbJob: Job? = null
    // 是否正在编辑术语，是的话暂停一下翻译
    private var isEditingTerm: Boolean = false
    // 是否暂停
    var isPausing by mutableStateOf(false)

    var transId = UUID.randomUUID().toString()
    internal var prompt by mutableStateOf(DEFAULT_PROMPT)
    private var memory = ChatMemoryFixedMsgLength(2)

    val allCorpus = TermList()
    val currentCorpus = TermList()
    var sourceText by mutableStateOf("")
    var resultText by mutableStateOf("")

    var currentTransPartLength by mutableStateOf(0) // 当前翻译的长度
    val currentResultStartOffset get() = lastResultText.length

    // 源文本翻译时的每一段结束位置，每一个值为该段的最后一个字符的索引
    val sourceTextSegments = mutableListOf<Int>()
    // 翻译结果的每一段结束位置，每一个值为该段的最后一个字符的索引
    val resultTextSegments = mutableListOf<Int>()

    // 当前翻译的这一段源文本
    private var currentPart = ""
    // 当前 part 翻译得到的结果
    private var currentOutput = StringBuilder()
    // 已经完成的 parts 翻译得到的结果
    private var lastResultText = ""
    // 因各种问题导致的失败次数
    var errorTimes by mutableIntStateOf(0)

    private val record = true // 仅在调试时使用，记录所有的翻译输入与输出
    // 包括
    // {
    //      "sourceText": "XiaoHong and XiaoMing are studying DB class.",
    //      “process": [
    //          { “endIndex": 5, "output": ["{", "{\"text\", ... , ] },
    //          { “endIndex": 10, "output": ["{", "{\"text\", ... , ] },
    //       ]
    // }
    private val recordObj = JSONObject()
    private var recordProcess = JSONArray()
    private var recordOutput = JSONArray()

    fun initArgs(id:String) {
        this.transId = id

        val v = DataHolder.get<String>(id)
        // TODO only for test
        if (v.isNullOrBlank()) {
            // 如果没有传，那么从数据库中加载
            viewModelScope.launch(Dispatchers.IO) {
                task = dao.getById(id)
                task?.let {
                    sourceText = it.sourceText
                    resultText = it.resultText
                    lastResultText = resultText
                    translatedLength = it.translatedLength
                    prompt = it.prompt
                    totalLength = it.sourceText.length
                    // TODO chatBot 选择
                    allCorpus.addAll(it.allCorpus)
                    sourceTextSegments.addAll(it.sourceTextSegments)
                    resultTextSegments.addAll(it.resultTextSegments)

                    if (translatedLength > 0) {
                        if (translatedLength >= totalLength) {
                            screenState = ScreenState.Result
                        } else {
                            screenState = ScreenState.Translating
                            isPausing = true
                            lastResultText = resultText
                        }
                    }
                }
            }
        } else {
            this.sourceText = v
            this.totalLength = this.sourceText.length
            DataHolder.remove(id)
        }

    }

    fun startTranslate() {
        screenState = ScreenState.Translating
        if (record) {
            recordObj.put("sourceText", sourceText)
        }
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                while (isActive && translatedLength < totalLength) {
                    if (isEditingTerm) {
                        isPausing = true
                        appCtx.toastOnUi(ResStrings.paused_due_to_editting)
                    }
                    while (isPausing) { delay(100) }

                    // 防止最后一段刚好是 \n||sep||，然后把 ||sep|| 当做关键部分了
                    lastResultText = lastResultText.removeSuffix("\n" + SEP)
                    // 前文的最后一小点，供上下文衔接
                    val prevEnd = if (lastResultText.isNotEmpty()) {
                        "..." + lastResultText.takeLast(100)
                    } else ""
                    val (part, messages) = getNextPart(prevEnd)
                    Log.d(TAG, "startTranslate: nextPart: $part")
                    if (part == "") break
                    translatePart(part, messages)
                }
                delay(500)
                screenState = ScreenState.Result
                if (record) {
                    recordObj.put("process", recordProcess)
                    saveRecord()
                }
                Log.d(TAG, "finishTranslate, sourceStringSegments: $sourceTextSegments, resultTextSegments: $resultTextSegments")
            } catch (e: Exception) {
                Log.e(TAG, "startTranslate: ", e)
                appCtx.toastOnUi(e.displayMsg(ResStrings.translate))
            }
        }
    }

    fun generateBothExportedText(): String {
        // 根据两个 Segments，生成源文本、翻译结果对照的文本
        if (sourceTextSegments.size != resultTextSegments.size) {
            Log.e(TAG, "generateBothExportedText: sourceTextSegments.size != resultTextSegments.size")
            return ""
        }
        return buildString {
            var lastSourceIndex = 0
            var lastResultIndex = 0
            for (i in sourceTextSegments.indices) {
                val sourceIndex = sourceTextSegments[i]
                val resultIndex = resultTextSegments[i]
                append(sourceText.substring(lastSourceIndex, sourceIndex + 1))
                append("\n")
                append(resultText.substring(lastResultIndex, resultIndex + 1))
                append("\n\n")
                lastSourceIndex = sourceIndex + 1
                lastResultIndex = resultIndex + 1
            }
        }
    }

    /**
     * 获取下一次要翻译的部分
     * 规则：
     * chatBot.maxContextLength 是一次能接受的最大长度
     * 它包括 SystemPrompt + 这次输入 format 后 + 输出的长度 +
     * @return String
     */
    private suspend fun getNextPart(prevEnd: String): Pair<String, ArrayList<ChatMessageReq>> {
        if (translatedLength >= totalLength) return "" to arrayListOf()

        // 最大的输入长度： 模型最大长度 * 0.8
        // 由于模型的输出长度实际远小于上下文长度（gpt3.5、gpt4都只有4096），这里乘以 0.8 以尽量使得输出能输出完
        val maxLength = maxSegmentLength ?: (chatBot.model.maxOutputTokens * 0.8f).toInt()
        val tokenCounter = chatBot.tokenCounter

        val messages = arrayListOf<ChatMessageReq>()
        // 把上次翻译的最后一点加上
        if (prevEnd.isNotEmpty()) {
            messages.add(ChatMessageReq.text(prevEnd, "assistant"))
        }

        val remainText = sourceText.safeSubstring(translatedLength, translatedLength + maxLength)
        Log.d(TAG, "getNextPart: remainText: ${remainText.abstract()}")
        val text = tokenCounter.truncate(remainText, emptyArray(), maxLength).let {
            TextSplitter.cutTextNaturally(it)
        }
        Log.d(TAG, "getNextPart: truncated text: ${text.abstract()}")

        val sb = StringBuilder(text)

        // 寻找当前的 corpus
        // TODO 改成更合理的方式，比如基于 NLP 的分词
        currentCorpus.clear()
        val needToAddTerms = mutableSetOf<Term>()
        allCorpus.list.forEach {
            if (text.contains(it.first)) {
                needToAddTerms.add(it)
            }
        }
        currentCorpus.addAll(needToAddTerms)
        Log.d(TAG, "getNextPart: allCorpus: $allCorpus, currentCorpus: $currentCorpus")

        sb.append(SEP)
        val list = currentCorpus.list.toList().map { it.toList() }
        sb.append(JSONArray(list).toString())

        messages.add(ChatMessageReq.text(sb.toString(), "user"))
        return text to messages
    }

    private suspend fun translatePart(part: String, messages: List<ChatMessageReq>) {
        /**
         * 处理错误相关的逻辑，返回值为是否达到了最大值而弹出 toast
         * @return Boolean
         */
        fun onError(): Boolean {
            errorTimes++
            if (errorTimes == 3) {
                isPausing = true
                appCtx.toastOnUi(ResStrings.translate_paused_too_many_retries)
                translateJob?.cancel()
                translateJob = null
                return true
            }
            return false
        }


        currentOutput.clear()
        currentPart = part
        currentTransPartLength = part.length

        val systemPrompt = prompt.toPrompt()
        val maxOutputTokens = chatBot.model.maxOutputTokens
        val chatMessages = messages.map { newChatMessage(it.role, it.content.toString()) }
        val args = mapOf("max_tokens" to maxOutputTokens)
        chatBot.chat(chatMessages, systemPrompt, memory, args).collect { streamMsg ->
            when(streamMsg) {
                is StreamMessage.Start -> {
                    if (record) {
                        recordOutput = JSONArray()
                    }
                }
                is StreamMessage.Part -> {
                    currentOutput.append(streamMsg.part)
                    val ans = parseStreamedOutput(currentOutput.toString())
                    resultText = lastResultText + ans

                    if (record) {
                        recordOutput.put(streamMsg.part)
                    }
                }
                is StreamMessage.End -> {
                    // 解析 keywords
                    val idx = currentOutput.lastIndexOf(SEP)
                    if (idx != -1) {
                        try {
                            val json = currentOutput.substring(idx+SEP.length).removeSuffix("}")
                            val list = try {
                                JsonX.fromJson<List<List<String>>>(json)
                            } catch (e: SerializationException) {
                                // ChatGLM 会生成 {"aaa", "bbb"] 这种东西
                                JsonX.fromJson<List<List<String>>>(json.replace('{', '['))
                            }
                            val keywords =
                                (list.filter { it.size == 2 } as? List<List<String>>)?.map { lst ->
                                    lst.map { it.trim() }
                                }
                            keywords?.forEach {
                                // 判定一下，一定是源文本 to 翻译后文本，有时候大模型会把他们反过来
                                if (it[0] in currentPart) {
                                    allCorpus.upsert(it[0] to it[1])
                                } else if (it[1] in currentPart) {
                                    allCorpus.upsert(it[1] to it[0])
                                }
                            }
                        } catch (e: Exception) {
                            onError()
                            Log.e(TAG, "translatePart: ", e)
                        }
                    }

                    translatedLength += part.length
                    lastResultText = resultText

                    sourceTextSegments.add(translatedLength - 1)
                    resultTextSegments.add(lastResultText.length - 1)

                    saveToDB()

                    if (record) {
                        recordProcess.put(JSONObject().apply {
                            put("endIndex", translatedLength - 1)
                            put("output", recordOutput)
                        })
                    }
                }
                is StreamMessage.Error -> {
                    if (!onError()) {
                        appCtx.toastOnUi(streamMsg.error)
                        delay(1000)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun saveToDB() {
        dbAction {
            task = LongTextTransTask(
                id = transId,
                chatBotId = chatBot.id,
                sourceText = sourceText,
                resultText = resultText,
                prompt = prompt,
                allCorpus = allCorpus.toList(),
                sourceTextSegments = sourceTextSegments,
                resultTextSegments = resultTextSegments,
                translatedLength = translatedLength,
                remark = task?.remark ?: "",
                createTime = task?.createTime ?: now(),
                updateTime = now()
            )
            dao.upsert(task!!)
        }
    }

    private inline fun dbAction(crossinline action: () -> Unit) {
        dbJob?.cancel()
        dbJob = viewModelScope.launch(Dispatchers.IO) {
            action()
        }
    }


    /**
     * 尝试解析流式的输出，返回前 ||sep|| 前面的
     * @param text String
     */
    private fun parseStreamedOutput(text: String): String {
        val idx = text.lastIndexOf(SEP)
        if (idx == -1 || idx == text.length - SEP.length) {
            return text
        }

        return text.substring(0, idx)
    }

    private fun newChatMessage(sender: String, msg: String): ChatMessage {
        return ChatMessage(botId = chatBot.id, conversationId = transId, sender = if (sender == "user") SENDER_ME else sender, content = msg)
    }

    fun updatePrompt(prefix: String) { prompt = prompt.copy(prefix = prefix) }
    fun resetPrompt() { prompt = DEFAULT_PROMPT }
    fun savePrompt() {
        task ?: return
        dbAction {
            task = task!!.copy(prompt = prompt)
            appDB.longTextTransTasksQueries.updatePrompt(prompt = prompt, id = task!!.id)
        }
    }
    fun updateEditingTermState(isEditing: Boolean) { isEditingTerm = isEditing }
    fun updateSourceText(text: String) { sourceText = text; totalLength = text.length }

    fun updateRemark(taskId: String, remark: String) {
        task ?: return
        dbAction {
            task = task!!.copy(remark = remark)
            appDB.longTextTransTasksQueries.updateRemark(id = taskId, remark = remark)
        }
    }

    fun toggleIsPausing() {
        isPausing = !isPausing
        if (isPausing) appCtx.toastOnUi(ResStrings.paused_tip)
        else {
            if (translateJob == null) {
                // 如果没有开始翻译（从外部加载进来的状态），那么开始翻译
                startTranslate()
            }
            errorTimes = 0
        }
    }

    fun retryCurrentPart() {
        isPausing = false
        errorTimes = 0
        startTranslate()
    }

    private fun saveRecord() {
        if (record) {
            viewModelScope.launch(Dispatchers.IO) {
                val file = CacheManager.cacheDir.resolve("long_text_trans_records/record_${System.currentTimeMillis()}.json")
                file.createFileIfNotExist()
                file.writeText(recordObj.toString(2))
            }
        }
    }

    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}

private fun String.abstract() =
    if (length > 30) substring(0, 15) + "..." + takeLast(15) else this + "(${length})"