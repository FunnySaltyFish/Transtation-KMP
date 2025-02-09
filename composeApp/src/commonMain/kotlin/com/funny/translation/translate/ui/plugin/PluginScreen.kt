@file:OptIn(ExperimentalFoundationApi::class)

package com.funny.translation.translate.ui.plugin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.WebViewActivity
import com.funny.translation.codeeditor.CodeEditorActivity
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.js.bean.JsBean
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.rememberOpenFileLauncher
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LocalSnackbarState
import com.funny.translation.translate.ui.main.LocalWindowSizeState
import com.funny.translation.translate.ui.main.WindowSizeState
import com.funny.translation.translate.utils.expandableStickyRow
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.NavPaddingItem
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.launch

private const val TAG = "PluginScreen"

@Composable
fun PluginScreen() {
    val vm: PluginViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarState.current

    val showSnackbar: (String) -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(it)
        }
    }

    val showDeleteDialogState = remember { mutableStateOf(false) }
    var showAddPluginMenu by remember { mutableStateOf(false) }

    val windowSizeState = LocalWindowSizeState.current

    val localPlugins by vm.plugins.collectAsState(initial = arrayListOf())

    val importPluginLauncher = rememberOpenFileLauncher {
        if (it == null) return@rememberOpenFileLauncher
        vm.importPlugin(it, successCall = { str ->
            showSnackbar(str)
        }, failureCall = { str ->
            showSnackbar(str)
        })
    }

    val showDeleteDialogAction = remember {
        { plugin: JsBean ->
            showDeleteDialogState.value = true
            vm.needToDeletePlugin = plugin
        }
    }

    SimpleDialog(
        openDialogState = showDeleteDialogState,
        title = ResStrings.message_confirm,
        message = ResStrings.message_delete_plugin,
        confirmButtonText = ResStrings.message_yes,
        confirmButtonAction = {
            vm.needToDeletePlugin?.let(vm::deletePlugin)
        }
    )

    // 横屏状态下，分为两部分，左边为本地插件，右边为在线插件，默认展开
    var expandLocalPlugins by rememberStateOf(windowSizeState.isHorizontal)
    val localPluginPartWrapper: LazyListScope.() -> Unit = remember {
        {
            expandableStickyRow(
                title = ResStrings.local_plugins,
                expandLocalPlugins,
                { expandLocalPlugins = it }
            ) {
                localPlugins(
                    plugins = localPlugins,
                    deletePlugin = showDeleteDialogAction,
                    updateSelect = vm::updateLocalPluginSelect
                )
            }
        }
    }

    val (onlinePluginLoadingState, retryLoadOnlinePlugin) = rememberRetryableLoadingState(loader = vm::getOnlinePlugins)
    var expandOnlinePlugins by remember { mutableStateOf(true) }
    val onlinePluginListWrapper: LazyListScope.() -> Unit = remember {
        {
            expandableStickyRow(
                title = ResStrings.online_plugin,
                expandOnlinePlugins,
                { expandOnlinePlugins = it }) {
                loadingList(
                    onlinePluginLoadingState,
                    retryLoadOnlinePlugin,
                    key = { it.fileName }) { jsBean ->
                    var onlinePluginState by rememberSaveable {
                        vm.checkPluginState(jsBean)
                    }
                    OnlinePluginItem(
                        plugin = jsBean,
                        onlinePluginState = onlinePluginState,
                        clickOnlinePluginAction = object : ClickOnlinePluginAction {
                            override fun install(jsBean: JsBean) {
                                vm.installOrUpdatePlugin(jsBean, {
                                    onlinePluginState = OnlinePluginState.Installed
                                    showSnackbar(it)
                                }, {
                                    showSnackbar(it)
                                })
                            }

                            override fun delete(jsBean: JsBean) {
                                vm.deletePlugin(jsBean)
                                onlinePluginState = OnlinePluginState.NotInstalled
                            }

                            override fun update(jsBean: JsBean) {
                                vm.updatePlugin(jsBean)
                                onlinePluginState = OnlinePluginState.Installed
                            }
                        }
                    )
                }
            }
        }
    }

    val updateShowAddPluginMenu = { show: Boolean -> showAddPluginMenu = show }
    CommonPage(
        modifier = Modifier,
        title = ResStrings.manage_plugins,
        actions = {
            IconButton(onClick = { updateShowAddPluginMenu(true) }) {
                FixedSizeIcon(
                    Icons.Default.AddCircle,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Add plugins"
                )
                DropdownMenu(
                    expanded = showAddPluginMenu,
                    onDismissRequest = { updateShowAddPluginMenu(false) }) {
                    DropdownMenuItem(onClick = {
                        updateShowAddPluginMenu(false)
                        importPluginLauncher.launch(arrayOf("text/*"))
                    }, text = {
                        Text(ResStrings.import_plugin)
                    })
                    DropdownMenuItem(onClick = {
                        updateShowAddPluginMenu(false)
                        ActivityManager.start(CodeEditorActivity::class.java)
                    }, text = {
                        Text(ResStrings.create_plugin)
                    })
                }
            }
        },
        addNavPadding = false
    ) {
        when (windowSizeState) {
            WindowSizeState.VERTICAL -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        localPluginPartWrapper()
                        item {
                            HorizontalDivider()
                        }
                        onlinePluginListWrapper()
                        item {
                            NavPaddingItem()
                        }
                    }
                }
            }
            WindowSizeState.HORIZONTAL -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.45f),
                        verticalArrangement = spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        localPluginPartWrapper()
                    }
                    Spacer(modifier = Modifier.weight(0.015f))
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.45f),
                        verticalArrangement = spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        onlinePluginListWrapper()
                    }
                }
            }
        }
    }
}


private fun LazyListScope.localPlugins(
    plugins: List<JsBean>,
    updateSelect: (JsBean) -> Unit,
    deletePlugin: (JsBean) -> Unit
) {
    if (plugins.isNotEmpty()) {
        itemsIndexed(plugins) { _: Int, item: JsBean ->
            PluginItem(plugin = item, updateSelect = updateSelect, deletePlugin = deletePlugin)
        }
    } else {
        item {
            Text(
                text = ResStrings.empty_plugin_tip,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = W400,
                color = Color.Gray
            )
        }
    }
}


@Composable
internal fun PluginItem(
    plugin: JsBean,
    updateSelect: (JsBean) -> Unit,
    deletePlugin: (JsBean) -> Unit
) {
    var expand by remember {
        mutableStateOf(false)
    }
    var selected by remember {
        mutableStateOf(plugin.enabled > 0)
    }
    Column(modifier = Modifier
        .touchToScale { expand = !expand }
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plugin.fileName, fontSize = 22.sp, fontWeight = W600)
            Switch(checked = selected, onCheckedChange = {
                updateSelect(plugin)
                selected = !selected
            })
        }
        MarkdownText(
            markdown = plugin.markdown,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.9f),
            maxLines = if (expand) Int.MAX_VALUE else 1,
            onLinkClicked = { context, url ->
                WebViewActivity.start(context, url)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (expand) {
            Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = ResStrings.plugin_info_template.format(plugin.author, plugin.version.toString()),
                    fontWeight = W600
                )
                Text(
                    ResStrings.delete_plugin,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = W600,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            deletePlugin(plugin)
                        }
                )
            }
        }
    }
}