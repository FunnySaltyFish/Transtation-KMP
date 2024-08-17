package com.funny.translation.translate.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.fileMD5
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.bean.UpdateInfo
import com.funny.translation.translate.bean.bytes
import com.funny.translation.translate.utils.UpdateUtils
import com.funny.translation.translate.utils.downloadedFile
import com.funny.translation.translate.utils.rememberInstallApkLauncher
import com.funny.translation.ui.AnyPopDialog
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.popDialogShape
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

    var showDialog by mutableStateOf(updateInfo.should_update)
    val closeDialogAction = {
        showDialog = false
        appDialogDisplayed = true
    }

    val forceUpdate = updateInfo.force_update == true

    if (showDialog) {
        AnyPopDialog(
            modifier = modifier.popDialogShape(),
            onDismissRequest = {
                if (!forceUpdate) {
                    closeDialogAction()
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text(text = ResStrings.new_version_detected, style = MaterialTheme.typography.headlineSmall)
                Text(text = "v${updateInfo.version_name}(${updateInfo.version_code}) | ${updateInfo.apk_size?.bytes()}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                MarkdownText(markdown = updateInfo.update_log ?: "", style = MaterialTheme.typography.bodySmall)
                ButtonLine(
                    modifier = Modifier.fillMaxWidth(),
                    forceUpdate = forceUpdate,
                    updateInfo = updateInfo,
                    closeDialogAction = closeDialogAction
                )
            }
        }
    }
}

@Composable
private fun ButtonLine(
    modifier: Modifier,
    forceUpdate: Boolean,
    updateInfo: UpdateInfo,
    closeDialogAction: SimpleAction
) {
    val context = LocalContext.current
    val installLauncher = rememberInstallApkLauncher {
        // 回调并不准确
//        if (it) {
//            closeDialogAction()
//        } else {
//            appCtx.toastOnUi(ResStrings.install_failed)
//        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        val color = MaterialTheme.colorScheme.primary
        val file = UpdateUtils.downloadedFile
        val apkSize = updateInfo.apk_size ?: 0
        var progress: Float by rememberStateOf(if (apkSize == 0) 0f else file.length().toFloat() / apkSize)

        // button
        if (!forceUpdate && progress == 0f) {
            TextButton(onClick = { closeDialogAction() }) {
                Text(text = ResStrings.cancel)
            }
        }



        val onError = remember {
            { e: Exception ->
                context.toastOnUi(e.displayMsg(ResStrings.download))
                e.printStackTrace()
                file.delete()
                progress = 0f
            }
        }

        TextButton(onClick = {
            UpdateUtils.downloadUpdate(
                updateInfo = updateInfo,
                file = file,
                onProgressChanged = { progress = it },
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
            val downloading by rememberDerivedStateOf { progress > 0f }
            if (!downloading) {
                Text(text = ResStrings.download, color = color)
            } else if (progress < 1f){
                // 保留两位小数
                val progressText = "${(progress * 10000).roundToInt() / 100f }%"
                Text(text = progressText, color = color)
            } else if (progress == 1f ){
                Text(text = ResStrings.install_now, color = color)
            }
        }
    }
}