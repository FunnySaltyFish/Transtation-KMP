package com.funny.translation.translate

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.funny.translation.AppConfig
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.UserUtils
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import com.funny.translation.translate.bean.NoticeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.util.Date


class ActivityViewModel : ViewModel(), LifecycleObserver {

    var lastBackTime: Long = 0
    var noticeInfo: MutableState<NoticeInfo?> = mutableStateOf(null)

    var userInfo by AppConfig.userInfo
    val uid by derivedStateOf { userInfo.uid }
    val token by derivedStateOf { userInfo.jwt_token }

    // 用于 Composable 跨层级直接观察 Activity 生命周期，实现方法有点奇特
    val activityLifecycleState = MutableSharedFlow<Lifecycle.State>()

    companion object {
        const val TAG = "ActivityVM"
    }

    init {
        getNotice()
    }

    fun refreshUserInfo() {
        if (userInfo.isValid()) {
            viewModelScope.launch {
                api(UserUtils.userService::getInfo, uid) {
                    success {
                        it.data?.let {  user -> AppConfig.login(user) }
                    }
                }
            }
        }
    }

    fun getNotice() {
        viewModelScope.launch (Dispatchers.IO) {
            kotlin.runCatching {
                val jsonBody = OkHttpUtils.get("${ServiceCreator.BASE_URL}/api/notice")
                if (jsonBody != "") {
                    withContext(Dispatchers.Main) {
                        noticeInfo.value = JsonX.fromJson(json = jsonBody, NoticeInfo::class)
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    noticeInfo.value = NoticeInfo(
                        "获取公告失败，如果网络连接没问题，则服务器可能崩了，请告知开发者修复……",
                        Date(),
                        null
                    )
                }
                it.printStackTrace()
            }
        }
    }

    override fun onStateChanged(state: Lifecycle.State) {
        viewModelScope.launch {
            // Log.d(TAG, "onStateChanged: emit $event")
            // 等待 Composable 订阅，以避免 Composable 未订阅时发送的事件丢失
            while (activityLifecycleState.subscriptionCount.value == 0) {
                delay(100)
            }
            activityLifecycleState.emit(state)
        }
    }
}