package com.funny.translation.translate.ui.thanks

import android.app.Activity.RESULT_OK
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cn.qhplus.emo.photo.activity.PhotoClipperActivity
import cn.qhplus.emo.photo.activity.PhotoPickResult
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoClipperResult
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.coil.CoilPhotoProvider
import coil.compose.AsyncImage
import com.eygraber.uri.toUri
import com.funny.translation.AppConfig
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import kotlinx.coroutines.launch

@Composable
actual fun UserAvatarTile() {
    val avatarPickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    var photoName by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val activityVM = LocalActivityVM.current

    val clipperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            it.data?.getPhotoClipperResult()?.let { img ->
                if (photoName == "") return@rememberLauncherForActivityResult
                scope.launch {
                    val avatarUrl = UserUtils.uploadUserAvatar(context, img.uri.toUri(), photoName, img.width, img.height, activityVM.uid)
                    if (avatarUrl != ""){
                        activityVM.userInfo = activityVM.userInfo.copy(avatar_url = avatarUrl)
                        context.toastOnUi(ResStrings.upload_avatar_success)
                        avatarPickResult.value = null
                    } else {
                        context.toastOnUi(ResStrings.upload_avatar_failed)
                    }
                }
            }
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.getPhotoPickResult()?.let { result ->
                    avatarPickResult.value = result
                    val img = result.list[0]
                    photoName = img.name
                    clipperLauncher.launch(
                        PhotoClipperActivity.intentOf(
                            context,
                            CoilPhotoProvider(img.uri, ratio = img.ratio())
                        )
                    )
                }
            }
        }

    Tile(
        text = ResStrings.avatar,
        onClick = {
            pickLauncher.launch(
                PhotoPickerActivity.intentOf(
                    context,
                    CoilMediaPhotoProviderFactory::class.java,
                    CustomPhotoPickerActivity::class.java,
                    pickedItems = arrayListOf<Uri>().apply {
                        avatarPickResult.value?.list?.mapTo(
                            this
                        ) { it.uri }
                    },
                    pickLimitCount = 1,
                )
            )
        }
    ) {
        AsyncImage(
            model = AppConfig.userInfo.value.avatar_url,
            contentDescription = ResStrings.avatar,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            placeholder = painterDrawableRes("ic_loading")
        )
    }
}