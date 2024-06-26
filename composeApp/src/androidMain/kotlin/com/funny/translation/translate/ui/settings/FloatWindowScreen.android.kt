package com.funny.translation.translate.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.JetSettingSwitch
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.toastOnUi
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.touchToScale

@Composable
actual fun FloatWindowScreen() {
    val context = LocalContext.current
    CommonPage(title = ResStrings.float_window) {
        JetSettingSwitch(state = AppConfig.sShowFloatWindow, text = ResStrings.open_float_window) {
            try {
                if (it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideAllFloatWindow()
            } catch (e: Exception) {
                context.toastOnUi(ResStrings.failed_to_show_float_window)
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier
                .touchToScale()
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            FixedSizeIcon(Icons.Default.Info, contentDescription = null)
            Text(text = ResStrings.float_window_tip, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp))
        }
    }
}