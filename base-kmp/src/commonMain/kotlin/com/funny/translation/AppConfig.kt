package com.funny.translation

import androidx.annotation.Keep
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.ClickClipboardHintAction
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.bean.TranslationConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.kmp.base.strings.ResStrings
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
    val sFloatWindowAutoTranslate = mutableDataSaverStateOf(DataSaverUtils, "KEY_FLOAT_WINDOW_AUTO_TRANSLATE", true)
    val sDefaultSourceLanguage = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEFAULT_SOURCE_LANGUAGE", Language.AUTO)
    val sDefaultTargetLanguage = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEFAULT_TARGET_LANGUAGE", Language.CHINESE)
    val sAITransExplain = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_AI_TRANS_EXPLAIN, true)
    val sAIImageTransSystemPrompt = mutableDataSaverStateOf(DataSaverUtils, "KEY_AI_IMAGE_TRANS_SYSTEM_PROMPT",
        EditablePrompt(
            prefix = ResStrings.default_ai_image_trans_system_prompt,
            suffix = "\n\nYour output must be a valid JSON with two keys: `source` indicates the source text and `target` indicates the translated result.\nExample output: {\"source\":\"Hello World\",\"target\":\"你好，世界\"}"
        )
    )
    val sClickClipboardHintAction = mutableDataSaverStateOf(DataSaverUtils, "KEY_CLICK_CLIPBOARD_HINT_ACTION", ClickClipboardHintAction.InputText)


    // 以下为Pro专享
    val sParallelTrans = mutableDataSaverStateOf(DataSaverUtils, "KEY_PARALLEL_TRANS", false)
    val sShowDetailResult = mutableDataSaverStateOf(DataSaverUtils, "KEY_SHOW_DETAIL_RESULT", false)
    val sExpandDetailByDefault = mutableDataSaverStateOf(DataSaverUtils, "KEY_EXPAND_DETAIL_BY_DEFAULT", false)

    //
    val developerMode = mutableDataSaverStateOf(DataSaverUtils, "KEY_DEVELOPER_MODE", false)

    fun updateJwtToken(newToken: String) {
        userInfo.value = userInfo.value.copy(jwt_token = newToken)
    }

    /**
     * 减去点数，优先从 vip_free_ai_point 中扣，没有就从 ai_point 中扣
     *
     * @param amount
     */
    fun subAITextPoint(amount: BigDecimal) {
        if (amount == BigDecimal.ZERO) return
        val user = userInfo.value

        if (user.vip_free_ai_point >= amount) {
            userInfo.value = user.copy(vip_free_ai_point = user.vip_free_ai_point - amount)
        } else {
            userInfo.value = user.copy(
                ai_point = user.ai_point - amount + user.vip_free_ai_point,
                vip_free_ai_point = BigDecimal.ZERO
            )
        }
    }

    fun addAITextPoint(amount: BigDecimal) {
        if (amount == BigDecimal.ZERO) return
        val user = userInfo.value
        userInfo.value = user.copy(ai_point = user.ai_point + amount)
    }


    // 强制内联此方法，避免被反编译绕过
    inline fun isMembership() = userInfo.value.isValidVip()

    // 老铁，你看到了，这是你破解的方法。译站已经是开源了，我个人开发整这个会员只是为了支撑服务器成本啊，我好心，你也别破坏了。
    @Keep
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

val isGooglePlayBuild by lazy {
    BuildConfig.FLAVOR == "google"
}

val isCommonBuild by lazy {
    BuildConfig.FLAVOR == "common"
}