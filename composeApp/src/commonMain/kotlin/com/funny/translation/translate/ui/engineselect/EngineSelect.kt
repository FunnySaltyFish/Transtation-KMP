package com.funny.translation.translate.ui.engineselect

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.appSettings
import com.funny.translation.bean.rememberRef
import com.funny.translation.helper.Log
import com.funny.translation.helper.rememberSaveableStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.ui.settings.SortModelDialog
import com.funny.translation.ui.HintText
import com.funny.translation.ui.dialog.AnyPopDialog
import com.funny.translation.ui.dialog.rememberAnyPopDialogState
import com.funny.translation.ui.popDialogShape

private const val TAG = "EngineSelectDialog"

// 用于选择引擎时的回调
interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun EngineSelectDialog(
    showDialog: MutableState<Boolean>,
    bindEngines: List<TranslationEngine>,
    jsEngines: List<TranslationEngine>,
    modelEngines: List<TranslationEngine>,
    selectStateProvider: (TranslationEngine) -> Boolean,
    updateSelectedEngine: UpdateSelectedEngine,
    showPreset: Boolean = false,
    onPresetClicked: (previousSelect: EnginePreset?, currentSelected: EnginePreset?) -> Unit = { _, _ -> }
) {
    var showEngineSelect by showDialog
    val state = rememberAnyPopDialogState()

    LaunchedEffect(showEngineSelect) {
        Log.d(TAG, "showEngineSelect: $showEngineSelect")
        if (showEngineSelect) {
            state.animateShow()
        } else {
            state.animateHide()
        }
    }

    AnyPopDialog(
        modifier = Modifier
            .popDialogShape()
            .heightIn(max = 640.dp)
            .padding(top = 8.dp, bottom = 8.dp),
        state = state,
        onDismissRequest = {
            showEngineSelect = false
        }
    ) {
        EngineSelect(
            modifier = Modifier.fillMaxWidth(),
            bindEngines,
            jsEngines,
            modelEngines,
            selectStateProvider,
            updateSelectedEngine,
            showPreset,
            onPresetClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
internal fun EngineSelect(
    modifier: Modifier = Modifier,
    bindEngines: List<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    modelEngines: List<TranslationEngine> = arrayListOf(),
    selectStateProvider: (TranslationEngine) -> Boolean,
    updateSelectEngine: UpdateSelectedEngine,
    showPreset: Boolean = false,
    onPresetClicked: (previousSelect: EnginePreset?, currentSelected: EnginePreset?) -> Unit = { _, _ -> }
) {
    // 状态管理
    var query by rememberSaveableStateOf("")
    var showSortDialog by remember { mutableStateOf(false) }
    var editPreset: EnginePreset? by rememberRef(null)
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var selectedPresetName: String? by rememberDataSaverState<String?>(
        key = "select_preset_name",
        initialValue = null
    )

    val presets by PresetManager.presets

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        EngineSearchBar(
            queryProvider = { query },
            onQueryChange = { query = it },
        )

        // 主内容区域
        if (showPreset) {
            // 预设区域标题
            TitleRow(modifier = Modifier.padding(top = 4.dp), title = "我的预设") {
                IconButton(onClick = {
                    guardSelectEngine(
                        selectedNum = presets.size,
                        maxSelectNum = appSettings.maxPresetNum,
                        vipMaxSelectNum = appSettings.vipMaxPresetNum,
                        toastTextFormatter = {
                            "您最多只能有${it}组预设"
                        }
                    ) {

                        editPreset = null
                        showAddPresetDialog = true
                    }
                }) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = "添加预设",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (presets.isEmpty()) {
                    item {
                        HintText(ResStrings.preset_hint, modifier = Modifier.fillParentMaxWidth())
                    }
                } else {
                    itemsIndexed(presets) { i, (presetName, _) ->
                        // 预设卡片
                        PresetChip(
                            presetName = presetName,
                            selectedPresetName = selectedPresetName,
                            updateSelectedPresetName = { selectedPresetName = it },
                            onPresetClicked = onPresetClicked,
                            showEditDialogAction = {
                                guardSelectEngine(
                                    selectedNum = i,
                                    maxSelectNum = appSettings.maxPresetNum,
                                    vipMaxSelectNum = appSettings.vipMaxPresetNum,
                                    toastTextFormatter = {
                                        "您最多只能有${it}组预设，当前预设无法编辑"
                                    }
                                ) {
                                    editPreset = it
                                    showAddPresetDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        // 大模型区域标题
        TitleRow(title = ResStrings.model_engine) {
            IconButton(onClick = { /* 过滤逻辑 */ }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "过滤",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showSortDialog = true }) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "排序和设置",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 模型引擎区域
        ModelsFlowGrid(
            engines = modelEngines,
            selectStateProvider = selectStateProvider,
            updateSelectEngine = updateSelectEngine
        )

        // 内置引擎区域标题
        TitleRow(modifier = Modifier.padding(vertical = 4.dp), title = ResStrings.bind_engine)

        // 内置引擎网格
        ModelsFlowGrid(
            engines = bindEngines,
            selectStateProvider = selectStateProvider,
            updateSelectEngine = updateSelectEngine
        )

        // 插件
        if (jsEngines.isNotEmpty()) {
            TitleRow(modifier = Modifier.padding(vertical = 4.dp), title = ResStrings.plugin_engine)
            ModelsFlowGrid(
                engines = jsEngines,
                selectStateProvider = selectStateProvider,
                updateSelectEngine = updateSelectEngine
            )
        }
    }

    // 添加预设对话框
    if (showAddPresetDialog) {
        AddOrConfigurePresetDialog(
            presetProvider = { editPreset },
            allEngines = listOf(bindEngines, jsEngines, modelEngines).flatten(),
            onDismiss = { showAddPresetDialog = false },
            onSave = { presetName, selectedEngines ->
                // 保存预设逻辑
                showAddPresetDialog = false
                PresetManager.updateOrCreatePreset(editPreset?.name, presetName, selectedEngines)
                if (selectedPresetName == presetName) {
                    // 如果正好编辑选中的，则立刻生效
                    onPresetClicked(editPreset, PresetManager.getPresetEngine(presetName))
                }
            },
            onDelete = {
                // 删除预设逻辑
                showAddPresetDialog = false
                PresetManager.deletePreset(editPreset?.name)
                if (selectedPresetName == editPreset?.name) {
                    // 如果正好编辑选中的，则立刻生效
                    onPresetClicked(editPreset, null)
                }
            }
        )
    }

    // 排序对话框
    if (showSortDialog) {
        SortModelDialog(onDismissRequest = { showSortDialog = false })
    }
}

@Composable
private fun TitleRow(
    modifier: Modifier = Modifier,
    title: String,
    extrasContent: @Composable RowScope.() -> Unit = { }
) {
    // 大模型区域标题
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )

        if (extrasContent != {}) {
            Row(
                modifier = Modifier.offset(x = 12.dp) // 向右移动一点，让 icon button 和上面其他元素对齐
            ) {
                extrasContent()
            }
        }
    }
}

@Composable
private fun PresetChip(
    presetName: String,
    selectedPresetName: String?,
    updateSelectedPresetName: (String?) -> Unit,
    onPresetClicked: (previousSelect: EnginePreset?, currentSelected: EnginePreset?) -> Unit,
    showEditDialogAction: (preset: EnginePreset?) -> Unit
) {
    val currentSelected = (selectedPresetName == presetName)
    FilterChip(
        selected = currentSelected,
        onClick = {
            val previousSelect = PresetManager.getPresetEngine(selectedPresetName)
            updateSelectedPresetName(if (currentSelected) null else presetName)
            val currentSelect = if (currentSelected) null else PresetManager.getPresetEngine(presetName)
            onPresetClicked(previousSelect, currentSelect)
        },
        label = {
            Text(
                text = presetName,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier.size(24.dp).offset((-8).dp),
                onClick = {
                    showEditDialogAction(PresetManager.getPresetEngine(presetName))
                }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑预设",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelsFlowGrid(
    engines: List<TranslationEngine>,
    selectStateProvider: (TranslationEngine) -> Boolean,
    updateSelectEngine: UpdateSelectedEngine
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp),
        verticalArrangement = spacedBy((-8).dp)
    ) {
        engines.forEach { engine ->
            key(engine.name) {
                SelectEngineChip(
                    engine = engine,
                    selectStateProvider = selectStateProvider,
                    updateSelectEngine = updateSelectEngine
                )
            }
        }
    }
}



