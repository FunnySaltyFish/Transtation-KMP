package com.funny.translation.ui

/**
 * https://github.com/jeziellago/compose-markdown
 */

// TODO Change it to
// to get more customized
// https://github.com/takahirom/jetpack-compose-markdown/blob/master/app/src/main/java/com/github/takahirom/jetpackcomposemarkdown/Markdown.kt

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.funny.translation.helper.Context

@Composable
expect fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    onClick: (() -> Unit)? = null,
    // this option will disable all clicks on links, inside the markdown text
    // it also enable the parent view to receive the click event
    disableLinkMovementMethod: Boolean = false,
    onLinkClicked: ((Context, String) -> Unit)? = null,
    onTextLayout: ((numLines: Int) -> Unit)? = null
)

@Composable
private fun PreviewMDText() {
    val markdown = "### 注音  \nhəˈlō  \n### 定义  \n**惊叹词**   \n- used as a greeting or to begin a phone conversation.  \n  - hello there, Katie!  \n  \n**名词**   \n- an utterance of “hello”; a greeting.  \n  - she was getting polite nods and hellos from people  \n  \n**动词**   \n- say or shout “hello”; greet someone.  \n  - I pressed the phone button and helloed  \n"
    MarkdownText(markdown = markdown)

}