package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.funny.jetsetting.core.ui.throttleClick
import com.funny.translation.ui.FixedSizeIcon

@Composable
fun RadioTile(
    text: String,
    selected: Boolean,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().throttleClick(onClick = onClick),
        headlineContent = {
            Text(text)
        },
        trailingContent = {
            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@Composable
fun ArrowTile(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().throttleClick(onClick = onClick),
        headlineContent = {
            Text(text)
        },
        trailingContent = {
            FixedSizeIcon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textColor)
        }
    )
}