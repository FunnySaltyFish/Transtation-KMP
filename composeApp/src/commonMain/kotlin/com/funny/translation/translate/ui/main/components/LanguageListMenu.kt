package com.funny.translation.translate.ui.main.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.funny.translation.translate.Language
import com.funny.translation.translate.enabledLanguages

@Composable
internal fun LanguageListMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    updateLanguage: (Language) -> Unit
) {
    val languages by enabledLanguages.collectAsState()
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        languages.forEach {
            DropdownMenuItem(onClick = {
                updateLanguage(it)
                onDismissRequest()
            }, text = {
                Text(it.displayText)
            })
        }
    }
}