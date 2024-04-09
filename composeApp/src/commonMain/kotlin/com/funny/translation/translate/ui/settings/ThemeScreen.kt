package com.funny.translation.translate.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.widget.RadioTile
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.theme.ThemeConfig
import com.funny.translation.ui.theme.ThemeStaticColors
import com.funny.translation.ui.theme.ThemeType
import com.funny.translation.ui.touchToScale
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ThemeScreen() {
    CommonPage(
        title = ResStrings.theme,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        val themeType by ThemeConfig.sThemeType
        var selectedColorIndex by rememberDataSaverState<Int>(
            key = "key_color_theme_selected_index",
            initialValue = 0
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .touchToScale()
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ){
            LabelText(text = ResStrings.preview_theme_here, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(12.dp))
        RadioTile(text = ResStrings.default_str, selected = themeType == ThemeType.StaticDefault) {
            ThemeConfig.updateThemeType(ThemeType.StaticDefault)
        }
        RadioTile(text = ResStrings.dynamic_color, selected = themeType.isDynamic) {
            ThemeConfig.updateThemeType(ThemeType.DynamicNative)
        }
        RadioTile(text = ResStrings.custom, selected = themeType is ThemeType.StaticFromColor) {
            ThemeConfig.updateThemeType(ThemeType.StaticFromColor(ThemeStaticColors[selectedColorIndex]))
        }
        Divider()
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedContent(targetState = themeType.id) { id ->
            when(id) {
                // 使用动态
                0, 1 -> SelectDynamicTheme(modifier = Modifier.fillMaxWidth())
                2 -> {
                    SelectColorTheme(
                        modifier = Modifier.fillMaxWidth(),
                        ThemeStaticColors,
                        { selectedColorIndex }
                    ) { index ->
                        selectedColorIndex = index
                        ThemeConfig.updateThemeType(ThemeType.StaticFromColor(ThemeStaticColors[index]))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun LabelText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@Composable
expect fun SelectDynamicTheme(modifier: Modifier)


@Composable
private fun SelectColorTheme(modifier: Modifier, colors: ImmutableList<Color>, selectedColorIndexProvider: () -> Int, updateSelectColorIndex: (Int) -> Unit) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(8),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        val selectedId = selectedColorIndexProvider()
        // 以圆形展示颜色们，可选择
        itemsIndexed(colors) { i, color ->
            Box(modifier = Modifier
                .background(color, CircleShape)
                .clip(CircleShape)
                .clickable {
                    if (!DefaultVipInterceptor()) return@clickable
                    updateSelectColorIndex(i)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val w = constraints.maxWidth
                    layout(w, w) {
                        placeable.placeRelative(0, 0)
                    }
                }, contentAlignment = Alignment.Center
            ) {
                if (i == selectedId) {
                    FixedSizeIcon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(0.dp, 6.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

