package com.funny.translation.translate.database

import androidx.paging.PagingSource
import com.funny.translation.database.Dao
import com.funny.translation.database.Delete
import com.funny.translation.database.Insert
import com.funny.translation.database.Query
import com.funny.translation.database.TransFavorite
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationResult

typealias TransFavoriteBean = TransFavorite


fun fromTransResult(
    transResult: TranslationResult,
    sourceString: String,
    sourceLanguageId: Int,
) =
    TransFavoriteBean(
        id = 0,
        sourceString = sourceString,
        resultText = transResult.basic,
        sourceLanguageId = sourceLanguageId,
        targetLanguageId = transResult.targetLanguage?.id ?: Language.AUTO.id,
        engineName = transResult.engineName,
        time = System.currentTimeMillis(),
        detailText = transResult.detailText ?: ""
    )


@Dao
interface TransFavoriteDao {
    @Query("select * from table_trans_favorite order by id desc")
    fun queryAllPaging(): PagingSource<Int, TransFavoriteBean>

    @Insert
    fun insertTransFavorite(transFavoriteBean: TransFavoriteBean)

    @Delete
    fun deleteTransFavorite(id: Long)

    @Query("""select count(1) from table_trans_favorite where 
        sourceString=:sourceString and resultText=:resultText and sourceLanguageId=:sourceLanguageId 
        and targetLanguageId=:targetLanguageId and engineName=:engineName""")
    fun count(sourceString: String, resultText: String, sourceLanguageId: Int, targetLanguageId: Int, engineName: String): Int
}

