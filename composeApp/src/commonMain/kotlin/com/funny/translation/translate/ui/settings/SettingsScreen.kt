package com.funny.translation.translate.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.JetSettingDialog
import com.funny.jetsetting.core.JetSettingListDialog
import com.funny.jetsetting.core.JetSettingSwitch
import com.funny.jetsetting.core.JetSettingTile
import com.funny.jetsetting.core.ui.SettingItemCategory
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.AppConfig
import com.funny.translation.bean.AppLanguage
import com.funny.translation.helper.ApplicationUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.Platform
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.currentPlatform
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.network.ServiceCreator
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.transHistoryDao
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.utils.SortResultUtils
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.theme.LightDarkMode
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.move
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable

private const val TAG = "SettingScreen"
private val languages = AppLanguage.entries.toImmutableList()
private val lightDarkModes = LightDarkMode.entries.toImmutableList()


@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    CommonPage(
        title = ResStrings.nav_settings,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState),
    ) {
        if (AppConfig.developerMode.value) {
            SettingItemCategory(title = { ItemHeading(text = ResStrings.developer_mode) }) {
                JetSettingSwitch(state = AppConfig.developerMode, text = ResStrings.developer_mode)
                DevSetBaseUrl()
            }
        }
        if (DateUtils.isSpringFestival) {
            SettingItemCategory(title = {
                ItemHeading(text = ResStrings.setting_time_limited)
            }) {
                JetSettingSwitch(
                    state = AppConfig.sSpringFestivalTheme,
                    resourceName = "ic_theme",
                    text = ResStrings.setting_spring_theme,
                ) {
                    context.toastOnUi(
                        "已${
                            if (it) {
                                "设置"
                            } else {
                                "取消"
                            }
                        }春节限定主题"
                    )
                }
            }
        }
        SettingItemCategory(
            title = {
                ItemHeading(text = ResStrings.setting_app_preference)
            },
        ) {
            // 设置应用显示的语言
            // 跟随系统、简体中文、英语
            SelectAppLanguage()
            // 设置应用的主题
            JetSettingTile(
                resourceName = "ic_theme",
                text = ResStrings.theme
            ) {
                navController.navigate(TranslateScreen.ThemeScreen.route)
            }
        }
        SettingItemCategory(
            title = {
                ItemHeading(text = ResStrings.setting_translate)
            }
        ) {
            JetSettingSwitch(
                state = AppConfig.sEnterToTranslate,
                resourceName = "ic_enter",
                text = ResStrings.setting_enter_to_translate
            ) {
                if (it) context.toastOnUi(ResStrings.opened_enter_to_trans_tip)
            }
            JetSettingSwitch(
                state = AppConfig.sAutoFocus,
                text = ResStrings.setting_auto_focus,
                resourceName = "ic_keyboard",
                description = ResStrings.setting_auto_focus_desc
            ) {

            }
            JetSettingSwitch(
                state = AppConfig.sTextMenuFloatingWindow,
                resourceName = "ic_float_window",
                text = ResStrings.setting_text_menu_floating_window,
                description = ResStrings.setting_text_menu_floating_window_desc
            ) {

            }
            JetSettingSwitch(
                state = AppConfig.sAITransExplain,
                imageVector = Icons.Default.Insights,
                text = ResStrings.ai_trans_explain,
                description = ResStrings.ai_trans_explain_desc
            ) {

            }
            JetSettingTile(
                imageVector = Icons.Default.Sort,
                text = ResStrings.sort_result,
            ) {
                navController.navigate(TranslateScreen.SortResultScreen.route)
            }
            JetSettingTile(
                resourceName = "ic_language_select",
                text = ResStrings.select_language,
            ) {
                navController.navigate(TranslateScreen.SelectLanguageScreen.route)
            }
            // TTS
            JetSettingTile(
                imageVector = Icons.Default.SettingsVoice,
                text = ResStrings.speak_settings,
                description = ResStrings.speak_settings_desc
            ) {
                navController.navigate(TranslateScreen.TTSSettingsScreen.route)
            }
            val openConfirmDeleteDialogState = remember { mutableStateOf(false) }
            SimpleDialog(
                openDialogState = openConfirmDeleteDialogState,
                title = ResStrings.message_confirm,
                message = ResStrings.confirm_delete_history_desc,
                dismissButtonAction = {
                    scope.launch(Dispatchers.IO) {
                        appDB.transHistoryDao.clearAll()
                    }
                    context.toastOnUi(ResStrings.clear_histories_success)
                },
                dismissButtonText = ResStrings.confirm_to_delete,
                confirmButtonText = ResStrings.no_thanks
            )
            JetSettingTile(
                imageVector = Icons.Default.Delete,
                text = ResStrings.clear_trans_history,
            ) {
                openConfirmDeleteDialogState.value = true
            }
        }


        SettingItemCategory(
            title = {
                ItemHeading(text = ResStrings.trans_pro)
            }
        ) {
            // 并行翻译
            ProJetSettingCheckbox(
                state = AppConfig.sParallelTrans,
                text = ResStrings.parallel_trans,
                description = ResStrings.parallel_trans_desc,
                resourceName = "ic_parallel"
            )
            ProJetSettingCheckbox(
                state = AppConfig.sShowDetailResult,
                text = ResStrings.show_detail_result,
                description = ResStrings.show_detail_result_desc,
                resourceName = "ic_detail"
            )
            ProJetSettingCheckbox(
                state = AppConfig.sExpandDetailByDefault,
                text = ResStrings.expand_detail_by_default,
                resourceName = "ic_detail"
            )
        }
    }
}

