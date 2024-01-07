package com.funny.translation.translate.utils

import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.readAssetsFile
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object FunnyEachText {
    @get:Throws(IOException::class, JSONException::class)
    val words: JSONObject by lazy {
        val assetsData: String = appCtx.readAssetsFile("words.json")
        JSONObject(assetsData)
    }
}