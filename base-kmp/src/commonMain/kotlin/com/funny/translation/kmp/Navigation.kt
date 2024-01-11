package com.funny.translation.kmp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition

// adapt androidx.navigation to precompose's navigation

typealias NavController = Navigator
typealias NavHostController = Navigator
typealias NavGraphBuilder = RouteBuilder
typealias NavDeepLink = String
typealias NavBackStackEntry = BackStackEntry
typealias NamedNavArgument = String

@Composable
fun rememberNavController() = rememberNavigator()

fun NavController.navigateUp() {
    popBackStack()
}

fun NavGraphBuilder.composable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (() -> EnterTransition) =
        { fadeIn(animationSpec = tween(700)) },
    exitTransition: (() -> ExitTransition) =
        { fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (() -> EnterTransition) =
        enterTransition,
    popExitTransition: (() -> ExitTransition) =
        exitTransition,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    scene(
        route,
        deepLinks,
        navTransition = NavTransition(
            createTransition = enterTransition(),
            destroyTransition = exitTransition(),
            pauseTransition = popExitTransition(),
            resumeTransition = popEnterTransition(),
        ),
    ) {
        content(it)
    }
}

// navigation(
//        startDestination = TranslateScreen.LongTextTransScreen.route,
//        route = "nav_1_long_text_trans"
//    ) {
fun NavGraphBuilder.navigation(
    startDestination: String,
    route: String,
    enterTransition: (() -> EnterTransition) =
        { fadeIn(animationSpec = tween(700)) },
    exitTransition: (() -> ExitTransition) =
        { fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (() -> EnterTransition) =
        enterTransition,
    popExitTransition: (() -> ExitTransition) =
        exitTransition,
    builder: NavGraphBuilder.() -> Unit,
) {
    group(
        route,
        initialRoute = startDestination,
    ) {
        builder()
    }
}

@Composable
fun NavHost(
    navController: NavController,
    startDestination: String,
    modifier: Modifier = Modifier,
    enterTransition: (() -> EnterTransition) =
        { fadeIn(animationSpec = tween(700)) },
    exitTransition: (() -> ExitTransition) =
        { fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (() -> EnterTransition) =
        enterTransition,
    popExitTransition: (() -> ExitTransition) =
        exitTransition,
    builder: NavGraphBuilder.() -> Unit,
) {
    val currentEntry by navController.currentEntry.collectAsState(null)
    AnimatedContent(currentEntry) {
        moe.tlaster.precompose.navigation.NavHost(
            navigator = navController,
            initialRoute = startDestination,
            modifier = modifier,
            navTransition = NavTransition(
                createTransition = enterTransition(),
                destroyTransition = exitTransition(),
                pauseTransition = popExitTransition(),
                resumeTransition = popEnterTransition(),
            ),
            persistNavState = true,
            builder = builder,
        )
    }
}

const val NAV_ANIM_DURATION = 500

fun NavGraphBuilder.animateComposable(
    route: String,
    animDuration: Int = NAV_ANIM_DURATION,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        }
    ) { entry ->
        content(entry)
    }
}

