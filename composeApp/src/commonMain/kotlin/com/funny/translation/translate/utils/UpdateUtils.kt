package com.funny.translation.translate.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.bean.UpdateInfo
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import java.io.File

object UpdateUtils {
    private var hasCheckedUpdate = false
    var updateInfo: UpdateInfo? by mutableStateOf(null)

    private const val TAG = "UpdateUtils"

    suspend fun checkUpdate() {
        if (!allowCheckUpdate) return
        if (hasCheckedUpdate) return
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                val versionCode = AppConfig.versionCode.toLong()
                Log.d(TAG, "checkUpdate: VersionCode:$versionCode")
                val channel = DataSaverUtils.readData(Consts.KEY_APP_CHANNEL, "stable")
                updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
                Log.i(TAG, "checkUpdate: $updateInfo")
                hasCheckedUpdate = true
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun downloadUpdate(
        updateInfo: UpdateInfo,
        file: File,
        onProgressChanged: (Float) -> Unit,
        onDownloadFinished: () -> Unit,
        onError: (Exception) -> Unit
    ): Call? {
        try {
            val apkUrl = updateInfo.apk_url ?: return null
            return OkHttpUtils.downloadWithResume(
                url = apkUrl,
                file = file,
                expectedLength = updateInfo.apk_size?.toLong() ?: 0L,
                progressCallback = { downloadedBytes: Long, totalBytes: Long ->
                    val progress = downloadedBytes.toFloat() / totalBytes
                    Log.d("UpdateUtils", "Progress: $progress")
                    if (progress < 1f) {
                        onProgressChanged(progress)
                    } else {
                        onDownloadFinished()
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
        return null
    }
}

expect val allowCheckUpdate: Boolean
expect fun getInstallApkFile(updateInfo: UpdateInfo): File?

