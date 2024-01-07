package com.funny.translation.js.config

class JsConfig {
    companion object{
        val DEBUG_DIVIDER = "=" * 18
        const val JS_ENGINE_VERSION = 7
    }
}

private operator fun String.times(times: Int): String {
    val stringBuilder = StringBuilder()
    for(i in 0 until times){
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}
