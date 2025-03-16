package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.utils.ModelList
import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.ai.utils.ModelManager.currentSelectBotId
import com.funny.compose.loading.DefaultFailure
import com.funny.translation.helper.buildSearchAnnotatedString
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.HintText
import kotlinx.coroutines.delay

@Composable
fun ModelListPart(
    maxHeight: Dp = 400.dp,
) {
    Category(
        title = ResStrings.model_select,
        helpText = ResStrings.model_select_help,
        extraRowContent = {},
        defaultExpand = true,
    ) { expanded ->
        val enableModels by ModelManager.enabledModels.collectAsState()
        var searchQuery by rememberStateOf("")
        val data by produceState(emptyList(), key1 = searchQuery, key2 = enableModels) {
            this.value = enableModels.run {
                if (searchQuery.isNotEmpty()) {
                    delay(300)
                    filter { it.name.contains(searchQuery, ignoreCase = true) }
                } else this
            }
        }
        val height by animateDpAsState(targetValue = if (expanded) maxHeight else 200.dp, label = "height")
        val onClick = { model: Model ->
            ModelManager.currentSelectBotId = model.chatBotId
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(text = ResStrings.search_model, fontSize = LocalTextStyle.current.fontSize)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        FixedSizeIcon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier.clickable {
                                searchQuery = ""
                            }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (enableModels.isEmpty()) {
                DefaultFailure(
                    modifier = Modifier.fillMaxSize(),
                    retry = ModelManager::retry
                )
            } else {
                ModelList(
                    data = data,
                    height = height,
                    onClick = onClick,
                    searchQueryProvider = { searchQuery },
                    currentSelectBotId = currentSelectBotId
                )
            }
        }
    }
}

@Composable
private fun ModelList(
    data: ModelList,
    height: Dp,
    onClick: (model: Model) -> Unit,
    searchQueryProvider: () -> String,
    currentSelectBotId: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, height)
    ) {
        val search = searchQueryProvider()
        if (search.isNotEmpty() && data.isEmpty()) {
            item {
                HintText(ResStrings.no_satisfied_result)
            }
        }
        items(
            data,
            key = { it.chatBotId },
        ) {
            ListItem(
                modifier = Modifier.clickable { onClick(it) }.animateItem(),
                headlineContent = {
                    // 根据 searchQuery 高亮显示
                    Text(
                        text = buildSearchAnnotatedString(content = it.name, search = search)
                    )
                },
                supportingContent = {
                    val description = remember { it.description() }
                    Text(text = description, fontSize = 12.sp)
                },
                trailingContent = {
                    RadioButton(selected = currentSelectBotId == it.chatBotId, onClick = {
                        onClick(it)
                    })
                },
                overlineContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val inputFileTypes = it.inputFileTypes
                        if (inputFileTypes.text) {
                            FixedSizeIcon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = "Text",
                                tint = MaterialColors.BlueA400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (inputFileTypes.supportImage) {
                            FixedSizeIcon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image",
                                tint = MaterialColors.BlueA400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

fun Model.description(): String
    = "${ResStrings.currency_symbol}${ cost1kTokens} / ${ResStrings.kilo_tokens} | $description"