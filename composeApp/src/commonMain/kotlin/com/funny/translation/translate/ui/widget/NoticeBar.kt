package com.funny.translation.translate.ui.widget

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.LaunchedEffect
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
import com.funny.data_saver.core.getLocalDataSaverInterface
import com.funny.translation.bean.rememberSaveableRef
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.now
import com.funny.translation.helper.toast
import com.funny.translation.kmp.ifThen
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon

// 改自 https://github.com/yangqi1024/jetpack-compose-ui/

/**
 * 通知条
 *
 * @param dismissForeverKey 本地持久化的 key，如果非空，则会在关闭时认为相同 text 的通知条不需要再次显示
 */
@Composable
fun NoticeBar(
    modifier: Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    singleLine: Boolean = false,
    showClose: Boolean = false,
    scrollable: Boolean = singleLine,
    dismissForeverKey: String? = null,
    iconSize: Dp = 16.dp,
    prefixIcon: ImageVector? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val localKey = dismissForeverKey?.let { "__notice_read__$it" }
    val dataSaver = getLocalDataSaverInterface()
    val hasRead = (localKey != null && dataSaver.readData(localKey, "") == text)
    if (hasRead) return

    var show by rememberSaveable {
        mutableStateOf(!hasRead)
    }

    if (show) {
        var showTime by rememberSaveableRef(now())
        LaunchedEffect(singleLine) {
            if (!singleLine) showTime = now()
        }
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
                text = if (scrollable) text.replace('\n', '\t') else text,
                color = color,
                overflow = overflow,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                modifier = Modifier
                    .ifThen(scrollable) { basicMarquee() }
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
                            // 如果是多行通知条，展开了5秒以上，则以后不再弹出相同内容
                            if (!singleLine && localKey != null && now() - showTime >= 5000L) {
                                dataSaver.saveData(localKey, text)
                                toast(ResStrings.the_notice_is_read)
                            }
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