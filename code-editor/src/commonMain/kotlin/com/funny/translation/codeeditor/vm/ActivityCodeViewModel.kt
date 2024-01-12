package com.funny.translation.codeeditor.vm

import androidx.compose.runtime.mutableStateOf
import com.eygraber.uri.Uri
import com.funny.translation.codeeditor.bean.Content
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.lazyPromise
import com.funny.translation.helper.readAssets
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.Language
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModelScope

class ActivityCodeViewModel : BaseViewModel() {
    private val TAG = "ActivityCodeVM"

    val codeState by lazy {
        mutableStateOf(Content(""))
    }

    var shouldExecuteCode = mutableStateOf(false)

    private val _initialCode by lazyPromise(viewModelScope) {
        try{
            appCtx.readAssets("js_template.js")
        }catch (e : Exception){
            "读取文件失败！${e.message}"
        }
    }

    var openFileUri: Uri = Uri.EMPTY

    val sourceString   = mutableStateOf("你好")
    val sourceLanguage = mutableStateOf(Language.CHINESE)
    val targetLanguage = mutableStateOf(Language.ENGLISH)

    var exportText = ""

    val allLanguageNames : List<String>
        get() {
            return Language.values().map { it.name }
        }

    private fun updateCode(str : String){
        codeState.value = Content(str)
    }

    init {
        viewModelScope.launch {
            updateCode(_initialCode.await())
        }
    }
}