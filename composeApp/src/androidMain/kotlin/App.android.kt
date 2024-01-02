import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funny.translation.helper.CacheManager
import com.funny.translation.kmp.appCtx
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.defaultImageResultMemoryCache
import com.seiko.imageloader.option.androidContext
import okio.Path.Companion.toOkioPath

// For android, we use the default toast
@Composable
actual fun Toast(modifier: Modifier) {}

actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        options {
            androidContext(appCtx)
        }
        components {
            setupDefaultComponents()
        }
        interceptor {
            // cache 100 success image result, without bitmap
            defaultImageResultMemoryCache()
            memoryCacheConfig {
                // Set the max size to 25% of the app's available memory.
                maxSizePercent(appCtx, 0.25)
            }
            diskCacheConfig {
                directory(CacheManager.cacheDir.resolve("image_cache").toOkioPath())
                maxSizeBytes(64L * 1024 * 1024) // 64
            }
        }
    }
}