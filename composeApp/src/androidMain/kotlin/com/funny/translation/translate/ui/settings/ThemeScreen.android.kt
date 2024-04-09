package com.funny.translation.translate.ui.settings

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import com.eygraber.uri.toUri
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.Context
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.getKeyColors
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.ui.widget.ArrowTile
import com.funny.translation.translate.ui.widget.AsyncImage
import com.funny.translation.translate.ui.widget.RadioTile
import com.funny.translation.ui.theme.ThemeConfig
import com.funny.translation.ui.theme.ThemeType

@Composable
actual fun SelectDynamicTheme(modifier: Modifier) {
    Column(modifier = modifier) {
        val context = LocalContext.current
        val themeType by ThemeConfig.sThemeType
        var selectImageUri: Uri? by rememberDataSaverState<Uri?>(
            key = "key_dynamic_theme_selected_img_uri",
            initialValue = null
        )
        val pickLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    it.data?.getPhotoPickResult()?.let { result ->
                        val img = result.list[0]
                        selectImageUri = img.uri
                        changeThemeFromImageUri(context, img.uri)
                    }
                }
            }

        RadioTile(
            text = ResStrings.wallpaper_color_extraction,
            selected = themeType == ThemeType.DynamicNative
        ) {
            ThemeConfig.updateThemeType(ThemeType.DynamicNative)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ArrowTile(text = ResStrings.select_from_image) {
            if (DefaultVipInterceptor.invoke()) {
                pickLauncher.launch(
                    PhotoPickerActivity.intentOf(
                        context,
                        CoilMediaPhotoProviderFactory::class.java,
                        CustomPhotoPickerActivity::class.java,
                        pickedItems = arrayListOf(),
                        pickLimitCount = 1,
                    )
                )
            }
        }

        if (selectImageUri != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AsyncImage(model = selectImageUri.toString(),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (themeType !is ThemeType.DynamicFromImage)
                            changeThemeFromImageUri(context, selectImageUri!!)
                    }
            )
        }
    }
}

private fun changeThemeFromImageUri(context: Context, uri: Uri) {
    val bytes = BitmapUtil.getBitmapFromUri(context, 400, 600, 1024*1024, uri.toUri())
    bytes ?: return
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val color = bitmap.getKeyColors(1).firstOrNull() ?: return
    ThemeConfig.updateThemeType(ThemeType.DynamicFromImage(color))
}