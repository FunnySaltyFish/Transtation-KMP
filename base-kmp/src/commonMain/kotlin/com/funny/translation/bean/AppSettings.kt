package com.funny.translation.bean

import com.funny.translation.AppConfig
import com.funny.translation.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty0

/**
 * 服务器下发的应用设置
 */
@Serializable
data class AppSettings(
    @SerialName("max_select_text_engines_num") // 最大可选择的文本引擎数目
    val maxSelectTextEnginesNum: Int = 5,
    @SerialName("vip_max_select_text_engines_num")
    val vipMaxSelectTextEnginesNum: Int = 8,
    @SerialName("max_preset_num") // 最大可创建的预设数目
    val maxPresetNum: Int = if (BuildConfig.DEBUG) 3 else 1,
    @SerialName("vip_max_preset_num") // 会员最大可创建的预设数目
    val vipMaxPresetNum: Int = 8,
    @SerialName("max_engine_num_each_preset")
    val maxEngineNumEachPreset: Int = maxSelectTextEnginesNum, // 每个预设最大可选择的引擎数目
    @SerialName("vip_max_engine_num_each_preset")
    val vipMaxEngineNumEachPreset: Int = vipMaxSelectTextEnginesNum, // 会员每个预设最大可选择的引擎数目
) {
    fun <T> vipAware(common: KProperty0<T>, vip: KProperty0<T>): T = if (AppConfig.isMembership()) vip.get() else common.get()
}
