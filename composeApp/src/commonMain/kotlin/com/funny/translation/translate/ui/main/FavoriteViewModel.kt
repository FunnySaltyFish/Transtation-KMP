package com.funny.translation.translate.ui.main

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.cash.sqldelight.paging3.QueryPagingSource
import com.funny.translation.translate.database.TransFavoriteBean
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.transFavoriteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class FavoriteViewModel: ViewModel() {
    val transFavorites by lazy {
        Pager(PagingConfig(pageSize = 10)) {
            val queries = appDB.transFavoriteQueries
//            QueryPagingSource(
//                countQuery = queries.countAll(),
//                transacter = queries,
//                context = Dispatchers.IO,
//                queryProvider = queries::queryAllPaging,
//            )
            QueryPagingSource(
                countQuery = queries.countAll(),
                transacter = queries,
                context = Dispatchers.IO,
                queryProvider = queries::queryAllPaging,
            )
        }.flow.cachedIn(viewModelScope)
    }

    fun deleteTransFavorite(bean: TransFavoriteBean){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transFavoriteDao.deleteTransFavorite(bean.id)
        }
    }
}