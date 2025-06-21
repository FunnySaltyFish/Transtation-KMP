package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.Log
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.utils.SortResultUtils
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.navPaddingItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.move
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun SortResultScreen(
    modifier: Modifier = Modifier
) {
    CommonPage(title = ResStrings.sort_result, enableOverScroll = false) {
        val state = rememberReorderState()
        val localEngines by SortResultUtils.sortedEngines.collectAsState()
        val data by remember {
            derivedStateOf {
                localEngines.toMutableStateList()
            }
        }
        LazyColumn(
            state = state.listState,
            modifier = modifier
                .then(
                    Modifier.reorderable(
                        state,
                        onMove = { from, to -> data.move(from.index, to.index) })
                )
        ) {
            itemsIndexed(data, { i, _ -> i }) { i, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggedItem(state.offsetByIndex(i))
                        .background(MaterialTheme.colorScheme.surface)
                        .detectReorderAfterLongPress(state)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(24.dp))
                        FixedSizeIcon(painterDrawableRes("ic_drag"), "Drag to sort")
                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    HorizontalDivider()
                }
            }
            navPaddingItem()
        }

        DisposableEffect(key1 = null) {
            onDispose {
                if (!SortResultUtils.checkEquals(data)) {
                    Log.d(TAG, "SortResult: 不相等")
                    SortResultUtils.resetMappingAndSave(data)
                }
            }
        }
    }
}

private const val TAG = "SortResultScreen"