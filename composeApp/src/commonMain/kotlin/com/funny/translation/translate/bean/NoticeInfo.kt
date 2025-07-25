package com.funny.translation.translate.bean

import androidx.annotation.Keep
import com.funny.translation.helper.DateSerializerType1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Keep
@Serializable
data class NoticeInfo(
    val message: String,
    @Serializable(with = DateSerializerType1::class) val date: Date,
    val url: String?,
    @SerialName("allow_dismiss_forever") val allowDismissForever: Boolean = true,
)
