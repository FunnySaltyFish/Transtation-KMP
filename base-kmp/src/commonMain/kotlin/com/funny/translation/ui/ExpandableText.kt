package com.funny.translation.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import com.funny.translation.helper.rememberSaveableStateOf

@OptIn(ExperimentalTextApi::class)
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLine: Int = 3,
    expandStateText: String,
    collapseStateText: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    expandTextStyle: TextStyle = textStyle.copy(
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
    )
) {
    var isExpanded by rememberSaveableStateOf(false)
    var expandable by rememberSaveableStateOf(false)
    var lastVisibleCharIndex by rememberSaveableStateOf(0)

    val (showMoreText, showLessText) = (expandStateText to collapseStateText)
    val toggleTextStyle = expandTextStyle.toSpanStyle()
    val density = LocalDensity.current
    val textHeight = remember(textStyle, collapsedMaxLine) {
        with(density) {
            textStyle.lineHeight.times(collapsedMaxLine).toDp()
        }
    }

    val annotatedText = buildAnnotatedString {
        if (expandable) {
            if (isExpanded) {
                append(text)

                withAnnotation("TAG_TOGGLE", "") {
                    withStyle(style = toggleTextStyle) { append(showLessText) }
                }
            } else {
                val textWithToggleSpace = text.substring(startIndex = 0, endIndex = lastVisibleCharIndex - showMoreText.length)
                append(textWithToggleSpace)

                withAnnotation("TAG_TOGGLE", "") {
                    withStyle(style = toggleTextStyle) { append(showMoreText) }
                }
            }
        } else {
            append(text)
        }
    }

    Column(
        modifier = Modifier.then(modifier),
    ) {
        ClickableText(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if (isExpanded) Dp.Unspecified else textHeight)
                .animateContentSize(),
            onClick = { offset ->
                val annotations = annotatedText.getStringAnnotations("TAG_TOGGLE", offset, offset)
                annotations.firstOrNull()?.let {
                    isExpanded = !isExpanded
                }
            },
            text = annotatedText,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.didOverflowHeight) {
                    expandable = true
                    lastVisibleCharIndex = textLayoutResult.getLineEnd(collapsedMaxLine - 1)
                }
            },
            style = textStyle
        )
    }
}
