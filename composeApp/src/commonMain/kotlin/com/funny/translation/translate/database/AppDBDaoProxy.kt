package com.funny.translation.translate.database

import com.funny.translation.database.Database
import com.funny.translation.database.createDaoProxy


/*
    abstract val jsDao : JsDao
    abstract val transHistoryDao: TransHistoryDao
    abstract val transFavoriteDao: TransFavoriteDao
    abstract val longTextTransDao: LongTextTransDao
    abstract val draftDao: DraftDao
    abstract val chatHistoryDao: ChatHistoryDao
 */

val Database.transHistoryDao by lazy {
    createDaoProxy<TransHistoryDao>(appDB.transHistoryQueries, TransHistoryBean::class.java)
}