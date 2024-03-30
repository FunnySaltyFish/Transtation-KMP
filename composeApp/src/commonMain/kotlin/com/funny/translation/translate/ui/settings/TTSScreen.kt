package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.kmp.viewModel
import com.funny.translation.translate.Language
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSConfManager
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.tts.findTTSProviderById
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.NavPaddingItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TTSScreen() {
    CommonPage(
        title = ResStrings.speak_settings,
    ) {
        val navController = LocalNavController.current
        val vm: TTSScreenViewModel = viewModel()
        val confList by vm.confListFlow.collectAsState(emptyList())
        val sortedConfList = remember(confList) {
            confList.sortedBy { it.language.id }.toImmutableList()
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
//            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedConfList, key = { it.id }) { conf ->
                ConfigItem(
                    conf = conf,
                    editAction = {
                        navController.navigate(
                            TranslateScreen.TTSEditConfScreen.route.formatBraceStyle(
                                "id" to it.id
                            )
                        )
                    },
                    deleteAction = vm::delete,
                    confList = sortedConfList,
                    applyConfAction = vm::applyToConfs
                )
            }

            item {
                AddConfigItem(vm.noConfLangList)
            }

            item {
                NavPaddingItem()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddConfigItem(
    noConfLangList: List<Language>
) {
    // 没有未添加配置的语言，则直接跳过
    if (noConfLangList.isEmpty()) return
    val showAddConfigDialog = rememberStateOf(false)
    val navController = LocalNavController.current
    SimpleDialog(
        openDialogState = showAddConfigDialog,
        title = ResStrings.add_config,
        content = {
            LazyColumn {
                items(noConfLangList) { lang ->
                    ListItem(
                        modifier = Modifier.clickable {
                            showAddConfigDialog.value = false
                            // 创建默认配置，并直接跳转到编辑页面
                            TTSConfManager.createNewAndJump(navController, lang)
                        }.animateItemPlacement(),
                        headlineContent = {
                            Text(lang.displayText)
                        },
                        trailingContent = {
                            FixedSizeIcon(imageVector = Icons.Default.Edit, contentDescription = null)
                        }
                    )
                }
            }
        }
    )

    ListItem(
        modifier = Modifier.clickable { showAddConfigDialog.value = true },
        headlineContent = {
            Text(ResStrings.add_config)
        }
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConfigItem(
    conf: TTSConf,
    editAction: (TTSConf) -> Unit,
    deleteAction: (TTSConf) -> Unit,
    confList: ImmutableList<TTSConf>,
    applyConfAction: (origin: TTSConf, target: List<TTSConf>) -> Unit
) {
    val ttsProvider = remember(conf) {
        findTTSProviderById(conf.ttsProviderId)
    }

    // 长按弹出 DropDownMenu：删除、应用到……
    val showDropdownMenuState = rememberStateOf(false)

    ListItem(
        modifier = Modifier.clickable { editAction(conf) },
        headlineContent = {
            Text(conf.language.displayText)
        },
        supportingContent = {
            Text("${ttsProvider.name} - ${conf.speaker.shortName}")
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FixedSizeIcon(imageVector = Icons.Default.Edit, contentDescription = null)
                IconButton(
                    onClick = {
                        showDropdownMenuState.value = true
                    }
                ) {
                    FixedSizeIcon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                    ConfItemDropdownMenu(
                        showDropdownMenuState = showDropdownMenuState,
                        conf = conf,
                        ttsProvider = ttsProvider,
                        confList = confList,
                        deleteAction = deleteAction,
                        applyConfAction = applyConfAction
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConfItemDropdownMenu(
    showDropdownMenuState: MutableState<Boolean>,
    conf: TTSConf,
    ttsProvider: TTSProvider,
    confList: ImmutableList<TTSConf>,
    deleteAction: (TTSConf) -> Unit,
    applyConfAction: (origin: TTSConf, target: List<TTSConf>) -> Unit
) {
    val showApplyToDialog = rememberStateOf(false)
    val selectedConfList = mutableStateListOf<TTSConf>()
    val filteredConfList = remember(confList) {
        confList.filter { it.language != conf.language }.toImmutableList()
    }

    SimpleDialog(
        openDialogState = showApplyToDialog,
        title = ResStrings.apply_config_to,
        content = {
            LazyColumn {
                stickyHeader {
                    // 当前配置
                    ListItem(
                        modifier = Modifier,
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        headlineContent = {
                            Text(ResStrings.current_config + " - " + conf.language.displayText)
                        },
                        supportingContent = {
                            Text("${ttsProvider.name} - ${conf.speaker.shortName}")
                        }
                    )
                }

                items(filteredConfList) { conf ->
                    ListItem(
                        modifier = Modifier,
                        headlineContent = {
                            Text(conf.language.displayText)
                        },
                        supportingContent = {
                            Text("${ttsProvider.name} - ${conf.speaker.shortName}")
                        },
                        trailingContent = {
                            Checkbox(
                                checked = selectedConfList.contains(conf),
                                onCheckedChange = {
                                    if (it) {
                                        selectedConfList.add(conf)
                                    } else {
                                        selectedConfList.remove(conf)
                                    }
                                }
                            )
                        }
                    )
                }
            }
        },
        confirmButtonAction = {
            // 应用到选中的配置
            applyConfAction(conf, selectedConfList)
        }
    )

    var showDropdownMenu by showDropdownMenuState
    DropdownMenu(
        expanded = showDropdownMenu,
        onDismissRequest = {
            showDropdownMenu = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text(ResStrings.delete)
            },
            onClick = {
                showDropdownMenu = false
                deleteAction(conf)
            }
        )

        DropdownMenuItem(
            text = {
                Text(ResStrings.apply_config_to)
            },
            onClick = {
                showDropdownMenu = false
                showApplyToDialog.value = true
            }
        )
    }
}