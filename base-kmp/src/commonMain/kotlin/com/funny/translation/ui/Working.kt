package com.funny.translation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.kmp.base.strings.ResStrings

@Composable
fun Working() {
    CommonPage(
        title = "Working"
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = ResStrings.working, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(text = ResStrings.please_wait, style = MaterialTheme.typography.bodyLarge)
        }
    }
}