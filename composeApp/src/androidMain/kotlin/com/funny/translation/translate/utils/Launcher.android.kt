package com.funny.translation.translate.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.ActivityOptionsCompat
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.toAndroidUri
import com.funny.translation.kmp.Launcher
import com.funny.translation.kmp.MultiFileLauncher
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import java.io.File


@Composable
actual fun rememberSelectImageLauncher(
    maxNum: Int,
    pickedItems: List<String>,
    onResult: (List<String>) -> Unit
): MultiFileLauncher<Array<String>> {
    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.getPhotoPickResult()?.let { result ->
                    onResult(result.list.map { it.uri.toString() })
                }
            }
        }

    val context = LocalContext.current
    val currentMaxNum by rememberUpdatedState(maxNum)
    val launcher: ActivityResultLauncher<Array<String>> = remember(pickLauncher) {
        object : ActivityResultLauncher<Array<String>>() {
            override fun launch(input: Array<String>) {
                launch(input, null)
            }

            override fun launch(input: Array<String>, options: ActivityOptionsCompat?) {
                contract.createIntent(context, input).let { intent ->
                    pickLauncher.launch(intent, options)
                }
            }

            override val contract: ActivityResultContract<Array<String>, *>
                get() = object : ActivityResultContract<Array<String>, List<String>>() {
                    override fun createIntent(context: android.content.Context, input: Array<String>): Intent {
                        return PhotoPickerActivity.intentOf(
                            context,
                            CoilMediaPhotoProviderFactory::class.java,
                            CustomPhotoPickerActivity::class.java,
                            pickedItems = arrayListOf<Uri>().apply {
                                pickedItems.mapTo(this, Uri::parse)
                            },
                            pickLimitCount = currentMaxNum,
                        )
                    }

                    override fun parseResult(resultCode: Int, intent: Intent?): List<String> {
                        return intent?.getPhotoPickResult()?.list?.map { it.uri.toString() } ?: emptyList()
                    }
                }

            override fun unregister() {
                // pickLauncher.unregister()
            }
        }
    }

    return remember(onResult) {
        MultiFileLauncher(launcher)
    }
}

actual class InstallApkLauncher(
    private val activityResultLauncher: ActivityResultLauncher<Intent>,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) : Launcher<File, Boolean>() {
    override fun launch(input: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = appCtx.packageManager.canRequestPackageInstalls()
            if (!hasPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.REQUEST_INSTALL_PACKAGES)
                return
            }
        }
        val apkUri = input.toAndroidUri()
        activityResultLauncher.launch(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }
}

@Composable
actual fun rememberInstallApkLauncher(onResult: (Boolean) -> Unit): InstallApkLauncher {
    val activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onResult(false)
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onResult(false)
    }
    return remember(onResult) {
        InstallApkLauncher(activityResultLauncher, requestPermissionLauncher)
    }
}