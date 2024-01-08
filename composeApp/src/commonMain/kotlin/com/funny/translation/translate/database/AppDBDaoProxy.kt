package com.funny.translation.translate.database

import com.funny.translation.database.Database
import com.funny.translation.database.createDaoProxy
import com.funny.translation.js.JsDao


/*
    abstract val jsDao : JsDao
    abstract val transHistoryDao: TransHistoryDao
    abstract val transFavoriteDao: TransFavoriteDao
    abstract val longTextTransDao: LongTextTransDao
    abstract val draftDao: DraftDao
    abstract val chatHistoryDao: ChatHistoryDao
 */

val Database.transHistoryDao by lazy {
    createDaoProxy<TransHistoryDao>(appDB.transHistoryQueries)
}

val Database.jsDao by lazy {
    createDaoProxy<JsDao>(appDB.jsQueries)
}

val Database.transFavoriteDao by lazy {
    createDaoProxy<TransFavoriteDao>(appDB.transFavoriteQueries)
}