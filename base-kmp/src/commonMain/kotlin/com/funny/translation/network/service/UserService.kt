package com.funny.translation.network.service

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.funny.translation.AppConfig
import com.funny.translation.bean.Price
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.AICostTypeSerializer
import com.funny.translation.helper.DateSerializerType1
import com.funny.translation.helper.Log
import com.funny.translation.helper.PriceSerializer
import com.funny.translation.network.CommonData
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Date

@Serializable
data class InvitedUser(
    val uid: Int,
    val register_time: String
)

/**
 * class AICostType(Enum):
 *     AskOrTranslate = "AskOrTranslate"
 *     ImageTranslate = "ImageTranslate"
 *     TTS = "TTS"
 */
@Serializable(AICostTypeSerializer::class)
enum class AICostType(val type: String) {
    AskOrTranslate("AskOrTranslate"),
    ImageTranslate("ImageTranslate"),
    TTS("TTS")
}

@Serializable
data class AIPointCost(
    val uid: Int,
    @Serializable(with = PriceSerializer::class)
    val cost: Price,
    val model_name: String,
    val input_tokens: Int,
    val output_tokens: Int,
    @Serializable(with = PriceSerializer::class)
    val model_price: Price,
    @Serializable(with = DateSerializerType1::class)
    val time: Date,
    // cost_type:
    // - AskOrTranslate
    val cost_type: AICostType
)

interface UserService {
    @POST("user/verify_email")
    @FormUrlEncoded
    suspend fun verifyEmail(
        @Field("email") email: String,
        @Field("verify_code") verifyCode: String
    ): CommonData<Unit>

    @POST("user/send_verify_email")
    @FormUrlEncoded
    suspend fun sendVerifyEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>

    // sendFindUsernameEmail
    @POST("user/send_find_username_email")
    @FormUrlEncoded
    suspend fun sendFindUsernameEmail(
        @Field("email") email: String
    ): CommonData<Unit>

    // sendResetPasswordEmail
    @POST("user/send_reset_password_email")
    @FormUrlEncoded
    suspend fun sendResetPasswordEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>

    // fun sendCancelAccountEmail(username: String, email: String)
    @POST("user/send_cancel_account_email")
    @FormUrlEncoded
    suspend fun sendCancelAccountEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>


    @POST("user/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_type") passwordType: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("invite_code") inviteCode: String
    ): CommonData<Unit>

    @POST("user/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_type") passwordType: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("verify_code") verifyCode: String,
        @Field("did") did: String
    ): CommonData<UserInfoBean>

    @POST("user/logout")
    @FormUrlEncoded
    // uid: Int, did: String
    suspend fun logout(
        @Field("uid") uid: Int,
        @Field("did") did: String
    ): CommonData<Unit>

    @POST("user/get_user_info")
    @FormUrlEncoded
    suspend fun getInfo(
        @Field("uid") uid: Int
    ): CommonData<UserInfoBean>

    @POST("user/get_user_email")
    @FormUrlEncoded
    suspend fun getUserEmail(
        @Field("username") username: String
    ): CommonData<String>

    @POST("user/refresh_token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("uid") uid: Int
    ): CommonData<String>

    @POST("user/change_avatar")
    suspend fun uploadAvatar(
        @Body body: MultipartBody
    ): CommonData<String>

    // resetPassword
    @POST("user/reset_password")
    @FormUrlEncoded
    suspend fun resetPassword(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): CommonData<Unit>

    // findUsername
    @POST("user/find_username_by_email")
    @FormUrlEncoded
    suspend fun findUsername(
        @Field("email") email: String,
        @Field("code") code: String
    ): CommonData<List<String>>

    // changeUsername
    @POST("user/change_username")
    @FormUrlEncoded
    suspend fun changeUsername(
        @Field("uid") uid: Int,
        @Field("new_username") username: String,
    ): CommonData<Unit>

    // cancelUser
    @POST("user/cancel_account")
    @FormUrlEncoded
    suspend fun cancelAccount(
        @Field("verify_code") verifyCode: String,
    ): CommonData<Unit>

    // generateInviteCode
    @POST("user/generate_invite_code")
    suspend fun generateInviteCode(): CommonData<String>

    // getInviteUsers
    @POST("user/get_invite_users")
    suspend fun getInviteUsers(): CommonData<List<InvitedUser>>

    /**
     * 获取 AI 点数消耗记录
     */
    @GET("user/get_ai_point_consume_records")
    suspend fun getAiPointConsumeRecords(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String? = null,
        @Query("filter") filter: String? = null,
        @Query("start_time") startTime: Long?,
        @Query("end_time") endTime: Long?,
        @Query("uid") uid: Int = AppConfig.uid
    ): CommonData<List<AIPointCost>>
}

class AIPointCostPagingSource(
    private val service: UserService,
    private val sort: String,
    private val filter: String?,
    private val startTime: Long?,
    private val endTime: Long?
) : PagingSource<Int, AIPointCost>() {
    companion object {
        private const val TAG = "AIPointCostPagingSource"
    }

    override fun getRefreshKey(state: PagingState<Int, AIPointCost>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AIPointCost> {
        return try {
            val nextPage = params.key ?: 0
            val data = service.getAiPointConsumeRecords(
                page = nextPage,
                size = params.loadSize,
                sort = sort,
                filter = filter,
                startTime = startTime,
                endTime = endTime
            )
            if (data.isSuccess) {
                Log.d(TAG, "load: $nextPage ${data.data?.size}")
                LoadResult.Page(
                    data = data.getOrDefault(emptyList()),
                    prevKey = if (nextPage == 0) null else nextPage - 1,
                    nextKey = if (data.data?.isNotEmpty() == true) nextPage + 1 else null
                )
            } else {
                return LoadResult.Error(Exception(data.displayErrorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}