package com.funny.translation.translate.ui.ai

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.funny.translation.helper.LocalContext
import com.funny.translation.kmp.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChatScreen() {
    val context = LocalContext.current
    // val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val viewModel: VoiceChatViewModel = viewModel()

    // 处理动态权限
    LaunchedEffect(Unit) {
//        if (!permissionState.hasPermission) {
//            permissionState.launchPermissionRequest()
//        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "${viewModel.modelName} - ${viewModel.conversationTime}")
                },
                actions = {
                    IconButton(onClick = { viewModel.showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                IconButton(onClick = { viewModel.toggleMicrophone() }) {
                    Icon(
                        if (viewModel.isMicrophoneOn) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.endConversation() }) {
                    Icon(Icons.Default.Stop, contentDescription = "End Conversation")
                }
            }
        },
        containerColor = Color.Black,
        content = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // 中间的圆形动画
                ConversationCircle(viewModel = viewModel)

                // 额外信息的弹出层
                if (viewModel.showExtraInfo) {
                    ExtraInfoOverlay(viewModel = viewModel)
                }

                // 设置面板
                if (viewModel.showSettings) {
                    SettingsPanel(viewModel = viewModel)
                }
            }
        }
    )
}

@Composable
fun ConversationCircle(viewModel: VoiceChatViewModel) {
    // 根据聊天状态显示不同的动画
    val animationState = remember { Animatable(0f) }

    LaunchedEffect(viewModel.isMicrophoneOn, viewModel.isThinking) {
        if (viewModel.isMicrophoneOn || viewModel.isThinking) {
            animationState.animateTo(1f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ))
        } else {
            animationState.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.size(200.dp)) {
        drawCircle(
            color = Color.White,
            radius = 100.dp.toPx() * animationState.value
        )
    }
}

@Composable
fun ExtraInfoOverlay(viewModel: VoiceChatViewModel) {
    // 定时自动隐藏
    LaunchedEffect(Unit) {
        delay(3000)
        viewModel.showExtraInfo = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { viewModel.showExtraInfo = false },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "剩余点数：${viewModel.remainingPoints}", color = Color.White)
            Text(text = "预估剩余时间：${viewModel.estimatedRemainingTime}", color = Color.White)
        }
    }
}

@Composable
fun SettingsPanel(viewModel: VoiceChatViewModel) {
    // 设置项，例如声音、模型、语速等
    Dialog(onDismissRequest = { viewModel.showSettings = false }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "设置", style = MaterialTheme.typography.titleSmall)
                // TODO: 添加声音、模型、语速等设置控件
            }
        }
    }
}
