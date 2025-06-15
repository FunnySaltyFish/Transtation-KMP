package com.funny.translation.translate.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.funny.translation.NeedToTransConfig
import com.funny.translation.bean.TranslationConfig
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.VibratorUtils
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.paging.items
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.database.TransFavoriteBean
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.HintText
import com.funny.translation.ui.MarkdownText

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
) {
    CommonPage(
        modifier = modifier,
        title = ResStrings.favorite
    ) {
        val navController = LocalNavController.current
        val vm: FavoriteViewModel = viewModel()
        TransHistoryList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            transFavorites = vm.transFavorites.collectAsLazyPagingItems(),
            onClickItem = { transFavoriteBean ->
                NeedToTransConfig = TranslationConfig(
                    transFavoriteBean.sourceString,
                    findLanguageById(transFavoriteBean.sourceLanguageId),
                    findLanguageById(transFavoriteBean.targetLanguageId)
                )
                navController.popBackStack()
            },
            onDeleteFavorite = vm::deleteTransFavorite
        )
    }
}


@Composable
private fun TransHistoryList(
    modifier: Modifier,
    transFavorites: LazyPagingItems<TransFavoriteBean>,
    onClickItem: (TransFavoriteBean) -> Unit,
    onDeleteFavorite: (TransFavoriteBean) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        if (transFavorites.itemCount == 0) {
            item {
                HintText(text = ResStrings.no_favorite)
            }
        }
        items(transFavorites, key = { it.id }) { transFavorite ->
            FavoriteItem(
                modifier = Modifier.padding(vertical = 8.dp),
                item = transFavorite,
                onClick = {
                    onClickItem(transFavorite)
                },
                deleteAction = {
                    onDeleteFavorite(transFavorite)
                }
            )
        }
    }
}

@Composable
private fun FavoriteItem(
    modifier: Modifier,
    item: TransFavoriteBean,
    onClick: () -> Unit,
    deleteAction: SimpleAction,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .padding(start = 16.dp, end = 8.dp, bottom = 8.dp, top = 4.dp)
            .animateContentSize()
    ) {
        var expandDetail by rememberStateOf(false)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )
            // 如果有详细释义，则显示展开按钮
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                ExpandMoreButton(
                    modifier = Modifier.offset(24.dp),
                    expand = expandDetail,
                    tint = MaterialTheme.colorScheme.primary
                ) {
                    expandDetail = it
                }
            }
            IconButton(
                onClick = onClick,
                modifier = Modifier.offset(x = 16.dp)
            ) {
                FixedSizeIcon(
                    Icons.Default.Translate,
                    contentDescription = ResStrings.translate,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            SpeakButton(
                modifier = Modifier.offset(8.dp),
                text = item.resultText,
                language = findLanguageById(item.targetLanguageId)
            )
            MoreMenu(
                item = item,
                deleteAction = deleteAction
            )
        }
        SelectionContainer {
            Text(
                text = if (expandDetail) item.sourceString else item.sourceString.replace(
                    "\n",
                    "  "
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp,
                maxLines = if (expandDetail) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        SelectionContainer {
            Text(
                text = if (expandDetail) item.resultText else item.resultText.replace("\n", "  "),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                fontSize = 12.sp,
                maxLines = if (expandDetail) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (expandDetail && !item.detailText.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
            MarkdownText(
                markdown = item.detailText!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                selectable = true,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MoreMenu(
    item: TransFavoriteBean,
    deleteAction: SimpleAction
) {
    // 复制原文、复制译文、复制原文-译文对照、复制详细翻译、删除
    var showMune by rememberStateOf(false)
    val context = LocalContext.current
    val copy = { text: String ->
        ClipBoardUtil.copy(text)
        showMune = false
        context.toastOnUi(ResStrings.copied_to_clipboard)
    }

    IconButton(
        onClick = {
            showMune = !showMune
        }
    ) {
        FixedSizeIcon(
            Icons.Default.MoreVert,
            contentDescription = "More",
            tint = MaterialTheme.colorScheme.primary
        )

        if (showMune) {
            DropdownMenu(
                expanded = showMune,
                onDismissRequest = { showMune = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = ResStrings.copy_source_text) },
                    onClick = {
                        copy(item.sourceString)
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = ResStrings.copy_target_text) },
                    onClick = {
                        copy(item.resultText)
                    }
                )
                if (!item.detailText.isNullOrEmpty()) {
                    DropdownMenuItem(
                        text = { Text(text = ResStrings.copy_detail_result) },
                        onClick = {
                            copy(item.detailText!!)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(text = ResStrings.delete) },
                    onClick = {
                        showMune = false
                        deleteAction()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    modifier: Modifier = Modifier,
    onDismissed: () -> Unit,
    dismissContent: @Composable RowScope.() -> Unit
) {
    // 侧滑删除所需State
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            // 按指定方向触发删除后的回调，在此处变更具体数据
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                onDismissed()
                VibratorUtils.vibrate()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        // 允许滑动删除的方向
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        // "背景 "，即原来显示的内容被划走一部分时显示什么
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            }
            val icon = Icons.Default.Delete
            val settled = dismissState.targetValue == SwipeToDismissBoxValue.Settled
            val scale by animateFloatAsState(if (settled) 1f else 1.5f, label = "")

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                FixedSizeIcon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.scale(scale),
                    tint = if (!settled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
        },
        content = dismissContent
    )
}