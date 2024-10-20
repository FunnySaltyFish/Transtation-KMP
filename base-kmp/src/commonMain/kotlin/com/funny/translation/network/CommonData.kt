package com.funny.translation.network

import androidx.annotation.Keep
import com.funny.translation.kmp.base.strings.ResStrings

@Keep
@kotlinx.serialization.Serializable
data class CommonData<T>(val code: Int, val message: String? = null, val data: T? = null, val error_msg:String? = null) {
    val displayErrorMsg get() = error_msg ?: message ?: ResStrings.unknown_error
    val isSuccess = code == CODE_SUCCESS

    fun getOrDefault(default: T): T = if (code == CODE_SUCCESS) data ?: default else default
}