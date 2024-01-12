package com.funny.translation.codeeditor.ui

import androidx.compose.runtime.Composable
import com.funny.translation.codeeditor.ui.editor.ComposeCodeEditor
import com.funny.translation.codeeditor.ui.runner.ComposeCodeRunner
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.kmp.NavHost
import com.funny.translation.kmp.composable
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.kmp.viewModel

const val TAG = "AppNav"
sealed class Screen(val route:String){
    object ScreenCodeEditor : Screen("nav_code_editor")
    object ScreenCodeRunner : Screen("nav_code_runner")
}

@Composable
fun CodeEditorNavigation() {
    val navController = rememberNavController()
    val activityCodeViewModel : ActivityCodeViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.ScreenCodeEditor.route
    ){
        composable(
            Screen.ScreenCodeEditor.route,
            deepLinks = listOf("funny://translate/code_editor")
        ){
            ComposeCodeEditor(navController = navController,activityViewModel = activityCodeViewModel)
        }
        composable(Screen.ScreenCodeRunner.route){
            ComposeCodeRunner(navController = navController,activityCodeViewModel = activityCodeViewModel)
        }
    }
}

