@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.funny.translation.translate

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.lifecycleScope
import com.eygraber.uri.toAndroidUri
import com.funny.translation.AppConfig
import com.funny.translation.BaseActivity
import com.funny.translation.Consts
import com.funny.translation.helper.Log
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.translate.network.NetworkReceiver
import com.funny.translation.translate.utils.DeepLinkManager
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.UpdateUtils
import com.funny.translation.ui.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "TransActivity"

actual class TransActivity : BaseActivity() {
    actual var navController: NavController? = null
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var netWorkReceiver: NetworkReceiver


    private var initialized = false


    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerNetworkReceiver()
        getIntentData(intent)

        if (!initialized) {
            activityViewModel = ActivityViewModel()

            // 做一些耗时的后台任务
            lifecycleScope.launch(Dispatchers.IO) {
                // MobileAds.initialize(context) {}
                activityViewModel.refreshUserInfo()
                UpdateUtils.checkUpdate(this@TransActivity)
            }

            // 显示悬浮窗
            EasyFloatUtils.initScreenSize()
            val showFloatWindow = AppConfig.sShowFloatWindow.value
            if (showFloatWindow) {
                EasyFloatUtils.showFloatBall(this)
            }

            initialized = true
        }

        setContent {
            App {
                // 此处通过这种方式传递 Activity 级别的 ViewModel，以确保获取到的都是同一个实例
                CompositionLocalProvider(LocalActivityVM provides activityViewModel) {
                    AppNavigation(
                        navController = rememberNavController().also {
                            this@TransActivity.navController = it
                        },
                        exitAppAction = { this@TransActivity.finish() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (initialized) {
            // 由于 PreCompose 的 ViewModel 不支持 Activity 生命周期的分发，手动分发一个 Active 事件
            // 供实现“打开应用自动打开软键盘”的功能
            activityViewModel.onStateChanged(moe.tlaster.precompose.lifecycle.Lifecycle.State.Active)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged: ")
        EasyFloatUtils.resetFloatBallPlace()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        EasyFloatUtils.dismissAll()
        unregisterReceiver(netWorkReceiver)
        super.onDestroy()
    }

    /**
     * 注册用于网络监听的广播，以判断网络状况
     */
    private fun registerNetworkReceiver() {
        netWorkReceiver = NetworkReceiver()
        IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }.let {
            registerReceiver(netWorkReceiver, it)
        }

        // 初始化一下最开始的网络状态
        NetworkReceiver.setNetworkState(this)
    }

    /**
     * 处理从各种地方传过来的 intent
     * @param intent Intent?
     */
    private fun getIntentData(intent: Intent?) {
        val action: String? = intent?.action
        Log.d(TAG, "getIntentData: action:$action, data:${intent?.data}")
        val transActivityIntent = TransActivityIntent.fromIntent(intent)
        // 处理从应用其他地方传过来的 Url
        if (transActivityIntent != null) {
            Log.d(TAG, "getIntentData: parsed transActivityIntent: $transActivityIntent")
            when (transActivityIntent) {
                is TransActivityIntent.TranslateText -> {
                    if (transActivityIntent.byFloatWindow) {
                        EasyFloatUtils.showFloatBall(this)
                        EasyFloatUtils.showTransWindow()
                        EasyFloatUtils.startTranslate(
                            transActivityIntent.text,
                            transActivityIntent.sourceLanguage
                                ?: AppConfig.sDefaultSourceLanguage.value,
                            transActivityIntent.targetLanguage
                                ?: AppConfig.sDefaultTargetLanguage.value
                        )
                    } else {
                        navigateToTextTrans(
                            transActivityIntent.text,
                            transActivityIntent.sourceLanguage,
                            transActivityIntent.targetLanguage
                        )
                    }
                }

                is TransActivityIntent.TranslateImage -> {
                    navController?.navigate(transActivityIntent.deepLinkUri.toString())
                }

                is TransActivityIntent.OpenFloatWindow -> {
                    EasyFloatUtils.showFloatBall(this)
                }
            }
        } else {
            Log.d(TAG, "getIntentData: 走到了 else, intent: $intent")
            // 这里是处理输入法选中后的菜单
            if (Intent.ACTION_PROCESS_TEXT == action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
                if (text != "") {
                    Log.d(TAG, "获取到输入法菜单传来的文本: $text")
                    navigateToTextTrans(text)
                }
            }
        }
    }

    private fun navigateToTextTrans(
        sourceText: String,
        sourceLanguage: Language? = null,
        targetLanguage: Language? = null
    ) {
        lifecycleScope.launch {
            while (navController == null) { delay(50) }
            navController!!.navigateToTextTrans(
                sourceText,
                sourceLanguage ?: AppConfig.sDefaultSourceLanguage.value,
                targetLanguage ?: AppConfig.sDefaultTargetLanguage.value
            )
        }
    }
}

// 统一处理 intent
sealed class TransActivityIntent() {
    fun asIntent(): Intent {
        return when (this) {
            is TranslateText -> Intent(Intent.ACTION_VIEW).apply {
                data = DeepLinkManager.buildTextTransUri(
                    text,
                    sourceLanguage,
                    targetLanguage,
                    byFloatWindow).toAndroidUri()
            }

            is TranslateImage -> Intent(Intent.ACTION_VIEW).apply {
                data = deepLinkUri
            }

            is OpenFloatWindow -> Intent().apply {
                action = Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE
                putExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }.apply {
            setClass(appCtx, TransActivity::class.java)
            // 带到前台
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    data class TranslateText(
        val text: String,
        val sourceLanguage: Language?,
        val targetLanguage: Language?,
        val byFloatWindow: Boolean = false
    ) : TransActivityIntent()

    data class TranslateImage(val deepLinkUri: Uri) : TransActivityIntent()

    object OpenFloatWindow: TransActivityIntent()

    companion object {
        fun fromIntent(intent: Intent?): TransActivityIntent? {
            intent ?: return null

            if (intent.action == Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE) {
                return if (intent.getBooleanExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, false)) OpenFloatWindow else null
            }

            val data = intent.data
            data ?: return null

            if (data.scheme == "funny" && data.host == "translation") {
                return when (data.path) {
                    DeepLinkManager.TEXT_TRANS_PATH -> kotlin.run {
                        val text = data.getQueryParameter("text") ?: ""
                        val sourceId = data.getQueryParameter("sourceId")
                        val targetId = data.getQueryParameter("targetId")
                        val byFloatWindow =
                            data.getQueryParameter("byFloatWindow")?.toBoolean() ?: false
                        TranslateText(
                            text,
                            Language.fromId(sourceId),
                            Language.fromId(targetId),
                            byFloatWindow
                        )
                    }
                    DeepLinkManager.IMAGE_TRANS_PATH ->  {
                        TranslateImage(data)
                    }
                    else -> null
                }
            }
            return null
        }
    }
}