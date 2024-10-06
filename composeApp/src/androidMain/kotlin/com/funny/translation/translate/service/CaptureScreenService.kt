package com.funny.translation.translate.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.eygraber.uri.toAndroidUri
import com.eygraber.uri.toUri
import com.funny.translation.R
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.Log
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.BuildConfig
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.TransActivityIntent
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_CAPTURE
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_INIT
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_STOP
import com.funny.translation.translate.bean.FileSize
import com.funny.translation.translate.utils.DeepLinkManager
import com.funny.translation.translate.utils.ScreenUtils
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore

class CaptureScreenService : Service() {
    companion object {
        private const val TAG = "CaptureScreenService"
        private var mResultCode = 0
        private var mResultData: Intent? = null

        val hasMediaProjection get() = mResultData != null
        val TEMP_CAPTURED_IMAGE_FILE = CacheManager.cacheDir.resolve("temp_captured_image.jpg")
        val WHOLE_SCREEN_RECT = Rect(-1, -1, -1, -1)

        fun stop() {
            val intent = Intent(appCtx, CaptureScreenService::class.java)
            appCtx.stopService(intent)
        }
    }

    private var mRect: Rect? = null

    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    // 仅 Android 14以上有效，由于一直开着录制，onImageAvailable 可能在 release 前回调多次
    // 为避免这种情况，加个信号量确保同一时刻只执行一次
    private val semaphore = Semaphore(1)

