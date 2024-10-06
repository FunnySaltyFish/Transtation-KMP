package com.funny.translation.translate.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.BaseApplication
import com.funny.translation.translate.service.CaptureScreenService

class StartCaptureScreenActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "StartCaptureScreenAc"
        private const val EXTRA_KEY_RECT = "rect"
        internal const val ACTION_INIT = "init"
        internal const val ACTION_CAPTURE = "capture"
        internal const val ACTION_STOP = "stop"

        fun start(rect: Rect?) {
            val context = BaseApplication.getCurrentActivity()
            val intent = Intent(context, StartCaptureScreenActivity::class.java)
                .putExtra(EXTRA_KEY_RECT, rect)
            context?.startActivity(intent)
        }
    }

    private lateinit var requestCaptureScreenLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val context = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCaptureScreenLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                startCaptureService(result.resultCode, result.data!!)
            }
            finish()
        }

        val rect: Rect? = intent.extras?.getParcelable(EXTRA_KEY_RECT)

        if (rect == null) {
            // Request necessary permissions
            if (Build.VERSION.SDK_INT >= 34) {
                requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    val granted = it.all { entry -> entry.value }
                    if (granted) {
                        requestScreenCapture()
                    } else {
                        finish()
                    }
                }
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION
                    )
                )
            } else {
                requestScreenCapture()
            }
        } else { // rect != null，截屏
            startService(
                Intent(this, CaptureScreenService::class.java)
                    .setAction(ACTION_CAPTURE)
                    .putExtra("rect", rect)
            )
        }

    }

    private fun startCaptureService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(context, CaptureScreenService::class.java).apply {
            action = ACTION_INIT
            putExtra("code", resultCode)
            putExtra("data", data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun requestScreenCapture() {
        val captureIntent = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
            .createScreenCaptureIntent()
        requestCaptureScreenLauncher.launch(captureIntent)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

}