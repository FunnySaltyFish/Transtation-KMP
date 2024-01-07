package com.funny.translation.translate.network.service

import retrofit2.Response
import retrofit2.http.GET

interface NoticeService {
    @GET("api/notice")
    suspend fun getNotice() : Response<Unit>
}