package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.ModelListPart(
    onModelLoaded: (currentSelectBotId: Int, models: List<Model>) -> Unit,
    onModelSelected: (model: Model) -> Unit,
    maxHeight: Dp = 400.dp,
) {


    Category(
        title = ResStrings.model_select,
        helpText = ResStrings.model_select_help,
        extraRowContent = {},
        defaultExpand = true,
    ) { expanded ->
        val (state, retry) = rememberRetryableLoadingState(loader = {
            ModelManager.models.await()
        })
        var searchQuery by rememberStateOf("")
        val data by produceState(emptyList(), key1 = searchQuery) {
            value = state.value.getOrDefault<List<Model>>(emptyList()).run {
                if (searchQuery.isNotEmpty()) {
                    delay(300)
                    this.filter { it.name.contains(searchQuery, ignoreCase = true) }
                } else this
            }
        }
        var currentSelectBotId by rememberDataSaverState("selected_chat_model_id", initialValue = 0)
        val height by animateDpAsState(targetValue = if (expanded) maxHeight else 200.dp, label = "height")
        val onClick = { model: Model ->
            currentSelectBotId = model.chatBotId
            onModelSelected(model)
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
//            var query by rememberStateOf("")
//            var active by rememberStateOf(false)
//            SearchBar(
//                query = query,
//                onQueryChange = { query = it },
//                modifier = Modifier.fillMaxWidth(),
//                onSearch = {
//                    searchQuery = it
//                    searchHistory = searchHistory.apply {
//                        if (!contains(it)) {
//                            add(it)
//                        } else {
//                            remove(it)
//                            add(it)
//                        }
//                    }
//                },
//                active = active,
//                onActiveChange = { active = it }
//            ) {
//                FlowRow(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    searchHistory.forEach {
//                        InputChip(
//                            selected = false,
//                            onClick = {
//                                query = it
//                            },
//                            label = {
//                                Text(text = it)
//                            },
//                            trailingIcon = {
//                                FixedSizeIcon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Delete",
//                                    modifier = Modifier.size(24.dp).clickable {
//                                        searchHistory -= it
//                                    }
//                                )
//                            }
//                        )
//                    }
//                }
//            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, height)
            ) {
                loadingList(
                    state,
                    retry = retry,
                    key = { it.chatBotId },
                    successDataProvider = {
                        data
                    },
                    onSuccess = {
                        onModelLoaded(currentSelectBotId, it)
                    },
                ) {
                    ListItem(
                        modifier = Modifier.clickable { onClick(it) }.animateItem(),
                        headlineContent = {
                            // 根据 searchQuery 高亮显示
//                            Text(text = it.name)
                            Text(
                                text = buildAnnotatedString {
                                    val name = it.name
                                    val query = searchQuery
                                    if (query.isEmpty()) {
                                        append(name)
                                        return@buildAnnotatedString
                                    }
                                    var start = 0
                                    var end = 0
                                    while (end < name.length) {
                                        end = name.indexOf(query, start, ignoreCase = true)
                                        if (end == -1) {
                                            append(name.substring(start))
                                            break
                                        }
                                        append(name.substring(start, end))
                                        withStyle(LocalTextStyle.current.copy(
                                            color = MaterialColors.OrangeA400,
                                            fontWeight = FontWeight.Bold,
                                        ).toSpanStyle()) {
                                            append(name.substring(end, end + query.length))
                                        }
                                        start = end + query.length
                                    }
                                },
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
                    )
                }
            }
        }
    }
}

fun Model.description(): String
    = "${ResStrings.context_length} ${ ((maxContextTokens)/1000f).roundToInt() }k | ${ResStrings.currency_symbol}${ cost1kTokens} / ${ResStrings.kilo_tokens} | $description"