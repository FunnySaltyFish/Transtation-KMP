package com.funny.translation.translate.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.kmp.paging.LazyPagingItems
import com.funny.translation.kmp.paging.collectAsLazyPagingItems
import com.funny.translation.kmp.paging.items
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.kmp.viewModel
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.database.TransFavoriteBean
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.navigateToTextTrans
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon

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
                navController.navigateToTextTrans(
                    transFavoriteBean.sourceString,
                    findLanguageById(transFavoriteBean.sourceLanguageId),
                    findLanguageById(transFavoriteBean.targetLanguageId)
                )
            },
            onDeleteFavorite = vm::deleteTransFavorite
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
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
            SwipeToDismissItem(modifier = Modifier.fillMaxWidth(), onDismissed = {
                onDeleteFavorite(transFavorite)
            }) {
                FavoriteItem(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateItemPlacement(),
                    item = transFavorite,
                    onClick = {
                        onClickItem(transFavorite)
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    modifier: Modifier,
    item: TransFavoriteBean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )
            SpeakButton(
                modifier = Modifier.offset(8.dp),
                text = item.resultText,
                language = findLanguageById(item.targetLanguageId)
            )
            CopyButton(
                text = item.resultText,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = item.sourceString.replace("\n", "  "),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.resultText.replace("\n", "  "),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
        // "背景 "，即原来显示的内容被划走一部分时显示什么
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            }
            val icon = Icons.Default.Delete
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f, label = ""
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                FixedSizeIcon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.scale(scale)
                )
            }
        },
        content = dismissContent
    )
}