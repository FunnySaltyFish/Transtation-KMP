package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.Consts
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.task.ModelTranslationTask
import com.funny.translation.translate.utils.EngineManager
import com.funny.translation.translate.utils.ModelManagerAction
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModelScope

class FloatWindowScreenModel: BaseViewModel() {
    private val localSelectEngineName = DataSaverUtils.readData(Consts.KEY_FLOAT_WINDOW_ENGINE, TextTranslationEngines.BaiduNormal.name)

    val bindEnginesFlow get() = EngineManager.bindEnginesStateFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val jsEnginesFlow : Flow<List<JsTranslateTaskText>> get() = EngineManager.jsEnginesStateFlow

    var modelEngines by mutableStateOf(persistentListOf<ModelTranslationTask>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            EngineManager.addObserver {
                when(it) {
                    is ModelManagerAction.OneEngineInitialized -> {

                    }
                    is ModelManagerAction.AllEnginesInitialized -> {
                        // do nothing
                        Log.d(TAG, "AllEnginesInitialized")
                    }
                }
            }

        }
    }


    companion object {
        private const val TAG = "FloatWindowScreenModel"
    }
}