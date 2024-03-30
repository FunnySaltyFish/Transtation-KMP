package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.kmp.viewModel
import com.funny.translation.translate.Language
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSConfManager
import com.funny.translation.translate.tts.findTTSProviderById
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.NavPaddingItem
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
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
                    }
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
                            TTSConfManager.createDefaultConf(lang).let {
                                appDB.tTSConfQueries.insert(it)
                            }
                            navController.navigate(
                                TranslateScreen.TTSEditConfScreen.route.formatBraceStyle(
                                    "id" to appDB.tTSConfQueries.getByLanguage(lang).executeAsOne().id
                                )
                            )
                        },
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

@Composable
private fun ConfigItem(
    conf: TTSConf,
    editAction: (TTSConf) -> Unit
) {
    val ttsProvider = remember(conf) {
        findTTSProviderById(conf.ttsProviderId)
    }

    ListItem(
        modifier = Modifier.clickable { editAction(conf) },
        headlineContent = {
            Text(conf.language.displayText)
        },
        supportingContent = {
            Text("${ttsProvider.name} - ${conf.speaker.shortName}")
        },
        trailingContent = {
            FixedSizeIcon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    )
}