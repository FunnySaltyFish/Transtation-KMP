package com.funny.translation.translate.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.ifThen
import com.funny.translation.ui.FixedSizeIcon

// 改自 https://github.com/yangqi1024/jetpack-compose-ui/

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoticeBar(
    modifier: Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    singleLine: Boolean = false,
    showClose: Boolean = false,
    scrollable: Boolean = singleLine,
    iconSize: Dp = 16.dp,
    prefixIcon: ImageVector? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Clip
) {
    var show by rememberSaveable {
        mutableStateOf(true)
    }


    if (show) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            prefixIcon?.let {
                FixedSizeIcon(
                    it,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(iconSize)
                )
            }

            Text(
                text = text,
                color = color,
                overflow = overflow,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                modifier = Modifier
                    .ifThen(scrollable, Modifier.basicMarquee())
                    .weight(1f)
                    .padding(horizontal = 5.dp)
                    .verticalScroll(rememberScrollState()),
                style = style
            )
            if (showClose) {
                Spacer(Modifier.width(2.dp))
                FixedSizeIcon(
                    Icons.Default.Close,
                    contentDescription = "close",
                    tint = color,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.Top)
                        .clickable {
                            show = false
                        },
                )
            }
        }
    }
}

@Stable
@Composable
fun Modifier.noticeBarModifier(
    onClick: SimpleAction
) = this.heightIn(max = 360.dp)
    .clickable(onClick = onClick, indication = null, interactionSource = null)
    .background(
        MaterialTheme.colorScheme.primaryContainer,
        RoundedCornerShape(8.dp)
    )
    .padding(8.dp)
    .animateContentSize()