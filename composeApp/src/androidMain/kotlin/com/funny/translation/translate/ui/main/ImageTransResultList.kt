package com.funny.translation.translate.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.compose.loading.DefaultFailure
import com.funny.compose.loading.LoadingState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationPart
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.ui.long_text.ModelListPart
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.collections.immutable.toImmutableList
import moe.tlaster.precompose.navigation.BackHandler


/**
 * 图片翻译的结果对照，允许使用者以列表的形式查看翻译结果。
 *
 * 并且可以任意选择多项结果，复制到剪贴板、导出文本、或利用AI进行整理
 */
@Composable
internal fun ImageTransResultList(
    updateCurrentPage: (ImageTransPage) -> Unit
) {
    val vm = viewModel<ImageTransViewModel>()
    val result = vm.translateState.getOrNull<ImageTranslationResult>() ?: return

    var displayText by rememberStateOf("")
    val selectedResultParts = vm.selectedResultParts
    val content = result.content

    val selectedNotEmpty by rememberDerivedStateOf { selectedResultParts.isNotEmpty() }

    var showAIOptimizationSheet by rememberStateOf(false)

    val showDisplayTextDialogState = rememberStateOf(false)
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

    fun goBack() {
        vm.cancelOptimizeByAI()
        updateCurrentPage(ImageTransPage.Main)
    }

    val showConfirmExitState = rememberStateOf(false)
    SimpleDialog(
        openDialogState = showConfirmExitState,
        content = {
            Text(text = "当前优化正在进行中，确认要退出吗？")//ResStrings.exit_optimization)
        },
        confirmButtonAction = {
            showConfirmExitState.value = false
            goBack()
        },
    )

    BackHandler {
        if (vm.isOptimizing()) {
            showConfirmExitState.value = true
        } else {
            goBack()
        }
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
                    showAIOptimizationSheet = true
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
            itemsIndexed(content) { i, item ->
                ResultItem(
                    result = item,
                    selected = selectedResultParts.contains(i to item),
                    onSelectedChange = { vm.updateSelectedResultParts(i, item, it) }
                )
            }
        }
    }

    if (showAIOptimizationSheet) {
        AIOptimizationSheet(
            vm = vm,
            onDismissRequest = {
                showAIOptimizationSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AIOptimizationSheet(
    vm: ImageTransViewModel,
    onDismissRequest: () -> Unit
) {
    val loadingStateState = vm.optimizeByAITask?.loadingState

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            if (loadingStateState == null) {
                ModelListPart(vm::onModelListLoaded, vm::updateChatBot)
                // Confirm
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = vm::optimizeByAI,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                ) {
                    Text(text = "开始优化")
                }
            } else {
                val loadingState by loadingStateState
                val task = vm.optimizeByAITask!!
                when (loadingState) {
                    is LoadingState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(task.aiJobGeneratedText)
                            // 右上角放个 Loading
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.TopEnd).size(36.dp))
                        }
                    }

                    is LoadingState.Failure -> {
                        DefaultFailure(
                            retry = vm::optimizeByAI
                        )
                    }

                    is LoadingState.Success -> {
                        val data = (loadingState as LoadingState.Success).data
                        val list = data.toImmutableList()
                        val selectedResults = remember(data) {
                            list.toMutableStateList()
                        }
                        LazyColumn() {
                            itemsIndexed(list) { i, item ->
                                IndexedPartItem(
                                    result = item,
                                    selected = item in selectedResults,
                                    onSelectedChange = {
                                        if (it) {
                                            selectedResults.add(item)
                                        } else {
                                            selectedResults.remove(item)
                                        }
                                    }
                                )
                            }

                            stickyHeader {
                                Row(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            onDismissRequest()
                                        },
                                        modifier = Modifier
                                    ) {
                                        Text(text = "取消")
                                    }
                                    TextButton(
                                        onClick = {
                                            vm.replaceSelectedParts(selectedResults)
                                            appCtx.toastOnUi("替换完成")
                                            onDismissRequest()
                                        },
                                        modifier = Modifier
                                    ) {
                                        Text(text = "替换原结果")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyActionButton(
    result: ImageTranslationResult,
    showDisplayTextDialog: (String) -> Unit,
    selectedParts: SnapshotStateList<SingleIndexedImageTranslationPart>,
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
                        val text = selectedParts.joinToString("\n") { it.second.source }
                        ClipBoardUtil.copy(text)
                        showContextMenu = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "复制选中译文")
                    },
                    onClick = {
                        val text = selectedParts.joinToString("\n") { it.second.target }
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

@Composable
private fun IndexedPartItem(
    result: MultiIndexedImageTranslationPart,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    ResultItem(
        result = result.part,
        selected = selected,
        onSelectedChange = onSelectedChange
    )
}