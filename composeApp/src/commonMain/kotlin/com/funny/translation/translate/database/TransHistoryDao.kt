package com.funny.translation.translate.database

import androidx.paging.PagingSource
import com.funny.translation.database.Dao
import com.funny.translation.database.Insert
import com.funny.translation.database.Query
import com.funny.translation.database.TransHistory

typealias TransHistoryBean = TransHistory

@Dao
interface TransHistoryDao {
//    @Query("select * from table_trans_history limit :size offset (:page * :size)")
//    fun queryPaged(page: Int = 0, size: Int = 10) : PagingSource<Int, TransHistoryBean>

    @Query("select * from table_trans_history where id in " +
            "(select max(id) as id from table_trans_history group by sourceString) order by id desc")
    fun queryAllPaging(): PagingSource<Int, TransHistoryBean>

    @Query("delete from table_trans_history where id = :id")
    fun deleteTransHistory(id: Int)

    @Query("delete from table_trans_history where sourceString = :sourceString")
    fun deleteTransHistoryByContent(sourceString: String)

    @Insert
    fun insertTransHistory(transHistoryBean: TransHistoryBean)

    @Query("select * from table_trans_history where time between :startTime and :endTime")
    fun queryAllBetween(startTime: Long, endTime: Long): List<TransHistoryBean>

    @Query("delete from table_trans_history")
    fun clearAll()
}

abstract class TransHistoryDaoImpl: TransHistoryDao