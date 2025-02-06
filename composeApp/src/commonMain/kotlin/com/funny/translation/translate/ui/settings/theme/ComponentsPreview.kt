package com.funny.translation.translate.ui.settings.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.SpacerWidth

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ComponentsPreview() {
    var textFieldValue by remember { mutableStateOf("") }
    var switchChecked by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0f) }
    var radioSelected by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val textStyle = MaterialTheme.typography.titleSmall
        // Buttons Section
        Text("Buttons", style = textStyle)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {}) {
                FixedSizeIcon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Button")
            }

            ElevatedButton(onClick = {}) {
                Text("Elevated")
            }

            OutlinedButton(onClick = {}) {
                Text("Outlined")
            }

            TextButton(onClick = {}) {
                Text("Text Button")
            }

            FilledTonalButton(onClick = {}) {
                Text("Tonal")
            }
        }

        // Text Fields Section
        Text("Text Fields", style = textStyle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Outlined") },
                modifier = Modifier.weight(1f)
            )

            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Filled") },
                modifier = Modifier.weight(1f)
            )
        }

        // Selection Controls
        Text("Selection Controls", style = textStyle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = switchChecked,
                    onCheckedChange = { switchChecked = it }
                )
                SpacerWidth(8.dp)
                Text("Switch")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = switchChecked,
                    onCheckedChange = { switchChecked = it }
                )
                Text("Checkbox")
            }
        }

        // Slider
        Text("Slider", style = textStyle)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Radio Buttons
        Text("Radio Buttons", style = textStyle)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = radioSelected == it,
                        onClick = { radioSelected = it }
                    )
                    Text("Option ${it + 1}")
                }
            }
        }

        // Cards
        Text("Cards", style = textStyle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.weight(1f),
                onClick = {}
            ) {
                Text(
                    "Elevated Card",
                    modifier = Modifier.padding(16.dp)
                )
            }

            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = {}
            ) {
                Text(
                    "Outlined Card",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Dropdown Menu
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text("Show Menu")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Option 1") },
                    onClick = { expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Option 2") },
                    onClick = { expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Option 3") },
                    onClick = { expanded = false }
                )
            }
        }
    }
}