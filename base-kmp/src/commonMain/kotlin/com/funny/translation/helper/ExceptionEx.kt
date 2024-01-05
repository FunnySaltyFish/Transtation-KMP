package com.funny.translation.helper

import com.funny.translation.kmp.base.strings.ResStrings

fun Exception.displayMsg(action: String = "")
    = message ?: (action + ResStrings.failed_unknown_err)