package com.funny.translation.translate

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.funny.trans.login.LoginActivity
import com.funny.trans.login.ui.LoginNavigation
import com.funny.translation.AppConfig
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.Log
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.WindowHolder
import com.funny.translation.kmp.addWindow
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.activity.ErrorDialog
import com.funny.translation.translate.activity.ErrorDialogActivity
import com.funny.translation.translate.utils.DesktopUncaughtExceptionHandler
import com.funny.translation.translate.utils.InitUtil
import com.funny.translation.translate.utils.initCommon
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
fun main() {
    init()
    application {
        WindowHolder {
            addWindow<TransActivity>(
                rememberWindowState(
                    placement = WindowPlacement.Floating,
                    width = 900.dp,
                    height = 600.dp,
                ),
                show = true,
                onCloseRequest = { exitApplication() },
            ) { transActivity ->
                CompositionLocalProvider(LocalActivityVM provides transActivity.activityViewModel) {
                    AppNavigation(navController = rememberNavController().also {
                        ActivityManager.findActivity<TransActivity>()?.navController = it
                    }, exitAppAction = ::exitApplication)
                }
            }

            addWindow<LoginActivity>(
                rememberWindowState(
                    placement = WindowPlacement.Floating,
                    width = 360.dp,
                    height = 700.dp,
                ),
                title = ResStrings.login_or_register,
            ) { loginActivity ->
                LoginNavigation(
                    onLoginSuccess = {
                        Log.d("Login", "登录成功: 用户: $it")
                        if (it.isValid()) AppConfig.login(it, updateVipFeatures = true)
                        loginActivity.finish()
                    }
                )
            }

            addWindow<ErrorDialogActivity>(
                rememberWindowState(),
                title = ResStrings.crash,
                show = false,
                onCloseRequest = ::exitApplication,
            ) { errorDialogActivity ->
                errorDialogActivity.crashMessage?.let {
                    ErrorDialog(
                        crashMessage = it,
                        destroy = errorDialogActivity::destroy
                    )
                }
            }
        }
    }
}

private fun init() {
    LocaleUtils.init(appCtx)
    DesktopUncaughtExceptionHandler.init(appCtx)
    runBlocking {
        InitUtil.initCommon()
    }
}