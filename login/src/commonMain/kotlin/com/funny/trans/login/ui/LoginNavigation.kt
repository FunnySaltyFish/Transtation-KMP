package com.funny.trans.login.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.LocalNavController
import com.funny.translation.kmp.NavGraphBuilder
import com.funny.translation.kmp.NavHost
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.animateComposable
import com.funny.translation.kmp.rememberNavController

sealed class LoginRoute(val route: String) {
    data object LoginPage: LoginRoute("login_page")
    data object ResetPasswordPage: LoginRoute("reset_password")
    data object FindUsernamePage: LoginRoute("find_user_name")
    data object ChangeUsernamePage: LoginRoute("change_user_name")
    data object CancelAccountPage: LoginRoute("cancel_account")
    data object AIPointCostPage: LoginRoute("ai_point_cost")
}

@Composable
fun LoginNavigation(
    onLoginSuccess: (UserInfoBean) -> Unit,
) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = LoginRoute.LoginPage.route,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            addLoginRoutes(navController, onLoginSuccess = onLoginSuccess)
        }
    }
}

fun NavGraphBuilder.addLoginRoutes(
    navController: NavHostController,
    onLoginSuccess: (UserInfoBean) -> Unit,
){
    animateComposable(LoginRoute.LoginPage.route){
        LoginPage(navController = navController, onLoginSuccess = onLoginSuccess)
    }
    animateComposable(LoginRoute.ResetPasswordPage.route){
        ResetPasswordPage(navController = navController)
    }
    animateComposable(LoginRoute.FindUsernamePage.route){
        FindUsernamePage()
    }
    animateComposable(LoginRoute.ChangeUsernamePage.route){
        ChangeUsernamePage(navController = navController)
    }
    animateComposable(LoginRoute.CancelAccountPage.route){
        CancelAccountPage(navController = navController)
    }
    animateComposable(LoginRoute.AIPointCostPage.route) {
        AIPointCostPage()
    }
}