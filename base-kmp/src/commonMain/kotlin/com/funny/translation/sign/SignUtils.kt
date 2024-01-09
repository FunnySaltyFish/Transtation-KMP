package com.funny.translation.sign

import com.funny.translation.helper.ScriptEngine
import com.funny.translation.helper.readAssets
import com.funny.translation.kmp.appCtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object SignUtils {
    private var hasInitJs = false
    private const val SCRIPT_ID = "SignUtils"
    fun encodeSign(uid : Long, appVersionCode: Int, sourceLanguageCode: Int, targetLanguageCode: Int, text: String, extra: String = "") = try {
        while (!hasInitJs){
            Thread.sleep(100)
        }
        ScriptEngine.invokeFunction(
            "encode_sign",
            maxOf(uid, 0L), appVersionCode, sourceLanguageCode, targetLanguageCode, text, extra,
            scriptId = SCRIPT_ID
        ).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    /**
     * 加载签名脚本，鉴于之前发生过疑似接口盗用的情况，故而此处区分了正式发布版本和开源版本
     */
    suspend fun loadJs(){
        withContext(Dispatchers.IO){
            val jsText = tryReadAssets("funny_sign_v1_release.js").ifEmpty {
                tryReadAssets("funny_sign_v1_open_source.js")
            }
//            Log.d("loaded jsText: \n$jsText")
            if (jsText != ""){
                ScriptEngine.eval(jsText, scriptId = SCRIPT_ID)
            }
        }.also {
            hasInitJs = true
        }
    }

    private fun tryReadAssets(fileName: String): String {
        return try {
           appCtx.readAssets(fileName)
        } catch (e: Exception) {
            ""
        }
    }
}