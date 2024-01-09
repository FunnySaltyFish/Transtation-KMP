package com.funny.translation.translate

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.translate.ui.App
import com.funny.translation.translate.utils.InitUtil
import com.funny.translation.translate.utils.initCommon
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
fun main() {
    init()
    application {
        App {
//            WindowHolder {
//                addWindow<TransActivity>(rememberWindowState(), show = true, {
//                    exitApplication()
//                }) {
//                    val transActivity = ActivityManager.findActivity<TransActivity>() ?: return@addWindow
//                    CompositionLocalProvider(LocalActivityVM provides transActivity.activityViewModel) {
//                        AppNavigation(navController = rememberNavController().also {
//                            ActivityManager.findActivity<TransActivity>()?.navController = it
//                        }, exitAppAction = ::exitApplication)
//                    }
//                }
//            }
            Window(onCloseRequest = ::exitApplication) {
                CompositionLocalProvider(LocalActivityVM provides TransActivity().activityViewModel) {
                    AppNavigation(navController = rememberNavController().also {
                        ActivityManager.findActivity<TransActivity>()?.navController = it
                    }, exitAppAction = ::exitApplication)
                }
            }
        }
        Unit
//    Window(onCloseRequest = ::exitApplication) {
//        Text("Hello World")
//    }
    }
}

private fun init() {
    LocaleUtils.init(appCtx)
    runBlocking {
        InitUtil.initCommon()
    }
}