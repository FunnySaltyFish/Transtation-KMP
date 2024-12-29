package com.funny.translation.translate.network.service

import com.funny.translation.helper.now
import com.funny.translation.network.CommonData
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class LLMAnalyzeResult(
    val llmTotalTimes: Int = 0,
    val llmTotalReadTimes: Int = 0,
    val llmTotalInputToken: Int = 0,
    val llmTotalOutputToken: Int = 0
) {
    fun isAllZero() = llmTotalTimes == 0 && llmTotalReadTimes == 0 && llmTotalInputToken == 0 && llmTotalOutputToken == 0
}

interface AnalyzeService {
    /**
     * # 2024 年度报告
     * @bp_analyze.route("/get_llm_data", methods=["GET"])
     * async def get_llm_data_route():
     *     from .utils import get_llm_data
     *     """
     *     获取 LLM 数据的 API 路由
     *     """
     *     # 获取请求参数
     *     uid = request.args.get("uid")
     *     start_time = int(request.args.get("startTime", 0))
     *     end_time = int(request.args.get("endTime"), datetime.now().timestamp() * 1000)
     *
     *     # 调用方法获取数据
     *     result = get_llm_data(uid, start_time, end_time)
     *
     *     # 返回 JSON 响应
     *     return resp_ok(result)
     */
    @GET("analyze/get_llm_data")
    suspend fun getLLMData(
        @Query("uid") uid: Int,
        @Query("startTime") startTime: Long = 0,
        @Query("endTime") endTime: Long = now()
    ): CommonData<LLMAnalyzeResult>
}