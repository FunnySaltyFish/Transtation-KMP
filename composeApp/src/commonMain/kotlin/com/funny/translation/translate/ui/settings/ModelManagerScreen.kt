package com.funny.translation.translate.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.ai.utils.ModelManager.enableKey
import com.funny.compose.ai.utils.ModelManager.updateSort
import com.funny.compose.ai.utils.ModelSortType
import com.funny.compose.loading.LoadingContent
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.assetsStringLocalized
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.ExpandableText
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText

@Composable
fun ModelManagerScreen() {


    // Help dialog state
    var showHelpDialog by rememberDataSaverState(
        key = "show_model_manager_help",
        initialValue = true
    )

    // Sort dialog state
    var showSortDialog by remember { mutableStateOf(false) }

    // Help dialog
    SimpleDialog(
        openDialog = showHelpDialog,
        updateOpenDialog = { showHelpDialog = it },
        content = {
            MarkdownText(
                markdown = assetsStringLocalized(name = "model_manager_help.md")
            )
        }
    )

    // Sort dialog
    if (showSortDialog) {
        SortModelDialog(onDismissRequest = { showSortDialog = false })
    }


    CommonPage(
        title = ResStrings.model_manager,
        actions = {
            IconButton(onClick = { showSortDialog = true }) {
                FixedSizeIcon(Icons.Default.Sort, contentDescription = ResStrings.sort)
            }
            IconButton(onClick = { showHelpDialog = true }) {
                FixedSizeIcon(Icons.Default.Help, contentDescription = ResStrings.help)
            }
        }
    ) {
        val state by ModelManager.modelState.collectAsState()

        DisposableEffect(Unit) {
            // 更新模型列表
            onDispose {
                ModelManager.refresh()
            }
        }

        LoadingContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            retry = ModelManager::retry
        ) { models ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(models.second, key = { it.name }) { model ->
                    ModelCard(modifier = Modifier.animateItem(), model = model)
                }
            }
        }
    }
}

@Composable
fun ModelCard(modifier: Modifier, model: Model) {
    var isEnabled by rememberDataSaverState(
        key = model.enableKey,
        initialValue = true
    )

    Card(
        modifier = modifier.fillMaxWidth().clipToBounds().clickable {
           isEnabled = !isEnabled
        }.animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (model.tag.isNotEmpty()) {
                        Badge {
                            Text(model.tag)
                        }
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExpandableText(
                text = model.description,
                collapsedMaxLine = 2,
                expandStateText = ResStrings.expand_text,
                collapseStateText = ResStrings.collapse_text,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (model.isFree) Icons.Default.FreeBreakfast else Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (model.isFree) ResStrings.free else "${model.cost1kTokens} / 1K Tokens",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Token,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val inputFiles = model.inputFileTypes
                if (inputFiles.text) {
                    Text(
                        text = ResStrings.text,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (inputFiles.supportImage) {
                    Text(
                        text = " / " + ResStrings.image,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
internal fun SortModelDialog(
    onDismissRequest: () -> Unit,
) {
    var sortType by rememberDataSaverState(
        key = "model_sort_type",
        initialValue = ModelSortType.DEFAULT
    )
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(ResStrings.sort_models) },
        text = {
            Column {
                val pairs = remember {
                    listOf(
                        ModelSortType.DEFAULT to ResStrings.sort_by_default,
                        ModelSortType.NAME_ASC to ResStrings.sort_by_name_asc,
                        ModelSortType.NAME_DESC to ResStrings.sort_by_name_desc,
                        ModelSortType.COST_ASC to ResStrings.sort_by_cost_asc,
                        ModelSortType.COST_DESC to ResStrings.sort_by_cost_desc
                    )
                }
                pairs.forEach {
                    key(it.second) {
                        ListItem(
                            modifier = Modifier.clickable {
                                sortType = it.first
                                updateSort(it.first)
                            },
                            headlineContent = { Text(it.second) },
                            leadingContent = {
                                RadioButton(
                                    selected = it.first == sortType,
                                    onClick = { updateSort(it.first) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(ResStrings.confirm)
            }
        }
    )
}