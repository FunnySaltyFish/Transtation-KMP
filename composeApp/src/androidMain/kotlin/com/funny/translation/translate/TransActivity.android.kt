@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.funny.translation.translate

import App
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import com.funny.translation.BaseActivity
import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.rememberNavController
import kotlin.properties.Delegates

actual class TransActivity : BaseActivity(), KMPActivity {
    actual var navController: NavController by Delegates.notNull()
    private lateinit var activityViewModel: ActivityViewModel

    private var initialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!initialized) {
            activityViewModel = ActivityViewModel()
            initLanguageDisplay()
            initialized = true
        }

        setContent {
            App {
                // 此处通过这种方式传递 Activity 级别的 ViewModel，以确保获取到的都是同一个实例
                CompositionLocalProvider(LocalActivityVM provides activityViewModel) {
                    AppNavigation(
                        navController = rememberNavController().also {
                            this@TransActivity.navController = it
                        },
                        exitAppAction = { this@TransActivity.finish() }
                    )
                }
            }
        }
    }
}