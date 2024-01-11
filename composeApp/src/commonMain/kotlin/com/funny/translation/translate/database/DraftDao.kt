package com.funny.translation.translate.database


import com.funny.translation.database.Dao
import com.funny.translation.database.Delete
import com.funny.translation.database.Query
import com.funny.translation.database.Upsert
import com.funny.translation.helper.now
import kotlinx.coroutines.flow.Flow

typealias Draft = com.funny.translation.database.Drafts

// 不加 Timestamp 的函数
fun Draft(
    id: Int,
    content: String,
    remark: String,
) = Draft(id, content, now(), remark)

@Dao
interface DraftDao {
    // getAll
    @Query("select * from table_drafts order by timestamp desc")
    fun getAll(): Flow<List<Draft>>

    // upsert
    @Upsert
    fun upsert(draft: Draft)

    // delete
    @Delete
    fun delete(draftId: Int)
}