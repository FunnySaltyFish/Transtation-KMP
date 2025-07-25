@file:OptIn(ExperimentalResourceApi::class)

package com.funny.translation.translate.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.funny.trans.login.LoginActivity
import com.funny.translation.AppConfig
import com.funny.translation.WebViewActivity
import com.funny.translation.bean.ClickClipboardHintAction
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.main.components.ChildrenFixedSizeRow
import com.funny.translation.translate.ui.main.components.ClipboardHint
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.translate.ui.widget.NoticeBar
import com.funny.translation.translate.ui.widget.ShadowedAsyncRoundImage
import com.funny.translation.translate.ui.widget.SwipeCrossFadeLayout
import com.funny.translation.translate.ui.widget.SwipeShowType
import com.funny.translation.translate.ui.widget.UpperPartBackground
import com.funny.translation.translate.ui.widget.noticeBarModifier
import com.funny.translation.ui.AutoResizedText
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.safeMain
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler
import org.jetbrains.compose.resources.ExperimentalResourceApi

// 主页面，在未输入状态下展示的页面，默认
@ExperimentalResourceApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MainPartNormal(
    vm: MainViewModel,
    isScreenHorizontal: Boolean,
    showEngineSelectAction: () -> Unit,
    openDrawerAction: SimpleAction?,
) {
    val scope = rememberCoroutineScope()
    val swipeableState = rememberSwipeableState(initialValue = SwipeShowType.Main)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 因为前景是列表，如果滑到底部仍然有多余的滑动距离，就关闭
                // Log.d("NestedScrollConnection", "onPostScroll: $available")
                // 读者可以自行运行这行代码，滑动列表到底部后仍然上滑，看看上面会打印什么，就能明白这个 available 的作用了
                return if (available.y < 0 && source == NestedScrollSource.Drag) {
                    swipeableState.performDrag(available.toFloat()).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                swipeableState.performFling(velocity = Offset(available.x, available.y).toFloat())
                return available
            }

            private fun Float.toOffset(): Offset = Offset(0f, this)

            private fun Offset.toFloat(): Float = this.y
        }
    }

    val activityVM = LocalActivityVM.current
    LaunchedEffect(key1 = activityVM) {
        activityVM.activityLifecycleState.collect {
            Log.d("MainPartNormal", "activityLifecycleEvent: $it")
            when (it) {
                Lifecycle.Event.ON_RESUME -> {
                    if (AppConfig.sAutoFocus.value && swipeableState.currentValue == SwipeShowType.Main) {
                        vm.updateMainScreenState(MainScreenState.Inputting)
                    }
                }

                else -> Unit
            }
        }
    }

    // 返回键关闭
    BackHandler(swipeableState.currentValue == SwipeShowType.Foreground) {
        scope.launch {
            swipeableState.animateTo(SwipeShowType.Main)
        }
    }

    val progressState = remember { mutableFloatStateOf(1f) }
    SwipeCrossFadeLayout(
        state = swipeableState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        onProgressChanged = { progressState.floatValue = it },
        mainUpper = {
            UpperPartBackground {
                Column(
                    modifier = Modifier
                        .run {
                            when (LocalWindowSizeState.current) {
                                WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                                WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Top + WindowInsetsSides.End))
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MainTopBarNormal(showDrawerAction = openDrawerAction)
                    Notice(Modifier.fillMaxWidth(0.9f))
                    Spacer(modifier = Modifier.height(8.dp))
                    HintText(
                        onClick = { vm.updateMainScreenState(MainScreenState.Inputting) },
                        onLongClick = vm::tryToPasteAndTranslate,
                        translateByClipboardText = {
                            val clickClipboardHintAction by AppConfig.sClickClipboardHintAction
                            when (clickClipboardHintAction) {
                                ClickClipboardHintAction.Translate -> {
                                    vm.updateTranslateText(it.trim())
                                    vm.translate()
                                }
                                ClickClipboardHintAction.InputText -> {
                                    vm.updateTranslateText(it.trim())
                                    vm.updateMainScreenState(MainScreenState.Inputting)
                                }
                            }
                        }
                    )
                }
            }
        },
        mainLower = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .run {
                        when (LocalWindowSizeState.current) {
                            WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                            WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Bottom + WindowInsetsSides.End))
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                LanguageSelectRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 12.dp),
                    sourceLanguage = vm.sourceLanguage,
                    updateSourceLanguage = vm::updateSourceLanguage,
                    targetLanguage = vm.targetLanguage,
                    updateTargetLanguage = vm::updateTargetLanguage,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunctionsRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = if (isScreenHorizontal) 8.dp else 24.dp
                        ),
                    showEngineSelectAction
                )
            }
        },
        foreground = {
            HistoryScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                progressProvider = { progressState.floatValue }
            ) {
                scope.launch {
                    swipeableState.animateTo(SwipeShowType.Main)
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HintText(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    translateByClipboardText: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp).combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Text(
            text = ResStrings.trans_text_input_hint,
            fontSize = 28.sp,
            fontWeight = FontWeight.W600,
            color = Color.LightGray,
            textAlign = TextAlign.Start,
            lineHeight = 32.sp,
            modifier = Modifier.weight(1f)
        )
        ClipboardHint(
            modifier = Modifier.fillMaxWidth(),
            translateByClipboardText = translateByClipboardText
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Notice(modifier: Modifier) {
    var singleLine by remember { mutableStateOf(true) }
    val activityVM = LocalActivityVM.current
    val notice by activityVM.noticeInfo
    val context = LocalKMPContext.current
    notice?.let {
        NoticeBar(
            modifier = modifier
                .noticeBarModifier {
                    if (it.url.isNullOrEmpty()) singleLine = !singleLine
                    else WebViewActivity.start(context, it.url)
                },
            text = it.message,
            singleLine = singleLine,
            showClose = true,
            dismissForeverKey = if (it.allowDismissForever) "main_notice" else null,
        )
    }
}

@Composable
fun FunctionsRow(
    modifier: Modifier = Modifier,
    showEngineSelectAction: () -> Unit = {},
) {
    val navHostController = LocalNavController.current

    // 从左至右，图片翻译、选择引擎、收藏夹
    Row(modifier, horizontalArrangement = Arrangement.SpaceAround) {
        FunctionIconItem(
            iconName = "ic_album",
            text = ResStrings.image_translate
        ) {
            navHostController.navigateSingleTop(TranslateScreen.ImageTranslateScreen.route)
        }
        FunctionIconItem(
            iconName = "ic_translate",
            text = ResStrings.engine_select,
            onClick = showEngineSelectAction
        )
        FunctionIconItem(
            iconName = "ic_star_filled",
            text = ResStrings.favorites
        ) {
            navHostController.navigate(TranslateScreen.FavoriteScreen.route)
        }
    }
}

@Composable
private fun FunctionIconItem(
    iconName: String,
    text: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape), onClick = onClick, colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            FixedSizeIcon(
                painter = painterDrawableRes(iconName),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun LanguageSelectRow(
    modifier: Modifier,
    exchangeButtonTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sourceLanguage: Language,
    updateSourceLanguage: (Language) -> Unit,
    targetLanguage: Language,
    updateTargetLanguage: (Language) -> Unit,
) {
    val enabledLanguages by enabledLanguages.collectAsState()
    ChildrenFixedSizeRow(
        modifier = modifier,
        elementsPadding = 16.dp,
        left = {
            LanguageSelect(
                Modifier.semantics {
                    contentDescription = ResStrings.des_current_source_lang
                },
                language = sourceLanguage,
                languages = enabledLanguages,
                updateLanguage = updateSourceLanguage
            )
        }, center = {
            ExchangeButton(tint = exchangeButtonTint) {
                val temp = sourceLanguage
                updateSourceLanguage(targetLanguage)
                updateTargetLanguage(temp)
            }
        }, right = {
            LanguageSelect(
                Modifier.semantics {
                    contentDescription = ResStrings.des_current_target_lang
                },
                language = targetLanguage,
                languages = enabledLanguages,
                updateLanguage = updateTargetLanguage
            )
        }
    )
}

@ExperimentalResourceApi
@Composable
private fun MainTopBarNormal(
    showDrawerAction: (() -> Unit)?,
) {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDrawerAction != null) {
            IconButton(onClick = showDrawerAction) {
                FixedSizeIcon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = ResStrings.menu
                )
            }
        }
        IconButton(
            onClick = {
                navController.navigateSingleTop(TranslateScreen.PluginScreen.route)
            }, modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
        ) {
            FixedSizeIcon(
                painterDrawableRes("ic_plugin"),
                contentDescription = ResStrings.manage_plugins
            )
        }
    }
}

@Composable
private fun LanguageSelect(
    modifier: Modifier = Modifier,
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Button(
        modifier = modifier, onClick = {
            expanded = true
        }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ), contentPadding = PaddingValues(horizontal = 2.dp, vertical = 16.dp)
    ) {
        AutoResizedText(
            text = language.displayText,
            style = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.W600),
            maxLines = 1,
            byHeight = false
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }, text = {
                    Text(it.displayText)
                })
            }
        }
    }
}



