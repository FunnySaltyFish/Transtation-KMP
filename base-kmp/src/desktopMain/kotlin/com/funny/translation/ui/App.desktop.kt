package com.funny.translation.ui
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eygraber.uri.Uri
import com.funny.translation.BuildConfig
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastState
import com.funny.translation.ui.toast.ToastUI
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.fetcher.FetchResult
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.defaultImageResultMemoryCache
import com.seiko.imageloader.option.Options
import com.seiko.imageloader.util.LogPriority
import com.seiko.imageloader.util.Logger
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import java.io.File


@Composable
actual fun Toast(modifier: Modifier) {
    ToastUI(toastState, modifier)
}

actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        logger = object : Logger {
            override fun isLoggable(priority: LogPriority) = if (BuildConfig.DEBUG) {
                true
            } else {
                priority >= LogPriority.WARN
            }

            override fun log(
                priority: LogPriority,
                tag: String,
                data: Any?,
                throwable: Throwable?,
                message: String
            ) {
                if (priority <= LogPriority.WARN) {
                    Log.d(tag, message, throwable ?: Exception(message))
                } else {
                    Log.e(tag, message, throwable ?: Exception(message))
                }
            }

        }
        components {
            add(FileUriFetcher.Factory())
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

/*

class Base64Fetcher private constructor(
    private val data: String,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return data.split(',').let {
            val contentType = it.firstOrNull()?.removePrefix("data:")?.removeSuffix(";base64")
            val content = it.last()
            FetchResult.OfSource(
                source = Buffer().apply {
                    write(content.decodeBase64Bytes())
                },
                extra = extraData {
                    mimeType(contentType)
                },
            )
        }
    }

    class Factory : Fetcher.Factory {
        override fun create(data: Any, options: Options): Fetcher? {
            if (data !is String) return null
            if (!isApplicable(data)) return null
            return Base64Fetcher(data)
        }

        private fun isApplicable(data: String): Boolean {
            return data.startsWith("data:")
        }
    }
}

 */
// file://xxx.xxx
private class FileUriFetcher(
    private val uri: String
): Fetcher {
    override suspend fun fetch(): FetchResult {
        val path = uri.removePrefix("file://")
        return FetchResult.OfSource(
            source = File(path).toOkioPath().source(),
        )
    }

    class Factory : Fetcher.Factory {
        override fun create(data: Any, options: Options): Fetcher? {
            if (data !is Uri) return null
            val uri = data.toString()
            if (!isApplicable(uri)) return null
            return FileUriFetcher(uri)
        }

        private fun isApplicable(data: String): Boolean {
            return data.startsWith("file://")
        }
    }
}


private fun Path.source(): BufferedSource {
    // 使用默认的 FileSystem 实例打开文件的 source
    val fileSource = FileSystem.SYSTEM.source(this)
    // 将 source 包装成 BufferedSource 并返回
    return fileSource.buffer()
}