package com.funny.translation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.funny.translation.helper.Context

@Composable
actual fun MarkdownText(
    markdown: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    textAlign: TextAlign?,
    maxLines: Int,
    selectable: Boolean,
    style: TextStyle,
    onClick: (() -> Unit)?,
    // this option will disable all clicks on links, inside the markdown text
    // it also enable the parent view to receive the click event
    disableLinkMovementMethod: Boolean,
    onLinkClicked: ((Context, String) -> Unit)?,
    onTextLayout: ((numLines: Int) -> Unit)?
)  {

}