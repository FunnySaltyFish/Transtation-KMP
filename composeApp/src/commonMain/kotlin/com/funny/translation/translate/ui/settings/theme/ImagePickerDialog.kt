package com.funny.translation.translate.ui.settings.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.KMPContext
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.settings.DefaultVipInterceptor
import com.funny.translation.translate.ui.widget.AsyncImage
import com.funny.translation.translate.utils.rememberSelectImageLauncher
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.theme.ThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ImagePickerDialog(
    selectedUri: String?,
    onThemeTypeSelected: (ThemeType) -> Unit,
    onDismiss: () -> Unit
) {
    var recentImages by rememberDataSaverState<List<String>>(
        key = "recent_theme_images",
        initialValue = emptyList()
    )
    var currentSelectedUri: String? by rememberStateOf(selectedUri)
    val pickLauncher = rememberSelectImageLauncher(
        pickedItems = currentSelectedUri?.let { arrayListOf(it) } ?: emptyList()
    ) {
        if (it.isNotEmpty()) {
            val img = it[0]
            currentSelectedUri = img
        }
    }
    var loading by rememberStateOf(false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        confirmButton = {
            Button(
                onClick = onClick@ {
                    if (!DefaultVipInterceptor()) return@onClick
                    currentSelectedUri?.let {
                        loading = true
                        scope.launch(Dispatchers.IO) {
                            getColorFromImageUri(context, it)?.let { color ->
                                onThemeTypeSelected(ThemeType.DynamicFromImage(color, it))
                                // Add to recent images
                                if (!recentImages.contains(it)) {
                                    recentImages = (listOf(it) + recentImages).take(5)
                                }
                            }
                            loading = false
                            onDismiss()
                        }
                    }
                    if (currentSelectedUri == null) onDismiss()
                },
                enabled = currentSelectedUri != null
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(end = 8.dp).align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 1.dp
                    )
                }
                Text(ResStrings.message_confirm, modifier = Modifier.align(Alignment.CenterVertically))
            }
        },
        dismissButton = {
            if (!loading) {
                TextButton(onClick = onDismiss) {
                    Text(ResStrings.cancel)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = ResStrings.select_theme_image,
                    style = MaterialTheme.typography.titleLarge
                )

                // Image Selection Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { pickLauncher.launch(arrayOf("image/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        FixedSizeIcon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(ResStrings.select_from_image)
                    }
                }

                // Recent Images Section
                if (recentImages.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ResStrings.recent_images,
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                onClick = { recentImages = emptyList() }
                            ) {
                                Text(ResStrings.clear_history)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(recentImages) { uri ->
                                RecentImageItem(
                                    uri = uri,
                                    selected = uri == currentSelectedUri,
                                    onClick = {
                                        currentSelectedUri = uri
                                    }
                                )
                            }
                        }
                    }
                }

                // Preview of selected image
                currentSelectedUri?.let { uri ->
                    Column {
                        Text(
                            text = ResStrings.image_preview,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun RecentImageItem(
    uri: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

expect fun getColorFromImageUri(context: KMPContext, uri: String): Color?
