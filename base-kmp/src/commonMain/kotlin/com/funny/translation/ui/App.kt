package com.funny.translation.ui
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.funny.data_saver.core.LocalDataSaver
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.ui.theme.TransTheme
import com.funny.translation.ui.theme.calcDark
import com.funny.translation.ui.toast.Toast
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import moe.tlaster.precompose.PreComposeApp

/**
 * Wraps the content, include
 * - CompositionLocalProvider: [LocalDataSaver], [LocalImageLoader]
 * - [PreComposeApp]
 * - [TransTheme]
 * - [Toast]
 * @param content [@androidx.compose.runtime.Composable] [@kotlin.ExtensionFunctionType] Function1<BoxWithConstraintsScope, Unit>
 */
@Composable
fun App(content: @Composable () -> Unit = {}) {
    CompositionLocalProvider(
        LocalDataSaver provides DataSaverUtils,
        LocalImageLoader provides remember { generateImageLoader() },
    ) {
        PreComposeApp {
            TransTheme(dark = calcDark()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    Toast(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
expect fun Toast(modifier: Modifier = Modifier)

expect fun generateImageLoader(): ImageLoader