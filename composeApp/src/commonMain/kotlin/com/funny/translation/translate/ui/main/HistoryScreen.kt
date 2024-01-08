package com.funny.translation.translate.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.paging.LazyPagingItems
import com.funny.translation.kmp.paging.collectAsLazyPagingItems
import com.funny.translation.kmp.paging.items
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.kmp.viewModel
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.widget.CommonNavBackIcon
import com.funny.translation.translate.ui.widget.CommonTopBar
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.translate.ui.widget.UpperPartBackground
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.touchToScale
import org.jetbrains.compose.resources.ExperimentalResourceApi


@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    progressProvider: () -> Float,
    navigateBackAction: SimpleAction
) {
    val vm: MainViewModel = viewModel()
    UpperPartBackground(
        modifier = modifier,
        cornerSizeProvider = { ((1 - progressProvider()) * 40).dp }
    ) {
        CommonTopBar(
            title = ResStrings.history,
            navigationIcon = {
                CommonNavBackIcon(navigateBackAction = navigateBackAction)
            }
        )
        TransFavoriteList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            transHistories = vm.transHistories.collectAsLazyPagingItems(),
            onClickHistory = { transHistory ->
                vm.translateText = transHistory.sourceString
                vm.sourceLanguage = findLanguageById(transHistory.sourceLanguageId)
                vm.targetLanguage = findLanguageById(transHistory.targetLanguageId)
                vm.translate()
            },
            onDeleteHistory = { sourceString ->
                vm.deleteTransHistory(sourceString)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
private fun TransFavoriteList(
    modifier: Modifier,
    transHistories: LazyPagingItems<TransHistoryBean>,
    onClickHistory: (TransHistoryBean) -> Unit,
    onDeleteHistory: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true // 这一条使得最新的历史会在最下面
    ) {
        if (transHistories.itemSnapshotList.isEmpty()) {
            item {
                HintText(text = ResStrings.no_history)
            }
        } else {
            items(transHistories, key = { it.id }) { transHistory ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .touchToScale {
                            onClickHistory(transHistory)
                        }
                        .padding(start = 8.dp)
                        .animateItemPlacement(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = transHistory.sourceString,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp
                    )
                    Row {
                        IconButton(onClick = {
                            ClipBoardUtil.copy(transHistory.sourceString)
                        }) {
                            FixedSizeIcon(
                                org.jetbrains.compose.resources.painterResource("drawable/ic_copy_content.png"),
                                ResStrings.copy_content
                            )
                        }
                        IconButton(onClick = {
                            onDeleteHistory(transHistory.sourceString)
                        }) {
                            FixedSizeIcon(Icons.Default.Delete, ResStrings.delete_this_history)
                        }
                    }
                }
            }
        }
    }
}