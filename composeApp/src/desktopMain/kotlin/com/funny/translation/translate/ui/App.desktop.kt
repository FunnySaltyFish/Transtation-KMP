package com.funny.translation.translate.ui
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.toastState
import com.funny.translation.ui.toast.ToastUI
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.defaultImageResultMemoryCache
import okio.Path.Companion.toOkioPath


@Composable
actual fun Toast(modifier: Modifier) {
    ToastUI(toastState, modifier)
}

actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            // cache 100 success image result, without bitmap
            defaultImageResultMemoryCache()
            memoryCacheConfig {
                maxSizeBytes(32 * 1024 * 1024) // 32MB
            }
            diskCacheConfig {
                directory(CacheManager.cacheDir.resolve("image_cache").toOkioPath())
                maxSizeBytes(64L * 1024 * 1024) // 512MB
            }
        }
    }
}