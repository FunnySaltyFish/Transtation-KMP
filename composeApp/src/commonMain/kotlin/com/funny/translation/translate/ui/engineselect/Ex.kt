package com.funny.translation.translate.ui.engineselect

import com.funny.translation.AppConfig.isMembership
import com.funny.translation.helper.toast
import com.funny.translation.strings.ResStrings

fun guardSelectEngine(
    selectedNum: Int,
    maxSelectNum: Int,
    vipMaxSelectNum: Int,
    toastTextFormatter: (maxNum: String) -> String,
    nonVipSuffixFormatter: (vipMaxNum: String) -> String = {
        ResStrings.out_of_max_engine_limit_non_vip_suffix.format(
            it
        )
    },
    block: () -> Unit
) {
    val maxNum = if (isMembership()) vipMaxSelectNum else maxSelectNum
    if (selectedNum < maxNum) {
        block()
    } else {
        val text = toastTextFormatter(maxNum.toString())
        if (isMembership()) {
            toast(text)
        } else {
            toast(text + nonVipSuffixFormatter(vipMaxSelectNum.toString()))
        }
    }
}