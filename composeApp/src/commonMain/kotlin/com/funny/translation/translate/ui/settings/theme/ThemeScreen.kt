package com.funny.translation.translate.ui.settings.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.settings.DefaultVipInterceptor
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.TransProIcon
import com.funny.translation.ui.theme.LightDarkMode
import com.funny.translation.ui.theme.ThemeConfig
import com.funny.translation.ui.theme.ThemeStaticColors
import com.funny.translation.ui.theme.ThemeType
import com.funny.translation.ui.theme.supportDynamicTheme

@Composable
fun ThemeScreen() {
    var isPreviewExpanded by rememberStateOf(false)
    val themeType by ThemeConfig.sThemeType
    var selectedColorIndex by rememberDataSaverState<Int>(
        key = "key_color_theme_selected_index",
        initialValue = 0
    )

    CommonPage(
        modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        title = ResStrings.theme,
    ) {
        // Theme Preview Section
        ThemePreviewSection(
            isExpanded = isPreviewExpanded,
            onExpandChange = { isExpanded -> isPreviewExpanded = isExpanded }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Type Selection
        ThemeTypeSelection(
            currentThemeType = themeType,
            selectedColorIndex = selectedColorIndex,
            onThemeTypeSelected = { newType ->
                ThemeConfig.updateThemeType(newType)
            },
            onColorIndexSelected = { index ->
                selectedColorIndex = index
                ThemeConfig.updateThemeType(ThemeType.StaticFromColor(ThemeStaticColors[index]))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Light/Dark Mode Selection
        LightDarkModeSelection()
    }
}

@Composable
private fun ThemePreviewSection(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onExpandChange(!isExpanded) }.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ResStrings.preview_theme_here,
                style = MaterialTheme.typography.titleMedium
            )
            FixedSizeIcon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        // Expanded Preview Content
        if (isExpanded) {
            ThemePreviewContent(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun ThemePreviewContent(
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Colors Preview
        ColorTokensPreview()

        Spacer(modifier = Modifier.height(8.dp))

        // Components Preview
        ComponentsPreview()
    }
}

@Composable
private fun ColorTokensPreview() {
    val colorScheme = MaterialTheme.colorScheme
    val tokenPairs = listOf(
        "Primary" to (colorScheme.primary to colorScheme.onPrimary),
        "Secondary" to (colorScheme.secondary to colorScheme.onSecondary),
        "Tertiary" to (colorScheme.tertiary to colorScheme.onTertiary),
        "Error" to (colorScheme.error to colorScheme.onError),
        "Background" to (colorScheme.background to colorScheme.onBackground),
        "Surface" to (colorScheme.surface to colorScheme.onSurface),
        "SurfaceVariant" to (colorScheme.surfaceVariant to colorScheme.onSurfaceVariant),
    )

    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tokenPairs, key = { it.first }) { (name, color) ->
            val (colorValue, colorOnValue) = color
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colorValue, CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        ClipBoardUtil.copy("$name: ${colorValue.toHex()}\non${name}: ${colorOnValue.toHex()}")
                        context.toastOnUi(ResStrings.copied_to_clipboard)
                    }
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Center),
                    color = colorOnValue,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
private fun ThemeTypeSelection(
    currentThemeType: ThemeType,
    selectedColorIndex: Int,
    onThemeTypeSelected: (ThemeType) -> Unit,
    onColorIndexSelected: (Int) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    Group(
        title = ResStrings.theme_type,
    ) {
        // Default Theme Option
        ThemeOption(
            title = ResStrings.default_str,
            subtitle = ResStrings.default_theme_description,
            selected = currentThemeType == ThemeType.StaticDefault,
            onClick = { onThemeTypeSelected(ThemeType.StaticDefault) }
        )

        // Dynamic Theme Option (Only for supported platforms)
        if (supportDynamicTheme()) {
            ThemeOption(
                title = ResStrings.dynamic_color,
                subtitle = ResStrings.dynamic_theme_description,
                selected = currentThemeType == ThemeType.DynamicNative,
                onClick = { onThemeTypeSelected(ThemeType.DynamicNative) }
            )
        }

        // Custom Color Theme Option
        ThemeOption(
            title = ResStrings.custom,
            subtitle = ResStrings.custom_theme_description,
            selected = currentThemeType is ThemeType.StaticFromColor,
            isVip = true,
            onClick = { showColorPicker = true }
        )

        ThemeOption(
            title = ResStrings.image_theme,
            subtitle = ResStrings.image_theme_description,
            selected = currentThemeType is ThemeType.DynamicFromImage,
            isVip = true,
            onClick = { showImagePicker = true }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            selectedIndex = selectedColorIndex,
            onColorSelected = { index ->
                onColorIndexSelected(index)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showImagePicker) {
        ImagePickerDialog(
            selectedUri = (currentThemeType as? ThemeType.DynamicFromImage)?.uri,
            onThemeTypeSelected = onThemeTypeSelected,
            onDismiss = { showImagePicker = false }
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    isVip: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isVip) TransProIcon(modifier = Modifier.padding(start = 4.dp).size(20.dp))
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun ColorPickerDialog(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Group(
            title = ResStrings.select_theme_color,
        ) {
            // Optimized Color Grid using LazyVerticalGrid with key
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                itemsIndexed(
                    items = ThemeStaticColors,
                    key = { index, color -> "${color.value}_$index" }
                ) { index, color ->
                    ColorItem(
                        color = color,
                        selected = index == selectedIndex,
                        onSelect = {
                            if (!DefaultVipInterceptor()) return@ColorItem
                            onColorSelected(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorItem(
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onSelect)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            FixedSizeIcon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun LightDarkModeSelection() {
    val currentMode by ThemeConfig.lightDarkMode

    Group(
        title = ResStrings.appearance
    ) {
        LightDarkMode.entries.forEach { mode ->
            key(mode) {
                ThemeOption(
                    title = mode.desc,
                    subtitle = when (mode) {
                        LightDarkMode.Light -> ResStrings.always_light_description
                        LightDarkMode.Dark -> ResStrings.always_dark_description
                        LightDarkMode.System -> ResStrings.follow_system_description
                    },
                    selected = currentMode == mode,
                    onClick = { ThemeConfig.updateLightDarkMode(mode) }
                )
            }
        }
    }
}


@Composable
private fun Group(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(4.dp),
                color = LocalContentColor.current
            )
            content()
        }
    }
}

private fun Color.toHex(): String {
    return "#${Integer.toHexString(this.toArgb()).uppercase()}"
}

private val CardColor
    @Composable
    get() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )