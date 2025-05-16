package com.funny.translation.translate.ui.engineselect


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.appSettings
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.task.ModelTranslationTask
import com.funny.translation.translate.ui.widget.TwoSectionList

@Composable
internal fun AddOrConfigurePresetDialog(
    presetProvider: () -> EnginePreset?,
    allEngines: List<TranslationEngine>,
    onDismiss: () -> Unit,
    onSave: (presetName: String, selectedEngines: List<TranslationEngine>) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val preset = presetProvider()
    var presetName by remember { mutableStateOf(preset?.name ?: "") }
    val selectedEngines = remember { mutableStateListOf<TranslationEngine>() }

    // Initialize with preset data if in edit mode
    LaunchedEffect(preset) {
        if (preset != null) {
            presetName = preset.name
            selectedEngines.clear()
            selectedEngines.addAll(preset.engines)
        } else {
            presetName = ""
            selectedEngines.clear()
        }
    }

    val availableEngines = allEngines.filter { !selectedEngines.contains(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (preset == null) ResStrings.new_preset else ResStrings.edit_preset) },
        text = {
            Column {
                // Preset name input
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(ResStrings.preset_name) },
                    placeholder = { Text(ResStrings.preset_name_hint) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Engine selection
                Text(
                    text = ResStrings.select_engines,
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = ResStrings.select_engines_description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Two-section engine list
                TwoSectionList(
                    selectedItems = selectedEngines,
                    availableItems = availableEngines,
                    maxSelectedNum = maxPresetEngineNum,
                    onItemSelected = {
                        guardSelectEngine(
                            selectedNum = selectedEngines.size,
                            maxSelectNum = appSettings.maxEngineNumEachPreset,
                            vipMaxSelectNum = appSettings.vipMaxEngineNumEachPreset,
                            toastTextFormatter = { ResStrings.tip_max_preset_engine_num_vip.format(it) },
                        ) {
                            selectedEngines.add(it)
                        }
                    },
                    onItemUnselected = { selectedEngines.remove(it) },
                    modifier = Modifier.fillMaxWidth()
                ) { engine, selected ->
                    EngineItem(engine = engine, selected = selected)
                }
            }
        },
        confirmButton = {
            Row {
                // Show delete button in edit mode
                if (preset != null && onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(ResStrings.delete, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(ResStrings.cancel)
                }

                TextButton(
                    onClick = {
                        if (presetName.isNotEmpty()) {
                            onSave(presetName, selectedEngines.toList())
                        }
                    },
                    enabled = presetName.isNotEmpty() && selectedEngines.isNotEmpty()
                ) {
                    Text(ResStrings.save)
                }
            }
        }
    )
}

@Composable
private fun EngineItem(
    engine: TranslationEngine,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (engine is ModelTranslationTask && engine.model.tag.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = engine.model.tag,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = engine.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private val maxPresetEngineNum
    get() = appSettings.vipAware(appSettings::maxEngineNumEachPreset, appSettings::vipMaxEngineNumEachPreset)