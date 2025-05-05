package com.funny.translation.translate.ui.engineselect

import androidx.compose.runtime.MutableState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.task.ModelTask
import com.funny.translation.translate.utils.EngineManager

typealias PresetPair = Pair<String, List<String>>

// 简单的预设数据类
data class EnginePreset(
    val name: String,
    val engines: List<TranslationEngine>
)

object PresetManager {
    var presets: MutableState<List<PresetPair>> = mutableDataSaverStateOf(DataSaverUtils, "KEY_TEXT_ENGINE_PRESETS", listOf())

    /**
     * 根据预设名称获取对应引擎列表
     *
     * @param presetName 预设名称
     * @return 如果找到对应的预设，则返回包含引擎列表的EnginePreset对象，否则返回null
     */
    fun getPresetEngine(presetName: String?): EnginePreset? {
        if (presetName == null) return null

        val presetPair = presets.value.find { it.first == presetName }
        if (presetPair == null) return null

        val engines = presetPair.second.mapNotNull { findEngineByPreset(it) }
        return EnginePreset(presetName, engines)
    }

    /**
     * 根据预设Key查找对应的引擎
     *
     * @param presetKey 预设Key
     * @return 找到的引擎，如果找不到则返回null
     */

    fun findEngineByPreset(presetKey: String): TranslationEngine? {
        val engine = when {
            presetKey.startsWith("bind_") -> EngineManager.bindEnginesStateFlow.value.find { presetKey.removePrefix("bind_") == it.name }
            presetKey.startsWith("plugin_") -> EngineManager.jsEnginesStateFlow.value.find { presetKey.removePrefix("plugin_") == it.name }
            presetKey.startsWith("model_") -> EngineManager.modelEnginesState.value.find { presetKey.removePrefix("model_").toInt() == it.model.chatBotId }
            else -> null
        }
        return engine
    }

    /**
     * 更新或创建新的预设。如果旧的预设名称不为空且存在，则会尝试保留其在列表中的位置
     *
     * @param oldName 旧的预设名称，如果为null则表示创建新的预设
     * @param presetName 新的预设名称
     * @param selectedEngine 选定的引擎列表
     */
    fun updateOrCreatePreset(oldName: String?, presetName: String, selectedEngine: List<TranslationEngine>) {
        val newPresetPair = presetName to selectedEngine.map { it.presetKey }
        val newPresets = presets.value.toMutableList()

        val oldIndex = if (oldName != null) newPresets.indexOfFirst { it.first == oldName } else -1
        val oldPreset = if (oldName != null) newPresets.find { it.first == oldName } else null


        if (oldIndex != -1) {
            // 更新
            Log.d("PresetManager", "updateOrCreatePreset: replace ${oldPreset} to ${newPresetPair}")
            newPresets[oldIndex] = newPresetPair

            // 如果新名称与旧名称不同，移除旧的预设
            if (presetName != oldName) {
                Log.d("PresetManager", "updateOrCreatePreset: remove $oldPreset, it is rename")
                newPresets.remove(oldPreset)
            }
        } else {
            // 新建
            Log.d("PresetManager", "updateOrCreatePreset: add new ${newPresetPair}")
            newPresets.add(newPresetPair)
        }

        presets.value = newPresets.toList()
    }
}

val TranslationEngine.presetKey: String
    get() = when (this) {
        is TextTranslationEngines -> "bind_$name"
        is JsTranslateTaskText -> "plugin_$name"
        is ModelTask -> "model_${model.chatBotId}"
        else -> "Unknown"
    }