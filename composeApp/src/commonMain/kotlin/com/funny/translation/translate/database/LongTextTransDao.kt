package com.funny.translation.translate.database

import com.funny.translation.bean.EditablePrompt
import com.funny.translation.database.Dao
import com.funny.translation.database.Insert
import com.funny.translation.database.LongTextTransTasks
import com.funny.translation.database.Query
import com.funny.translation.database.Upsert
import kotlinx.coroutines.flow.Flow

private fun now() = System.currentTimeMillis()

typealias LongTextTransTask = LongTextTransTasks

fun LongTextTransTask(
    id: String,
    chatBotId: Int,
    sourceText: String,
    resultText: String,
    prompt: EditablePrompt,
    allCorpus: List<Pair<String, String>>,
    sourceTextSegments: List<Int>,
    resultTextSegments: List<Int>,
    translatedLength: Int,
) = LongTextTransTask(
    id = id,
    chatBotId = chatBotId,
    sourceText = sourceText,
    resultText = resultText,
    prompt = prompt,
    allCorpus = allCorpus,
    sourceTextSegments = sourceTextSegments,
    resultTextSegments = resultTextSegments,
    translatedLength = translatedLength,
    createTime = now(),
    updateTime = now(),
    remark = "",
)


val LongTextTransTask.finishTranslating: Boolean
    get() = translatedLength == sourceText.length

val LongTextTransTask.translatedProgress
    get() = (translatedLength.toFloat() / sourceText.length.toFloat()).coerceIn(0f, 1f)

// 不带具体文本，只包括少量信息的 LongTextTransTask，以加快列表时的查询速度
data class LongTextTransTaskMini(
    val id: String,
    val chatBotId: Int,
    val translatedLength: Int = 0,
    val remark: String = "",
    val createTime: Long = now(),
    val updateTime: Long = now(),

    val sourceTextLength: Int,
) {
    val finishTranslating: Boolean
        get() = translatedLength >= sourceTextLength

    val translatedProgress
        get() = (translatedLength.toFloat() / sourceTextLength.toFloat()).coerceIn(0f, 1f)
}

@Dao
interface LongTextTransDao {
    @Query("select * from table_long_text_trans_tasks where id = :id")
    fun getById(id: String): LongTextTransTask?

    @Query("select * from table_long_text_trans_tasks")
    fun getAll(): Flow<List<LongTextTransTask>>

    @Query("select id, chatBotId, translatedLength, remark, createTime, updateTime, length(sourceText) as `sourceTextLength` from table_long_text_trans_tasks")
    fun getAllMini(): Flow<List<LongTextTransTaskMini>>

    @Query("delete from table_long_text_trans_tasks where id = :id")
    fun deleteById(id: String)

    @Upsert
    fun upsert(task: LongTextTransTask)

    @Insert
    fun insert(task: LongTextTransTask)

    // updateAllCorpus
    @Query("update table_long_text_trans_tasks set allCorpus = :allCorpus where id = :id")
    fun updateAllCorpus(id: String, allCorpus: List<Pair<String, String>>)

    @Query("update table_long_text_trans_tasks set sourceText = :text where id = :id")
    fun updateSourceText(id: String, text: String)

    @Query("update table_long_text_trans_tasks set remark = :remark where id = :id")
    fun updateRemark(id: String, remark: String)
}