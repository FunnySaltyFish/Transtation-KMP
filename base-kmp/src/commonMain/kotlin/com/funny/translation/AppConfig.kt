package com.funny.translation

import androidx.annotation.Keep
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.TranslationConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.Language
import com.funny.translation.ui.theme.ThemeConfig
import java.math.BigDecimal

private const val TAG = "AppConfig"
internal expect fun getDid(): String
internal expect fun getVersionCode(): Int
internal expect fun getVersionName(): String

@Keep
object AppConfig {

    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0

    var userInfo = mutableDataSaverStateOf(DataSaverUtils, "user_info", UserInfoBean())
    val uid by derivedStateOf { userInfo.value.uid }
    val jwtToken by derivedStateOf { userInfo.value.jwt_token }

    val versionCode: Int = getVersionCode()
    val versionName: String = getVersionName()

    // 隐私合规，延迟获取
    val androidId: String by lazy { getDid() }

    // 下面为可设置的状态
    val sTextMenuFloatingWindow = mutableDataSaverStateOf(DataSaverUtils, "KEY_TEXT_MENU_FLOATING_WINDOW", false)
    val sSpringFestivalTheme = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SPRING_THEME, true)
    val sEnterToTranslate = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_ENTER_TO_TRANSLATE, false)
    val sHideBottomNavBar = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_CRASH_MSG, false)
    val sAutoFocus = mutableDataSaverStateOf(DataSaverUtils, "KEY_AUTO_FOCUS", false)
    val sShowFloatWindow = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SHOW_FLOAT_WINDOW, false)
    val sDefaultSourceLanguage = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEFAULT_SOURCE_LANGUAGE", Language.AUTO)
    val sDefaultTargetLanguage = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEFAULT_TARGET_LANGUAGE", Language.CHINESE)

    // 以下为Pro专享
    val sParallelTrans = mutableDataSaverStateOf(DataSaverUtils, "KEY_PARALLEL_TRANS", false)
    val sShowDetailResult = mutableDataSaverStateOf(DataSaverUtils, "KEY_SHOW_DETAIL_RESULT", false)
    val sExpandDetailByDefault = mutableDataSaverStateOf(DataSaverUtils, "KEY_EXPAND_DETAIL_BY_DEFAULT", false)

    //
    val developerMode = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEVELOPER_MODE", false)

    fun updateJwtToken(newToken: String) {
        userInfo.value = userInfo.value.copy(jwt_token = newToken)
    }

    fun subAITextPoint(amount: BigDecimal) {
        if (amount == BigDecimal.ZERO) return
        val user = userInfo.value
        userInfo.value = user.copy(ai_text_point = user.ai_text_point - amount)
    }

    fun subAIVoicePoint(amount: BigDecimal) {
        if (amount == BigDecimal.ZERO) return
        val user = userInfo.value
        userInfo.value = user.copy(ai_voice_point = user.ai_voice_point - amount)
    }

    fun isVip() = userInfo.value.isValidVip()

    // 开启 VIP 的一些功能，供体验
    fun enableVipFeatures(){
        sParallelTrans.value = true
        sShowDetailResult.value = true
    }

    private fun disableVipFeatures(){
        sParallelTrans.value = false
        sShowDetailResult.value = false
        ThemeConfig.updateThemeType(ThemeConfig.defaultThemeType)
    }

    fun logout(){
        userInfo.value = UserInfoBean()
        disableVipFeatures()
    }

    fun login(userInfoBean: UserInfoBean, updateVipFeatures: Boolean = false) {
        userInfo.value = userInfoBean
        if (updateVipFeatures) {
            if (userInfoBean.isValidVip()) enableVipFeatures()
            else disableVipFeatures()
        }
    }
}

val GlobalTranslationConfig = TranslationConfig()
// 外部 intent 导致，表示待会儿需要做翻译
// 不用 DeepLink
var NeedToTransConfig by mutableStateOf(TranslationConfig())