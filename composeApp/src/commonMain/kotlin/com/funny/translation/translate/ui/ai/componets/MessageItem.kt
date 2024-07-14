package com.funny.translation.translate.ui.ai.componets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.sendByMe
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.IconWidget
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.Language
import com.funny.translation.translate.ui.main.SpeakButton
import com.funny.translation.translate.ui.widget.AsyncImage
import com.funny.translation.ui.MarkdownText

@Composable
internal fun MessageItem(
    modifier: Modifier = Modifier,
    maxWidth: Dp,
    chatMessage: ChatMessage,
    copyAction: SimpleAction,
    deleteAction: SimpleAction,
    refreshAction: SimpleAction? = null,
    previewImageAction: (String) -> Unit,
) {
    val sendByMe = chatMessage.sendByMe

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = if (sendByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .requiredWidthIn(0.dp, maxWidth),
            horizontalAlignment = if (sendByMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (sendByMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer, //Color(247,249,253),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
                    .animateContentSize()
            ) {
                when (chatMessage.type) {
                    ChatMessageTypes.TEXT -> {
                        if (sendByMe) {
                            Text(
                                text = chatMessage.content,
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            val color = if (chatMessage.error != null)
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                            val content = if (chatMessage.error != null) {
                                chatMessage.content + "\n" + chatMessage.error
                            } else {
                                chatMessage.content.ifEmpty { "thinking..." }
                            }
                            MarkdownText(
                                markdown = content,
                                color = color,
                                selectable = true
                            )
                        }
                    }

                    ChatMessageTypes.IMAGE -> {
                        val (image, size) = remember(chatMessage.content) {
                            chatMessage.content.split("@")
                        }
                        AsyncImage(model = image, modifier = Modifier.width(200.dp).clickable { previewImageAction(image) })
                    }

                    ChatMessageTypes.ERROR ->
                        Text(
                            text = chatMessage.error ?: "Unknown Error",
                            modifier = Modifier,
                            color = MaterialTheme.colorScheme.error
                        )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                Modifier
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                // speak / refresh / copy / delete
                if (chatMessage.type == ChatMessageTypes.TEXT) {
                    SpeakButton(
//                        modifier = Modifier.padding(4.dp),
                        text = chatMessage.content,
                        language = Language.AUTO,
                        boxSize = 28.dp,
                        iconSize = 20.dp
                    )
                    MessageItemMenuIcon(
                        icon = FunnyIcon(imageVector = Icons.Default.ContentCopy),
                        onClick = copyAction
                    )
                }
                if (refreshAction != null) {
                    MessageItemMenuIcon(
                        icon = FunnyIcon(imageVector = Icons.Default.Refresh),
                        onClick = refreshAction
                    )
                }
                MessageItemMenuIcon(
                    icon = FunnyIcon(imageVector = Icons.Default.Delete),
                    onClick = deleteAction
                )
            }
        }
    }
}

@Composable
private fun MessageItemMenuIcon(
    modifier: Modifier = Modifier,
    icon: FunnyIcon,
    onClick: SimpleAction
) {
    IconWidget(funnyIcon = icon, modifier = modifier.clickable(onClick = onClick).padding(4.dp).size(20.dp))
}