@Composable
private fun SelectAppLanguage() {
    var tempLanguage by remember { mutableStateOf(LocaleUtils.getAppLanguage()) }

    JetSettingListDialog(
        list = languages,
        text = ResStrings.app_language,
        description = tempLanguage.description,
        resourceName = "ic_language_select",
        selected = tempLanguage,
        updateSelected = {
            tempLanguage = it
        },
        confirmButtonText = ResStrings.confirm_and_restart_app,
        confirmButtonAction = {
            CoroutineScope(Dispatchers.Default).launch {
                LocaleUtils.saveAppLanguage(tempLanguage)
                if (currentPlatform == Platform.Android) {
                    delay(200)
                    // restart App
                    ApplicationUtil.restartApp()
                } else {
                    appCtx.toastOnUi(ResStrings.restart_app_tip)
                }
            }
        }
    )
}


@Composable
private fun DevSetBaseUrl() {
    var text by remember {
        mutableStateOf(ServiceCreator.BASE_URL)
    }
    JetSettingDialog(
        text = ResStrings.setting_base_url,
        confirmButtonAction = {
            ServiceCreator.BASE_URL = text
        }) {
        TextField(value = text, onValueChange = { text = it })
    }
}

internal val DefaultVipInterceptor = {
    if (!AppConfig.isMembership()) {
        appCtx.toastOnUi(ResStrings.vip_only_tip)
        false
    } else {
        true
    }
}

@Composable
private fun ProJetSettingCheckbox(
    state: MutableState<Boolean>,
    text: String,
    description: String? = null,
    resourceName: String? = null,
    imageVector: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    JetSettingSwitch(
        state = state,
        imageVector = imageVector,
        resourceName = resourceName,
        text = text,
        description = description,
        interceptor = DefaultVipInterceptor,
        onCheck = onCheckedChange
    )
}


@Composable
fun SortResultScreen(
    modifier: Modifier = Modifier
) {
    CommonPage(title = ResStrings.sort_result) {
        val state = rememberReorderState()
        val localEngines by SortResultUtils.localEngines.collectAsState()
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

                    Divider()
                }
            }
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

@Composable
fun SelectLanguageScreen(modifier: Modifier) {
    val data = remember {
        allLanguages.map { DataSaverUtils.readData(it.selectedKey, true) }.toMutableStateList()
    }

    fun setEnabledState(language: Language, enabled: Boolean) {
        DataSaverUtils.saveData(language.selectedKey, enabled)
        if (enabled) {
            enabledLanguages.value = (enabledLanguages.value + language).sortedBy { it.id }
        } else {
            enabledLanguages.value = (enabledLanguages.value - language).sortedBy { it.id }
        }
    }

    fun setAllState(state: Boolean) {
        for (i in 0 until data.size) {
            data[i] = state
            setEnabledState(allLanguages[i], state)
        }
    }

    CommonPage(
        title = ResStrings.select_language,
        actions = {
            var selectAll by rememberSaveable {
                // 当所有开始都被选上时，默认就是全选状态
                mutableStateOf(data.firstOrNull { !it } == null)
            }
            val tintColor by animateColorAsState(targetValue = if (selectAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            IconButton(
                onClick = {
                    selectAll = !selectAll
                    setAllState(selectAll)
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
        DisposableEffect(key1 = Unit) {
            onDispose {
                // 如果什么都没选，退出的时候默认帮忙选几个
                data.firstOrNull { it } ?: kotlin.run {
                    for (i in 0..2) {
                        setEnabledState(allLanguages[i], true)
                    }
                }
            }
        }

        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(12.dp)
        ) {
            itemsIndexed(data, { i, _ -> i }) { i, selected ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = allLanguages[i].displayText,
                        modifier = Modifier.padding(16.dp)
                    )
                    Checkbox(checked = selected, onCheckedChange = {
                        data[i] = it
                        setEnabledState(allLanguages[i], it)
                    })
                }
            }
        }
    }
}

@Composable
internal fun ItemHeading(text: String) {
    Text(
        modifier = Modifier.semantics { heading() },
        text = text,
        fontWeight = FontWeight.Bold
    )
}