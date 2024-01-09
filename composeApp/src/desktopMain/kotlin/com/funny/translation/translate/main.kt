package com.funny.translation.translate

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.WindowHolder
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.translate.ui.App

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
fun main() = application {
    App {
        WindowHolder {
            addWindow(TransActivity::class.java, rememberWindowState(), show = true, {
                exitApplication()
            }) {
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
