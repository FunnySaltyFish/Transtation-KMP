@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.funny.translation.translate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import com.eygraber.uri.toAndroidUri
import com.funny.translation.BaseActivity
import com.funny.translation.Consts
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.rememberNavController
import com.funny.translation.translate.utils.DeepLinkManager
import com.funny.translation.ui.App
import kotlin.properties.Delegates

actual class TransActivity : BaseActivity() {
    actual var navController: NavController by Delegates.notNull()
    private lateinit var activityViewModel: ActivityViewModel

    private var initialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!initialized) {
            activityViewModel = ActivityViewModel()
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