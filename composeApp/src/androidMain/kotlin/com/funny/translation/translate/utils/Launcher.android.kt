package com.funny.translation.translate.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.funny.translation.kmp.MultiFileLauncher
import com.funny.translation.translate.activity.CustomPhotoPickerActivity


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

            override fun unregister() {
                // pickLauncher.unregister()
            }

            override fun getContract(): ActivityResultContract<Array<String>, *> {
                return object : ActivityResultContract<Array<String>, List<String>>() {
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
            }
        }
    }

    return remember(onResult) {
        MultiFileLauncher(launcher)
    }
}