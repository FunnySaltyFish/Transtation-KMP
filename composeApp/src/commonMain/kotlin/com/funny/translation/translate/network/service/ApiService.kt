package com.funny.translation.translate.network.service

import com.funny.translation.bean.AppSettings
import com.funny.translation.network.CommonData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/get_app_settings")
    suspend fun getAppSettings(
        @Query("uid") uid: Int // 区分一下 uid，避免后期出现要跟用户相关的设置
    ) : CommonData<AppSettings>
}