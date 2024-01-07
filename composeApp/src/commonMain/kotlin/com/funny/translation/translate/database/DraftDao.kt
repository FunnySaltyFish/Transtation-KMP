package com.funny.translation.translate.database


import com.funny.translation.database.Dao
import com.funny.translation.database.Delete
import com.funny.translation.database.Query
import com.funny.translation.database.Upsert
import kotlinx.coroutines.flow.Flow

typealias Draft = com.funny.translation.database.Drafts

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
    fun delete(draft: Draft)
}