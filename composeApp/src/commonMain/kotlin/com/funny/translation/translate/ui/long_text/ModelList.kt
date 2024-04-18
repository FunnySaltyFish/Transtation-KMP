package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.utils.ModelManager
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.strings.ResStrings
import kotlin.math.roundToInt

@Composable
fun ColumnScope.ModelListPart(
    onModelLoaded: (currentSelectBotId: Int, models: List<Model>) -> Unit,
    onModelSelected: (model: Model) -> Unit,
    maxHeight: Dp = 400.dp,
) {
    Category(
        title = ResStrings.model_select,
        helpText = ResStrings.model_select_help,
        defaultExpand = true,
    ) { expanded ->
        var currentSelectBotId by rememberDataSaverState("selected_chat_model_id", initialValue = 0)
        val height by animateDpAsState(targetValue = if (expanded) maxHeight else 200.dp, label = "height")
        val onClick = { model: Model ->
            currentSelectBotId = model.chatBotId
            onModelSelected(model)
        }
        val (state, retry) = rememberRetryableLoadingState(loader = {
            ModelManager.models.await()
        })

        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, height)
        ) {
            loadingList(
                state,
                retry = retry,
                key = { it.chatBotId },
                onSuccess = {
                    onModelLoaded(currentSelectBotId, it)
                },
            ) {
                ListItem(
                    modifier = Modifier.clickable { onClick(it) },
                    headlineContent = {
                        Text(text = it.name)
                    },
                    supportingContent = {
                        Text(text = it.description(), fontSize = 12.sp)
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

fun Model.description(): String
    = "${ResStrings.context_length} ${ ((maxContextTokens)/1000f).roundToInt() }k | ${ResStrings.currency_symbol}${ cost1kTokens} / ${ResStrings.kilo_tokens} | $description"