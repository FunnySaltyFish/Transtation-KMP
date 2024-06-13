package com.funny.translation.translate.ui.settings

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.JetSettingSwitch
import com.funny.jetsetting.core.JetSettingTile
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.Log
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.ui.main.components.EngineSelectDialog
import com.funny.translation.translate.ui.main.components.UpdateSelectedEngine
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.EngineManager
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.touchToScale

@Composable
actual fun FloatWindowScreen() {
    val context = LocalContext.current
    CommonPage(title = ResStrings.float_window) {
        JetSettingSwitch(state = AppConfig.sShowFloatWindow, text = ResStrings.open_float_window) {
            try {
                if (it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideAllFloatWindow()
            } catch (e: Exception) {
                context.toastOnUi(ResStrings.failed_to_show_float_window)
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier
                .touchToScale()
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            FixedSizeIcon(Icons.Default.Info, contentDescription = null)
            Text(text = ResStrings.float_window_tip, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp))
        }

        SelectEngineTile()
//        JetSettingSwitch(
//            state = AppConfig.sFloatWindowAutoTranslate,
//            text = "自动翻译",
//            description = "悬浮窗切换语言时自动翻译",
//        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SelectEngineTile() {
    var selectEngineName by rememberDataSaverState<String>(
        key = Consts.KEY_FLOAT_WINDOW_ENGINE,
        initialValue = TextTranslationEngines.BaiduNormal.name
    )

    val showDialogState = rememberStateOf(false)

    JetSettingTile(
        text = ResStrings.float_window_translate_engine,
        description = selectEngineName,
    ) {
        showDialogState.value = true
    }

    val jsEngines by EngineManager.jsEnginesStateFlow.collectAsState(emptyList())
    val bindEngines by EngineManager.bindEnginesStateFlow.collectAsState(emptyList())
    val modelEngines by EngineManager.modelEnginesState
    val states = remember(jsEngines, bindEngines, modelEngines) {
        Log.d("FloatWindowScreen", "remember states triggered")
        hashMapOf<TranslationEngine, MutableState<Boolean>>().apply {
            bindEngines.forEach { put(it, mutableStateOf(it.name == selectEngineName)) }
            jsEngines.forEach { put(it, mutableStateOf(it.name == selectEngineName)) }
            modelEngines.forEach { put(it, mutableStateOf(it.name == selectEngineName)) }
        }
    }

    LaunchedEffect(selectEngineName) {
        states.forEach {
            it.value.value = (it.key.name == selectEngineName)
        }
        EngineManager.updateFloatWindowTranslateEngine(selectEngineName)
    }

    EngineSelectDialog(
        showDialog = showDialogState,
        bindEngines = bindEngines,
        jsEngines = jsEngines,
        modelEngines = modelEngines,
        selectStateProvider = { engine -> states[engine] ?: rememberStateOf(false) },
        updateSelectedEngine = object : UpdateSelectedEngine {
            override fun add(engine: TranslationEngine) {
                selectEngineName = engine.name
            }

            override fun remove(engine: TranslationEngine) {
                states[engine]?.value = false
            }
        }
    )
}