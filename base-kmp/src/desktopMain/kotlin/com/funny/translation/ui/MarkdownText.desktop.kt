package com.funny.translation.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.Context
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.markdownColor
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.markdownTypography
import org.intellij.lang.annotations.Language

private val h1Style = buildTextStyle(fontSize = 28.sp, fontWeight = FontWeight.W700)
private val h2Style = buildTextStyle(fontSize = 24.sp, fontWeight = FontWeight.W600)
private val h3Style = buildTextStyle(fontSize = 20.sp, fontWeight = FontWeight.W600)
private val h4Style = buildTextStyle(fontSize = 18.sp, fontWeight = FontWeight.W500)
private val h5Style = buildTextStyle(fontSize = 16.sp, fontWeight = FontWeight.W500)

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
    SelectableWrapper(selectable) {
        Markdown(
            modifier = modifier.padding(8.dp),
            content = markdown,
            colors = markdownColor(
                text = color
            ),
            typography = markdownTypography(
                h1 = h1Style,
                h2 = h2Style,
                h3 = h3Style,
                h4 = h4Style,
                h5 = h5Style
            ),
            padding = markdownPadding(
                list = 1.dp,
                indentList = 4.dp,
                block = 1.dp
            )
        )
    }
}

@Composable
private fun SelectableWrapper(
    selectable: Boolean,
    content: @Composable () -> Unit
) {
    if (selectable) {
        SelectionContainer(content = content)
    } else {
        content()
    }
}


val a = 8.sp

private fun buildTextStyle(fontSize: TextUnit, fontWeight: FontWeight = FontWeight.Normal) =
    TextStyle(
        fontSize = fontSize,
        fontWeight = fontWeight,
    )

@Language("Markdown")
const val markdown = "### 注音  \nhəˈlō  \n### 定义  \n**惊叹词**   \n- used as a greeting or to begin a phone conversation.  \n  - hello there, Katie!  \n  \n**名词**   \n- an utterance of “hello”; a greeting.  \n  - she was getting polite nods and hellos from people  \n  \n**动词**   \n- say or shout “hello”; greet someone.  \n  - I pressed the phone button and helloed  \n"

@Composable
@Preview
fun MarkdownPreview() {
    MarkdownText(markdown)
}