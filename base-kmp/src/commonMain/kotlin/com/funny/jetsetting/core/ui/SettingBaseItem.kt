package com.funny.jetsetting.core.ui

import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.funny.translation.ui.CenterListItem

@Composable
internal fun SettingBaseItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    CenterListItem(
        modifier = modifier.throttleClick(300, onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Unspecified
        ),
        leadingContent = icon,
        headlineContent = title,
        trailingContent = action,
        supportingContent = text
    )
}
