package com.funny.translation.translate

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.Consts
import com.funny.translation.NeedToTransConfig
import com.funny.translation.bean.TranslationConfig
import com.funny.translation.helper.Log
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.NavGraphBuilder
import com.funny.translation.kmp.NavHost
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.animateComposable
import com.funny.translation.kmp.composable
import com.funny.translation.kmp.navOptions
import com.funny.translation.kmp.navigation
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.kmp.viewModel
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.main.FavoriteScreen
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.ui.MarkdownText
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler

private const val TAG = "AppNav"
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}
val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarState has not been initialized! ")
}
val LocalActivityVM = staticCompositionLocalOf<ActivityViewModel> {
    error("Local ActivityVM has not been initialized! ")
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun AppNavigation(
    navController: NavHostController,
    exitAppAction: () -> Unit
) {
    val context = LocalKMPContext.current

    LaunchedEffect(key1 = navController) {
        (context as TransActivity).navController = navController
    }

    val activityVM: ActivityViewModel = viewModel()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val canGoBack by navController.canGoBack.collectAsState(false)
    BackHandler(enabled = true) {
        if (!canGoBack) {
            val curTime = System.currentTimeMillis()
            if (curTime - activityVM.lastBackTime > 2000) {
                scope.launch {
                    snackbarHostState.showSnackbar(ResStrings.snack_quit)
                }
                activityVM.lastBackTime = curTime
            } else {
                exitAppAction()
            }
        } else {
            Log.d(TAG, "AppNavigation: back")
            //currentScreen = TranslateScreen.MainScreen
        }
    }

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarState provides snackbarHostState,
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { scaffoldPadding ->
            NavHost(
                navController = navController,
                startDestination = TranslateScreen.MainScreen.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    TranslateScreen.MainScreen.route,
                ) {
                    MainScreen()
                }
//                    animateComposable(
//                        TranslateScreen.ImageTranslateScreen.route,
//                        deepLinks = listOf(
//                            navDeepLink {
//                                uriPattern =
//                                    "${DeepLinkManager.PREFIX}${DeepLinkManager.IMAGE_TRANS_PATH}?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}&doClip={doClip}"
//                            }
//                        ),
//                        arguments = listOf(
//                            navArgument("imageUri") {
//                                type = NavType.StringType; defaultValue = null; nullable = true
//                            },
//                            navArgument("sourceId") {
//                                type = NavType.IntType; defaultValue = Language.AUTO.id
//                            },
//                            navArgument("targetId") {
//                                type = NavType.IntType; defaultValue = Language.CHINESE.id
//                            },
//                            navArgument("doClip") {
//                                type = NavType.BoolType; defaultValue = false
//                            }
//                        )
//                    ) {
//                        // 使用 Intent 跳转目前会导致 Activity 重建
//                        // 不合理，相当不合理
//                        ImageTransScreen(
//                            imageUri = it.arguments?.getString("imageUri")?.toUri(),
//                            sourceId = it.arguments?.getInt("sourceId"),
//                            targetId = it.arguments?.getInt("targetId"),
//                            doClipFirst = it.arguments?.getBoolean("doClip") ?: false
//                        )
//                    }
//                    animateComposable(TranslateScreen.AboutScreen.route) {
//                        AboutScreen()
//                    }
//                    animateComposable(TranslateScreen.PluginScreen.route) {
//                        PluginScreen()
//                    }
//                    animateComposable(TranslateScreen.TransProScreen.route) {
//                        TransProScreen()
//                    }
//                    animateComposable(TranslateScreen.ThanksScreen.route) {
//                        ThanksScreen()
//                    }
//                    animateComposable(TranslateScreen.FloatWindowScreen.route) {
//                        FloatWindowScreen()
//                    }
                animateComposable(TranslateScreen.FavoriteScreen.route) {
                    FavoriteScreen()
                }
//                    animateComposable(TranslateScreen.AppRecommendationScreen.route) {
//                        AppRecommendationScreen()
//                    }
//                    val animDuration = NAV_ANIM_DURATION
//                    composable(
//                        TranslateScreen.ChatScreen.route,
//                        enterTransition = {
//                            slideIntoContainer(
//                                AnimatedContentTransitionScope.SlideDirection.Up,
//                                animationSpec = tween(animDuration)
//                            )
//                        },
//                        exitTransition = {
//                            slideOutOfContainer(
//                                AnimatedContentTransitionScope.SlideDirection.Up,
//                                animationSpec = tween(animDuration)
//                            )
//                        },
//                        popEnterTransition = {
//                            slideIntoContainer(
//                                AnimatedContentTransitionScope.SlideDirection.Down,
//                                animationSpec = tween(animDuration)
//                            )
//                        },
//                        popExitTransition = {
//                            slideOutOfContainer(
//                                AnimatedContentTransitionScope.SlideDirection.Down,
//                                animationSpec = tween(animDuration)
//                            )
//                        }
//                    ) {
//                        ChatScreen()
//                    }
//                    animateComposable(
//                        TranslateScreen.BuyAIPointScreen.route,
//                        arguments = listOf(
//                            navArgument("planName") {
//                                type = NavType.StringType; defaultValue = null; nullable = true
//                            }
//                        )
//                    ) {
//                        val planName = it.arguments?.getString("planName")
//                        BuyAIPointScreen(planName ?: AI_TEXT_POINT)
//                    }
//                    animateComposable(TranslateScreen.AnnualReportScreen.route) {
//                        AnnualReportScreen()
//                    }
//                addLongTextTransNavigation()
//                addSettingsNavigation()
//                    addUserProfileRoutes(
//                        navHostController = navController
//                    ) { userBean ->
//                        Log.d(TAG, "登录成功: 用户: $userBean")
//                        if (userBean.isValid()) AppConfig.login(userBean, updateVipFeatures = true)
//                    }
            }
        }

        var firstOpenApplication by rememberDataSaverState<Boolean>(
            key = Consts.KEY_FIRST_OPEN_APP,
            initialValue = true
        )
        if (firstOpenApplication) {
            AlertDialog(
                onDismissRequest = { },
                text = {
                    MarkdownText(markdown = "我们更新了新的[隐私政策](https://api.funnysaltyfish.fun/trans/v1/api/privacy)和[用户协议](https://api.funnysaltyfish.fun/trans/v1/api/user_agreement)，请认真阅读并同意后，方可使用本应用")
                },
                confirmButton = {
                    Button(onClick = { firstOpenApplication = false }) {
                        Text(ResStrings.agree)
                    }
                },
                dismissButton = {
                    Button(onClick = exitAppAction) {
                        Text(ResStrings.not_agree)
                    }
                }
            )
        }
    }


}


