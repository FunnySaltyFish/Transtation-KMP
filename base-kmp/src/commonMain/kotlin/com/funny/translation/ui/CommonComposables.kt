package com.funny.translation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.Platform
import com.funny.translation.kmp.base.strings.ResStrings
import com.funny.translation.kmp.currentPlatform
import com.funny.translation.translate.LocalNavController

/**
 * CommonPage，有一个 TopBar 以及剩余内容，被 Column 包裹
 * @param modifier Modifier
 * @param title String?
 * @param navController NavHostController
 * @param navigationIcon [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param actions TopBar 的 actions
 * @param content 主要内容
 */
@Composable
fun CommonPage(
    modifier: Modifier = Modifier,
    title: String? = null,
    addNavPadding: Boolean = true,
    navController: NavHostController = LocalNavController.current,
    navigationIcon: @Composable () -> Unit = { CommonNavBackIcon(navController) },
    actions: @Composable RowScope.() -> Unit = { },
    topBar: @Composable () -> Unit = {
        CommonTopBar(title = title, navigationIcon = navigationIcon, navController = navController, actions = actions)
    },
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        topBar()
        content()
        if (addNavPadding) {
            NavPaddingItem()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    modifier: Modifier = Modifier,
    title: String?,
    navController: NavHostController = LocalNavController.current,
    navigationIcon: @Composable () -> Unit = { CommonNavBackIcon(navController) },
    actions: @Composable RowScope.() -> Unit = { },
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(text = title, Modifier.padding(start = 12.dp))
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            actions()
            Spacer(modifier = Modifier.width(12.dp))
        }
    )
}

// TopBar that without background
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navController: NavHostController = LocalNavController.current,
    navigationIcon: @Composable () -> Unit = { CommonNavBackIcon(navController) },
    actions: @Composable RowScope.() -> Unit = { },
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(text = title, Modifier.padding(start = 12.dp))
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            actions()
            Spacer(modifier = Modifier.width(12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )
}

@Composable
fun CommonNavBackIcon(
    navController: NavHostController = LocalNavController.current,
    navigateBackAction: SimpleAction = { navController.popBackStack() }
) {
    IconButton(onClick = navigateBackAction) {
        FixedSizeIcon(
            Icons.Default.ArrowBack,
            contentDescription = ResStrings.back
        )
    }
}

/**
 * 纵向的空白，高度为底部导航栏的高度
 * 仅 Android 有效
 */
@Composable
fun NavPaddingItem() {
    if (currentPlatform == Platform.Android) {
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Bottom)))
    } else {
        // add some padding for Desktop
        Spacer(modifier = Modifier.height(8.dp))
    }
}

fun LazyListScope.navPaddingItem() {
    item {
        NavPaddingItem()
    }
}