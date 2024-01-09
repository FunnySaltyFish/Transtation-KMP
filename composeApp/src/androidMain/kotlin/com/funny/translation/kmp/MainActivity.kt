@file:OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)

package com.funny.translation.kmp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import com.funny.translation.BaseActivity
import com.funny.translation.translate.AppNavigation
import com.funny.translation.translate.ui.App


class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App {
                AppNavigation(
                    navController = rememberNavController().also {  },
                    exitAppAction = { this@MainActivity.finish() }
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}