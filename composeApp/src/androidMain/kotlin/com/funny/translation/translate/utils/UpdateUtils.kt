package com.funny.translation.translate.utils

import android.content.Context
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.R
import com.funny.translation.bean.show
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

object UpdateUtils {
    private var hasCheckedUpdate = false
    private const val TAG = "UpdateUtils"

    suspend fun checkUpdate(context: Context) {
        if (hasCheckedUpdate) return
        kotlin.runCatching {
            val manager = DownloadManager.getInstance(context)
            withContext(Dispatchers.IO) {
                val versionCode = AppConfig.versionCode.toLong()
                Log.d(TAG, "checkUpdate: VersionCode:$versionCode")
                val channel = DataSaverUtils.readData(Consts.KEY_APP_CHANNEL, "stable")
                val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
                Log.i(TAG, "checkUpdate: $updateInfo")
                if (updateInfo.should_update) {
                    val configuration = UpdateConfiguration().apply {
                        httpManager =
                            UpdateDownloadManager(CacheManager.cacheDir.resolve("apk").absolutePath)
                        isForcedUpgrade = updateInfo.force_update == true
                    }

                    manager.setApkName("update_apk.apk")
                        .setApkUrl(updateInfo.apk_url)
                        .setApkMD5(updateInfo.apk_md5)
                        .setSmallIcon(R.drawable.ic_launcher)
                        //非必须参数
                        .setConfiguration(configuration)
                        //设置了此参数，那么会自动判断是否需要更新弹出提示框
                        .setApkVersionCode(updateInfo.version_code!!)
                        .setApkDescription(updateInfo.update_log)
                        .setApkVersionName(updateInfo.version_name)
                        .setApkSize((BigDecimal(updateInfo.apk_size!!).divide(BigDecimal.valueOf(1024L*1024L))).show(1))
                    withContext(Dispatchers.Main) {
                        manager.download()
                    }
                }
            }
            hasCheckedUpdate = true
        }.onFailure {
            it.printStackTrace()
        }
    }
}