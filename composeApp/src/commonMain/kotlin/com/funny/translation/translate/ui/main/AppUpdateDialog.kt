package com.funny.translation.translate.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.createFileIfNotExist
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.fileMD5
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.rememberSaveableStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.BuildConfig
import com.funny.translation.translate.bean.UpdateInfo
import com.funny.translation.translate.bean.bytes
import com.funny.translation.translate.utils.InstallApkLauncher
import com.funny.translation.translate.utils.UpdateUtils
import com.funny.translation.translate.utils.getInstallApkFile
import com.funny.translation.translate.utils.rememberInstallApkLauncher
import com.funny.translation.ui.AnyPopDialog
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.floatingActionBarModifier
import com.funny.translation.ui.popDialogShape
import java.io.File
import kotlin.math.roundToInt

private var appDialogDisplayed = false

@Composable
fun AppUpdateDialog(
    modifier: Modifier = Modifier,
    updateInfo: UpdateInfo
) {
    if (appDialogDisplayed) {
        return
    }

    val apkSize = updateInfo.apk_size ?: 0
    val file = remember(updateInfo) { getInstallApkFile(updateInfo).also { it?.createFileIfNotExist() } } ?: return

    var progress: Float by rememberSaveableStateOf(if (apkSize == 0) 0f else file.length().toFloat() / apkSize)

    var showDialog by rememberSaveableStateOf(updateInfo.should_update)
    // 缩小显示为小的悬浮球
    var showFloatBall by rememberSaveableStateOf(false)

    val closeDialogAction = lambda@ {
        showDialog = false
        appDialogDisplayed = true
    }

    val forceUpdate = updateInfo.force_update == true
    val installLauncher = rememberInstallApkLauncher {
        // 回调并不准确
//        if (it) {
//            closeDialogAction()
//        } else {
//            appCtx.toastOnUi(ResStrings.install_failed)
//        }
    }

    if (showDialog) {
        AnyPopDialog(
            modifier = modifier.popDialogShape(),
            onDismissRequest = {
                if (!forceUpdate) {
                    if (progress > 0f) { // 如果下载已经开始了，则显示一个悬浮球
                        showFloatBall = true
                        showDialog = false
                    } else {
                        closeDialogAction()
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text(text = ResStrings.new_version_detected, style = MaterialTheme.typography.headlineSmall)
                Text(text = "v${updateInfo.version_name}(${updateInfo.version_code}) | ${updateInfo.apk_size?.bytes()}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                MarkdownText(
                    markdown = updateInfo.update_log ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())
                )
                ButtonLine(
                    modifier = Modifier.fillMaxWidth(),
                    installLauncher = installLauncher,
                    file = file,
                    forceUpdate = forceUpdate,
                    progressProvider = { progress },
                    updateProgress = { progress = it },
                    updateInfo = updateInfo,
                    closeDialogAction = closeDialogAction
                )
            }
        }
    } else if (showFloatBall) {
        FloatBall(
            modifier = modifier.floatingActionBarModifier(),
            installLauncher = installLauncher,
            file = file,
            progressProvider = { progress },
            updateProgress = { progress = it },
            openDialogAction = {
                showDialog = true
                showFloatBall = false
            }
        )

    }
}

@Composable
private fun ButtonLine(
    modifier: Modifier,
    installLauncher: InstallApkLauncher,
    file: File,
    forceUpdate: Boolean,
    progressProvider: () -> Float,
    updateProgress: (Float) -> Unit,
    updateInfo: UpdateInfo,
    closeDialogAction: SimpleAction
) {
    val context = LocalContext.current
    val progress = progressProvider()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        val color = MaterialTheme.colorScheme.primary

        // button
        if (!forceUpdate && progress == 0f) {
            TextButton(
                onClick = closeDialogAction,
                modifier = Modifier
            ) {
                Text(text = ResStrings.cancel)
            }
        }

        val onError = remember {
            { e: Exception ->
                context.toastOnUi(e.displayMsg(ResStrings.download))
                e.printStackTrace()
                file.delete()
                updateProgress(0f)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
        ) {
            TextButton(
                onClick = {
                    val url = if (updateInfo.apk_url != null) {
                        updateInfo.apk_url + "&from_source=app_${BuildConfig.FLAVOR}_${AppConfig.versionCode}"
                    } else "https://www.funnytraslation.fun/trans"
                    context.openUrl(url)
                }
            ) {
                Text(text = ResStrings.download_from_browser)
            }
            Spacer(Modifier.width(4.dp))
            TextButton(onClick = {
                UpdateUtils.downloadUpdate(
                    updateInfo = updateInfo,
                    file = file,
                    onProgressChanged = updateProgress,
                    onDownloadFinished = {
                        if (file.fileMD5() != updateInfo.apk_md5) {
                            onError(Exception("MD5 is incorrect"))
                        } else {
                            try {
                                installLauncher.launch(file)
                            } catch (e: Exception) {
                                onError(e)
                            }
                        }
                    },
                    onError = onError
                )
            }) {
                ProgressText(progress, color)
            }
        }
    }
}

// 悬浮球
@Composable
internal fun FloatBall(
    modifier: Modifier = Modifier,
    installLauncher: InstallApkLauncher,
    file: File,
    progressProvider: () -> Float,
    updateProgress: (Float) -> Unit,
    openDialogAction: SimpleAction,
) {
    val progress = progressProvider()
    val color = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    val onError = remember {
        { e: Exception ->
            context.toastOnUi(e.displayMsg(ResStrings.download))
            e.printStackTrace()
            file.delete()
            updateProgress(0f)
        }
    }

    val onClick = remember {
        {
            if (progress < 1f) {
                openDialogAction()
            } else if (progress == 1f) {
                try {
                    installLauncher.launch(file)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }


    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        shape = CircleShape,
        modifier = modifier.clip(CircleShape).clickable { onClick() },
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(48.dp),
                color = color,
                strokeWidth = 2.dp,
            )
            if (progress < 1f) {
                ProgressText(progress, color, fontSize = 10.sp)
            } else if (progress == 1f) {
                FixedSizeIcon(imageVector = Icons.Filled.ArrowForward, tint = color, modifier = Modifier.size(24.dp), contentDescription = "Install")
            }
        }
    }

}

@Composable
private fun ProgressText(progress: Float, color: Color, fontSize: TextUnit = LocalTextStyle.current.fontSize) {
    if (progress == 0f) {
        Text(text = ResStrings.download, color = color, fontSize = fontSize)
    } else if (progress < 1f) {
        // 保留两位小数
        val progressText = "${(progress * 10000).roundToInt() / 100f}%"
        Text(text = progressText, color = color, fontSize = fontSize)
    } else if (progress == 1f) {
        Text(text = ResStrings.install_now, color = color, fontSize = fontSize)
    }
}