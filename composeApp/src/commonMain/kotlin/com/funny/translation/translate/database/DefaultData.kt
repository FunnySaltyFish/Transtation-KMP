package com.funny.translation.translate.database

import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.engine.NormalImageTranslationEngines
import com.funny.translation.translate.engine.TextTranslationEngines

object DefaultData {
    private val TAG = "DefaultData"

    fun isPluginBound(jsBean: JsBean) = bindEngines.any { it.name == jsBean.fileName }

    val bindEngines = listOf(
        TextTranslationEngines.BaiduNormal,
        TextTranslationEngines.Tencent,
        // TextTranslationEngines.Youdao,
        TextTranslationEngines.Jinshan,

        TextTranslationEngines.BiggerText,
        TextTranslationEngines.EachText,
        TextTranslationEngines.Bv2Av
    )

    val bindImageEngines = listOf(NormalImageTranslationEngines.Baidu, NormalImageTranslationEngines.Tencent)
}