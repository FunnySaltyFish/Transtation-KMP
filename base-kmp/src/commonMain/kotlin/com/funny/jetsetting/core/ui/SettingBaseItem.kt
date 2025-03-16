package com.funny.jetsetting.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun SettingBaseItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    rememberSaveable(300, saver = ThrottleHandler.Saver) { ThrottleHandler(1000) }
    ListItem(
        modifier = modifier.throttleClick(300, onClick = onClick).height(IntrinsicSize.Max),
        colors = ListItemDefaults.colors(
            containerColor = Color.Unspecified
        ),
        leadingContent = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                icon?.invoke()
            }
        },
        headlineContent = title,
        trailingContent = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                action?.invoke()
            }
        },
        supportingContent = text
    )
}
