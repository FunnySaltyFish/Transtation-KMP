package com.funny.translation.translate.bean

@kotlinx.serialization.Serializable
data class BuyProductResponse(
    val trade_no: String,
    val pay_url: String
)