@Composable
fun UserInfoPanel(navHostController: NavHostController) {
    val TAG = "UserInfoPanel"
    val activityVM = LocalActivityVM.current

    LaunchedEffect(key1 = activityVM.uid) {
        Log.d(TAG, "UserInfoPanel: uid is: ${activityVM.uid}, token is: ${activityVM.token}")
    }

    Box(
        Modifier
            .touchToScale {
                if (activityVM.uid <= 0) { // 未登录
                    ActivityManager.start(LoginActivity::class.java)
                } else {
                    navHostController.navigateSingleTop(
                        TranslateScreen.UserProfileScreen.route,
                        false
                    )
                }
            }
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
    ) {
        val userBean by AppConfig.userInfo
        if (userBean.isValid()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    ShadowedAsyncRoundImage(
                        modifier = Modifier.size(100.dp),
                        model = userBean.avatar_url,
                        contentDescription = ResStrings.avatar
                    )
                    if (userBean.isValidVip()) {
                        FixedSizeIcon(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(70.dp, 70.dp),
                            painter = painterDrawableRes("ic_vip"),
                            contentDescription = "VIP",
                            tint = Color.Unspecified
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${userBean.username} | uid: ${userBean.uid}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = LocalContentColor.current.copy(0.8f)
                )
            }
        } else {
            Text(
                text = ResStrings.login_or_register,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
