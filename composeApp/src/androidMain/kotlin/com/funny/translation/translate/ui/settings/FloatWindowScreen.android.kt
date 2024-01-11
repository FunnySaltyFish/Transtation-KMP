package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.Composable
import com.funny.translation.helper.LocalContext

@Composable
actual fun FloatWindowScreen() {
    val context = LocalContext.current
    // TODO finish float window
//    CommonPage(title = ResStrings.float_window) {
//        JetSettingSwitch(state = AppConfig.sShowFloatWindow, text = ResStrings.open_float_window) {
//            try {
//                if (it) EasyFloatUtils.showFloatBall(context as Activity)
//                else EasyFloatUtils.hideAllFloatWindow()
//            } catch (e: Exception) {
//                context.toastOnUi("显示悬浮窗失败，请检查是否正确授予权限！")
//                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
//            }
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Row(
//            Modifier
//                .touchToScale()
//                .fillMaxWidth(0.9f)
//                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
//                .padding(16.dp)
//        ) {
//            FixedSizeIcon(Icons.Default.Info, contentDescription = null)
//            Text(text = ResStrings.float_window_tip, modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp))
//        }
//    }

}