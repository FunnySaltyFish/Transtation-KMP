package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.strings.ResStrings

/**
 * A reusable composable for displaying a two-section list (selected and available items)
 * @param T The type of items in the list
 * @param selectedItems List of currently selected items
 * @param availableItems List of available items that can be selected
 * @param onItemSelected Called when an available item is selected
 * @param onItemUnselected Called when a selected item is unselected
 * @param itemContent Composable that displays an item
 */
@Composable
fun <T> TwoSectionList(
    selectedItems: List<T>,
    availableItems: List<T>,
    onItemSelected: (T) -> Unit,
    onItemUnselected: (T) -> Unit,
    modifier: Modifier = Modifier,
    maxSelectedNum: Int = Int.MAX_VALUE,
    itemContent: @Composable (T, Boolean) -> Unit
) {
    Column(modifier = modifier) {
        // Selected items section
        if (selectedItems.isNotEmpty()) {
            Text(
                text = ResStrings.selected_engines + "(${selectedItems.size}/$maxSelectedNum)",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedItems) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemUnselected(item) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            itemContent(item, true)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available items section
        Text(
            text = ResStrings.available_engines,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 4.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableItems) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        itemContent(item, false)
                    }
                }
            }
        }
    }
}