private fun NavGraphBuilder.addLongTextTransNavigation() {
    navigation(
        startDestination = TranslateScreen.LongTextTransScreen.route,
        route = "nav_1_long_text_trans"
    ) {
//        animateComposable(TranslateScreen.LongTextTransScreen.route) {
//            LongTextTransScreen()
//        }
//        animateComposable(TranslateScreen.LongTextTransListScreen.route) {
//            LongTextTransListScreen()
//        }
//        animateComposable(
//            TranslateScreen.LongTextTransDetailScreen.route,
//            arguments = listOf(
//                navArgument("id") {
//                    type = NavType.StringType; defaultValue = null; nullable = true
//                }
//            )
//        ) {
//            val id = it.arguments?.getString("id")
//            LongTextTransDetailScreen(id = id ?: UUID.randomUUID().toString())
//        }
//        animateComposable(
//            TranslateScreen.TextEditorScreen.route,
//            arguments = listOf(
//                navArgument("action") {
//                    type = NavType.StringType
//                }
//            )
//        ) {
//            val action = kotlin.runCatching {
//                TextEditorAction.fromString(it.arguments?.getString("action") ?: "")
//            }.getOrNull()
//            TextEditorScreen(action)
//        }
//        animateComposable(
//            TranslateScreen.DraftScreen.route
//        ) {
//            DraftScreen()
//        }
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSettingsNavigation() {
    navigation(
        startDestination = TranslateScreen.SettingScreen.route,
        route = "nav_1_setting",
    ) {
//        animateComposable(TranslateScreen.SettingScreen.route) {
//            SettingsScreen()
//        }
//        animateComposable(
//            TranslateScreen.OpenSourceLibScreen.route,
//        ) {
//            OpenSourceLibScreen()
//        }
//        animateComposable(
//            TranslateScreen.ThemeScreen.route,
//        ) {
//            ThemeScreen()
//        }
//        animateComposable(
//            TranslateScreen.SortResultScreen.route,
//        ) {
//            SortResult(Modifier.fillMaxSize())
//        }
//        animateComposable(
//            TranslateScreen.SelectLanguageScreen.route
//        ) {
//            SelectLanguage(modifier = Modifier.fillMaxSize())
//        }
    }
}

fun NavHostController.navigateSingleTop(route: String, popUpToMain: Boolean = false) {
    val navController = this
    navController.navigate(route, navOptions {
        // 先清空其他栈，使得返回时能直接回到主界面
        if (popUpToMain) {
            popUpTo(TranslateScreen.MainScreen.route) {
                saveState = true
                inclusive = false
                //currentScreen = TranslateScreen.MainScreen
            }
        }
        //从名字就能看出来 跟activity的启动模式中的SingleTop模式一样 避免在栈顶创建多个实例
        launchSingleTop = true
        //切换状态的时候保存页面状态
        restoreState = true
    })
}

// 跳转到翻译页面，并开始翻译
fun NavHostController.navigateToTextTrans(
    sourceText: String?,
    sourceLanguage: Language,
    targetLanguage: Language
) {
    if (sourceText?.isNotBlank() == true) {
        NeedToTransConfig = TranslationConfig(sourceText, sourceLanguage, targetLanguage)
    }
    this.navigate(
        route = TranslateScreen.MainScreen.route,
        navOptions {
            launchSingleTop = true
            popUpTo(TranslateScreen.MainScreen.route) {
                inclusive = false
            }
        })
}