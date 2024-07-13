package com.funny.translation.translate.ui.ai.componets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon

@Composable
fun ChatInputTextField(
    modifier: Modifier,
    input: String,
    onValueChange: (String) -> Unit,
    sendAction: () -> Unit,
    clearAction: () -> Unit,
    chatBot: ModelChatBot,
    pickedItems: List<String>,
    showAddFilePanel: Boolean,
    updateShowAddFilePanel: (Boolean) -> Unit
) {
    val color = MaterialTheme.colorScheme.surface
    TextField(
        value = input,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(ResStrings.chat_input_hint) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = color,
            unfocusedContainerColor = color,
            disabledContainerColor = color,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            val clearBtnVisible by remember(input) {
                derivedStateOf {
                    input != ""
                }
            }
            AnimatedContent(
                targetState = clearBtnVisible,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) togetherWith
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                },
                label = ""
            ) { visible ->
                Row {
                    val context = LocalContext.current
                    val showComing = {
                        context.toastOnUi(ResStrings.comming_soon)
                    }
                    val button = @Composable { icon: ImageVector, contentDescription: String?, onClick: () -> Unit ->
                        IconButton(onClick = onClick) {
                            FixedSizeIcon(
                                modifier = Modifier.size(24.dp),
                                imageVector = icon,
                                contentDescription = contentDescription
                            )
                        }
                    }
                    if (visible) {
                        button(
                            Icons.Filled.Clear, ResStrings.clear_content
                        ) {
                            onValueChange("")
                        }
                        button(
                            Icons.Default.Send, ResStrings.send
                        ) {
                            sendAction()
                        }
                    } else {
                        // Clear
                        button(
                            Icons.Filled.CleaningServices, ResStrings.clear_content
                        ) {
                            clearAction()
                        }
                        if (pickedItems.isNotEmpty()) {
                            // send
                            button(
                                Icons.Default.Send, ResStrings.send
                            ) {
                                sendAction()
                            }
                        }
                        if (chatBot.model.inputFileTypes.supportImage) {
                            // Add
                            val rotateDegree by animateFloatAsState(targetValue = if (showAddFilePanel) 45f else 0f)
                            IconButton(
                                onClick = {
                                    updateShowAddFilePanel(!showAddFilePanel)
                                }
                            ) {
                                FixedSizeIcon(
                                    modifier = Modifier.size(24.dp).graphicsLayer {
                                        rotationZ = rotateDegree
                                    },
                                    imageVector = Icons.Default.Add,
                                    contentDescription = ResStrings.add_file
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}