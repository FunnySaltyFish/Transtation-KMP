package com.funny.translation.codeeditor.ui.runner

import androidx.compose.runtime.mutableStateOf
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.debug.Debug
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTaskText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.viewModelScope

class CodeRunnerViewModel : BaseViewModel(),
    Debug.DebugTarget {
    private var jsEngine: JsEngine? = null

    val outputDebug = mutableStateOf("执行代码以查看输出结果")

    fun initJs(activityCodeViewModel: ActivityCodeViewModel,code: String) {
        //Log.d(TAG, "initJs: code:$code")
        viewModelScope.launch(Dispatchers.IO) {
            jsEngine = JsEngine(code).apply {
                loadBasicConfigurations(
                    onSuccess = {
                        val jsTranslateTask = JsTranslateTaskText(
                            jsEngine = this,
                        ).apply {
                            sourceLanguage = activityCodeViewModel.sourceLanguage.value
                            targetLanguage = activityCodeViewModel.targetLanguage.value
                            sourceString = activityCodeViewModel.sourceString.value
                        }
                        with(GlobalTranslationConfig) {
                            sourceString = activityCodeViewModel.sourceString.value
                            sourceLanguage = activityCodeViewModel.sourceLanguage.value
                            targetLanguage = activityCodeViewModel.targetLanguage.value
                        }
                        viewModelScope.launch(Dispatchers.IO) { jsTranslateTask.translate() }
                    }
                ) {
                    it.printStackTrace()
                }
            }
        }

    }

    fun clearDebug(){
        outputDebug.value = ""
    }

    init {
        Debug.addTarget(this)
    }

    companion object{
        const val TAG = "CodeRunnerVM"
    }

    override val source: String
        get() = "插件"

    override fun appendLog(text: CharSequence) {
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                outputDebug.value = "${outputDebug.value}\n$text"
            }
        }
    }
}