    private val screenWidth by lazy { ScreenUtils.getScreenWidth() }
    private val screenHeight by lazy { ScreenUtils.getScreenHeight() }
    private val densityDpi get() = ScreenUtils.getScreenDensityDpi()

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: intent: ${intent?.action} extras: ${intent?.extras} ")
        when (intent?.action) {
            ACTION_INIT -> {
                mResultCode = intent.getIntExtra("code", -1)
                mResultData = intent.getParcelableExtra("data")
                // init()
            }

            ACTION_CAPTURE -> {
                mRect = intent.getParcelableExtra("rect")
                init()
                capture()
            }

            ACTION_STOP -> {
                stop()
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        mResultCode = 0
        mResultData = null
        virtualDisplay?.release()
        virtualDisplay = null
        mMediaProjection?.stop()
        // 关闭通知
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                NotificationCompat.Builder(this, FunnyApplication.SCREEN_CAPTURE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(ResStrings.capture_screen)
                    .setContentText(ResStrings.capture_screen_desc)
                    // 加个按钮，用于关闭
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_close,
                            ResStrings.stop_capture_screen,
                            PendingIntent.getService(
                                this,
                                0,
                                Intent(this, CaptureScreenService::class.java).apply {
                                    action = ACTION_STOP
                                },
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
                    .build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(
                1,
                NotificationCompat.Builder(this, FunnyApplication.SCREEN_CAPTURE_CHANNEL_ID).build()
            )
        }
    }

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: CaptureScreenService get() = this@CaptureScreenService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private fun capture() {
        // Android 14 以下每次都创建新的 VirtualDisplay
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            setUpVirtualDisplay(releaseOnEnd = true)
        } else {
            // Android 14 及以上，只初始化一次，然后每次都从那里获取最新图片
            if (virtualDisplay == null) {
                setUpVirtualDisplay(releaseOnEnd = false)
            } else {
                imageReader?.close()
                imageReader = createImageReader(releaseOnEnd = true)
                virtualDisplay!!.surface = imageReader!!.surface
            }
        }
    }

    private fun init() {
        if (mMediaProjectionManager == null) {
            mMediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mMediaProjection =
                mMediaProjectionManager!!.getMediaProjection(mResultCode, mResultData!!).apply {
                    registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() {
                            Log.d(TAG, "MediaProjection.Callback stopped")
                            virtualDisplay?.release()
                        }
                    }, null)
                }
        }
    }

    @SuppressLint("WrongConstant")
    private fun setUpVirtualDisplay(releaseOnEnd: Boolean = true) {
        runOnUI {
            try {
                val width = screenWidth
                val height = screenHeight
                imageReader = createImageReader(releaseOnEnd)
                virtualDisplay = mMediaProjection!!.createVirtualDisplay(
                    "CaptureScreen",
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader!!.surface,
                    object : VirtualDisplay.Callback() {
                        override fun onPaused() {
                            super.onPaused()
                            Log.d(TAG, "VirtualDisplay.Callback paused")
                        }

                        override fun onResumed() {
                            super.onResumed()
                            Log.d(TAG, "VirtualDisplay.Callback resumed")
                        }
                    },
                    null
                )
            } catch (throwable: Throwable) {
                showError(ResStrings.failed_to_take_screenshot, throwable)
            }
        }
    }

    private fun createImageReader(
        releaseOnEnd: Boolean
    ) = ImageReader.newInstance(
        screenWidth,
        screenHeight,
        PixelFormat.RGBA_8888,
        1
    ).apply {
        setOnImageAvailableListener({ reader ->
            onImageAvailable(virtualDisplay, reader, releaseOnEnd)
        }, null)
    }

    private fun onImageAvailable(virtualDisplay: VirtualDisplay?, reader: ImageReader, releaseOnEnd: Boolean) {
        try {
            semaphore.acquire()
            val image = reader.acquireLatestImage()
            image?.use {
                val width = image.width
                val height = image.height
                val planes: Array<Image.Plane> = image.planes
                if (planes.isEmpty()) {
                    appCtx.toastOnUi(ResStrings.screenshot_failed)
                    semaphore.release()
                    return
                }
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                var bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                Log.d(TAG, "onImageAvailable: $mRect")
                if (mRect != null && mRect != WHOLE_SCREEN_RECT) {
                    // 做裁剪
                    val rect = Rect(
                        mRect!!.left,
                        mRect!!.top,
                        mRect!!.right,
                        mRect!!.bottom
                    )
                    bitmap = Bitmap.createBitmap(
                        bitmap,
                        rect.left,
                        rect.top,
                        rect.width(),
                        rect.height()
                    )
                }
                //保存图片到本地
                val bytes = BitmapUtil.compressImage(bitmap, FileSize.fromMegabytes(1).size)
                BitmapUtil.saveBitmap(bytes, TEMP_CAPTURED_IMAGE_FILE.absolutePath)
                appCtx.toastOnUi(ResStrings.save_screenshot_success)
                // 如果是全屏翻译，先裁剪一下
                startTranslate(doClip = (mRect == WHOLE_SCREEN_RECT))
            }
        } catch (throwable: Throwable) {
            showError(ResStrings.failed_to_save_screenshot, throwable)
        } finally {
            execSafely {
                if (releaseOnEnd) {
                    virtualDisplay?.release()
                } else {
                    // Android 14 及以上版本，virtualDisplay 不会自动释放
                    // 设置 surface 为 null 以进入到 pause 状态，减少无意义的消耗
                    virtualDisplay?.surface = null
                }
                reader.close()
                semaphore.release()
            }
        }
    }

    private fun startTranslate(doClip: Boolean){
//        val fileUri = TEMP_CAPTURED_IMAGE_FILE.toUri()
        val context = appCtx
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            TEMP_CAPTURED_IMAGE_FILE
        )
        Log.d(TAG, "startTranslate fileUri: $fileUri, toURi: ${fileUri.toUri()}")
        TransActivityIntent.TranslateImage(DeepLinkManager.buildImageTransUri(imageUri = fileUri.toUri(), doClip = doClip).toAndroidUri()).asIntent().let {
            // 已经存在，就带到前台
            it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    private fun showError(message: String, throwable: Throwable) {
        toastOnUi(message + (if (BuildConfig.DEBUG) "\n\n$throwable" else ""))
        throwable.printStackTrace()
    }

    private inline fun execSafely(block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }
}