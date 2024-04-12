package com.funny.translation.translate.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationPart
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon


/**
 * 图片翻译的结果对照，允许使用者以列表的形式查看翻译结果。
 *
 * 并且可以任意选择多项结果，复制到剪贴板、导出文本、或利用AI进行整理
 */
@Composable
internal fun ImageTransResultList(
    vm: ImageTransViewModel
) {
    val result = vm.translateState.getOrNull<ImageTranslationResult>() ?: return

    val showDisplayTextDialogState = rememberStateOf(false)
    var displayText by rememberStateOf("")
    val selectedResultParts = vm.selectedResultParts
    val content = result.content

    val selectedNotEmpty by rememberDerivedStateOf { selectedResultParts.isNotEmpty() }

    SimpleDialog(
        openDialogState = showDisplayTextDialogState,
        content = {
            SelectionContainer {
                Text(text = displayText)
            }
        },
        confirmButtonText = ResStrings.copy_content,
        confirmButtonAction = {
            // 复制到剪贴板
            ClipBoardUtil.copy(displayText)
        }
    )

    fun showDisplayTextDialog(text: String) {
        displayText = text
        showDisplayTextDialogState.value = true
    }

    CommonPage(
        title = "结果处理",
        actions = {
            // AI 处理
            AnimatedVisibility(
                visible = selectedNotEmpty,
                enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
            ) {
                IconButton(onClick = {

                }) {
                    FixedSizeIcon(
                        painter = painterDrawableRes("ic_magic"),
                        contentDescription = "AI合并",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 查看原文、查看译文、全选
            CopyActionButton(result = result, showDisplayTextDialog = ::showDisplayTextDialog, selectedParts = selectedResultParts)

            val selectAll by rememberDerivedStateOf {
                selectedResultParts.size == content.size
            }
            val tintColor by animateColorAsState(targetValue = if (selectAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            IconButton(
                onClick = {
                    if (selectAll) {
                        vm.clearSelectedResultParts()
                    } else {
                        vm.selectAllResultParts()
                    }
                }
            ) {
                FixedSizeIcon(
                    painter = painterDrawableRes("ic_select_all"),
                    contentDescription = ResStrings.whether_selected_all,
                    tint = tintColor
                )
            }
        }
    ) {
        LazyColumn() {
            items(content) { item ->
                ResultItem(
                    result = item,
                    selected = selectedResultParts.contains(item),
                    onSelectedChange = { vm.updateSelectedResultParts(item, it) }
                )
            }
        }
    }
}

@Composable
private fun CopyActionButton(
    result: ImageTranslationResult,
    showDisplayTextDialog: (String) -> Unit,
    selectedParts: SnapshotStateList<ImageTranslationPart>,

) {
    var showContextMenu by rememberStateOf(false)

    IconButton(onClick = { showContextMenu = true }) {
        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "查看原文")

        DropdownMenu(showContextMenu, onDismissRequest = { showContextMenu = false} ) {
            // 显示原文和译文
            DropdownMenuItem(
                text = {
                    Text(text = "查看原文")
                },
                onClick = {
                    showDisplayTextDialog(result.source)
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = "查看译文")
                },
                onClick = {
                    showDisplayTextDialog(result.target)
                }
            )
            // 复制原文、复制译文、复制选中原文、复制选中译文
            DropdownMenuItem(
                text = {
                    Text(text = "复制原文")
                },
                onClick = {
                    ClipBoardUtil.copy(result.source)
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = "复制译文")
                },
                onClick = {
                    ClipBoardUtil.copy(result.target)
                    showContextMenu = false
                }
            )
            if (selectedParts.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(text = "复制选中原文")
                    },
                    onClick = {
                        val text = selectedParts.joinToString("\n") { it.source }
                        ClipBoardUtil.copy(text)
                        showContextMenu = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "复制选中译文")
                    },
                    onClick = {
                        val text = selectedParts.joinToString("\n") { it.target }
                        ClipBoardUtil.copy(text)
                        showContextMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultItem(
    result: ImageTranslationPart,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    // 多选框、原文本、译文本，复制按钮
    ListItem(
        modifier = Modifier.clickable {
            onSelectedChange(!selected)
        },
        headlineContent = {
            Text(text = result.source)
        },
        supportingContent = {
            Text(text = result.target)
        },
        leadingContent = {
            // 多选框
            Checkbox(
                checked = selected,
                onCheckedChange = onSelectedChange
            )
        },
        trailingContent = {
            // 复制按钮
            CopyButton(text = result.target, tint = MaterialTheme.colorScheme.onSurface)
        }
    )
}