package com.funny.trans.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.translation.helper.Log
import com.funny.translation.helper.UserUtils.userService
import com.funny.translation.network.service.AIPointCostPagingSource
import org.json.JSONObject

class AIPointCostViewModel : ViewModel() {
    private var filter = ""
    private var sort = ""
    private var startTime: Long? = null
    private var endTime: Long? = null

    val costs = Pager(PagingConfig(pageSize = 10)) {
        AIPointCostPagingSource(userService, sort, filter, startTime, endTime)
    }.flow.cachedIn(viewModelScope)

    fun updateFilters(
        sortType: AICostSortType,
        sortOrder: Int,
        modelName: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        sort = "[(\"${sortType.value}\", $sortOrder)]"

        val filter = JSONObject()
        if (!modelName.isNullOrBlank()) {
            filter.put("model_name", JSONObject().apply {
                put("\$regex", modelName)
                put("\$options", "i")
            })
        }
        this.filter = filter.toString()
        this.startTime = startDate
        this.endTime = endDate

        Log.d(TAG, "updateFilters: sort=$sort, filter=$filter, startTime=$startTime, endTime=$endTime")
    }

    companion object {
        private const val TAG = "AIPointCostViewModel"
    }
}