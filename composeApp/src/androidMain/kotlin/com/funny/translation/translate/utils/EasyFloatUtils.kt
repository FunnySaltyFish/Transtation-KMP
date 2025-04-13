package com.funny.translation.translate.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.funny.translation.AppConfig
import com.funny.translation.R
import com.funny.translation.helper.Log
import com.funny.translation.helper.VibratorUtils
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.Language
import com.funny.translation.translate.TransActivityIntent
import com.funny.translation.translate.activity.StartCaptureScreenActivity
import com.funny.translation.translate.service.CaptureScreenService
import com.funny.translation.translate.ui.floatwindow.FloatingTranslationWindow
import com.funny.translation.translate.ui.main.MainViewModel
import com.funny.translation.ui.App
import com.github.only52607.compose.window.ComposeFloatingWindow
import com.github.only52607.compose.window.dragFloatingWindow
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.permission.PermissionUtils
import com.lzf.easyfloat.utils.DragUtils
import com.lzf.easyfloat.widget.BaseSwitchView
import com.tomlonghurst.roundimageview.RoundImageView
import kotlinx.coroutines.launch
import kotlin.math.min


object EasyFloatUtils {
    internal const val TAG_FLOAT_BALL = "ball"

    private const val TAG = "EasyFloat"
    private var vibrating = false
    var initFloatBall = false

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private val floatTransWindow by lazy {
        ComposeFloatingWindow(
            appCtx,
            ComposeFloatingWindow.defaultLayoutParams(appCtx).apply {
                gravity = Gravity.TOP or Gravity.START
                width = (min(AppConfig.SCREEN_WIDTH, AppConfig.SCREEN_HEIGHT) * 0.9).toInt()
                x = (AppConfig.SCREEN_WIDTH - width) / 2
                y = 100
                // 可超过系统状态栏
                flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS
            }
        ).apply {
            val window = this
            setContent {
                App(boxModifier = Modifier) {
                    val interactionSource = remember { MutableInteractionSource() }
                    var focusIndication: FocusInteraction.Focus? by rememberStateOf(null)
                    val isFocused by rememberDerivedStateOf { focusIndication != null }
                    val focusManager = LocalFocusManager.current

                    LaunchedEffect(interactionSource){
                        interactionSource.interactions.collect {
                            when (it) {
                                is FocusInteraction.Focus -> {
                                    focusIndication = it
                                    Log.d(TAG, "FocusInteraction.Focus")
                                }
                            }
                        }
                    }
                    val scope = rememberCoroutineScope()

                    DisposableEffect (window.decorView) {
                        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
                            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
//                            Log.d("ComposeFloatingWindow", "Insets received by decorView: ime=$imeVisible, bottom=${insets.getInsets(
//                                WindowInsetsCompat.Type.ime()).bottom}")
                            // 防止遮挡输入框

                            if (!imeVisible && isFocused && window.windowParams.flags and FLAG_NOT_FOCUSABLE == 0) {
                                Log.d(TAG, "IME closed, restoring FLAG_NOT_FOCUSABLE")
                                window.windowParams.flags =
                                    FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS
                                window.update()
                                focusIndication?.let {
                                    Log.d(TAG, "Unfocusing window: $it")
                                    scope.launch {
                                        focusManager.clearFocus()
                                        focusIndication = null
                                    }
                                }
                            }
                            insets
                        }

                        onDispose {
                            // 清理工作
                            ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
                        }
                    }

                    FloatingTranslationWindow(
                        MainViewModel().also { mainViewModel = it },
                        onClose = { this.hide() },
                        onOpenApp = { vm ->
                            TransActivityIntent.TranslateText(
                                text = vm.actualTransText,
                                sourceLanguage = vm.sourceLanguage,
                                targetLanguage = vm.targetLanguage,
                                byFloatWindow = false
                            ).asIntent().let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                appCtx.startActivity(it)
                            }
                        },
                        modifier = Modifier.dragFloatingWindow().imePadding(),
                        interactionSource = interactionSource,
                        onTapInput = {
                            Log.d(TAG, "onTapd")
                            // 让浮窗获取焦点，并打开软键盘
                            window.windowParams.flags = FLAG_NOT_TOUCH_MODAL or FLAG_WATCH_OUTSIDE_TOUCH
                            window.update()
                        }
                    )
                }
            }
        }
    }

    // 主ViewModel引用
    private var mainViewModel: MainViewModel? = null

    fun initScreenSize() {
        AppConfig.SCREEN_WIDTH = ScreenUtils.getScreenWidth()
        AppConfig.SCREEN_HEIGHT = ScreenUtils.getScreenHeight()
    }

    fun showTransWindow(){
        floatTransWindow.windowParams.width = (min(AppConfig.SCREEN_WIDTH, AppConfig.SCREEN_HEIGHT) * 0.9).toInt()
        floatTransWindow.show()
    }

    fun resetFloatBallPlace(){
        initScreenSize()
        EasyFloat.updateFloat(TAG_FLOAT_BALL, AppConfig.SCREEN_WIDTH - 200, AppConfig.SCREEN_HEIGHT * 2 / 3)
    }

    fun startTranslate(sourceString: String, sourceLanguage: Language, targetLanguage: Language){
        showTransWindow()
        mainViewModel?.let {
            it.updateTranslateText(sourceString)
            it.updateSourceLanguage(sourceLanguage)
            it.updateTargetLanguage(targetLanguage)
            it.translate()
        }
    }

    @SuppressLint("MissingPermission")
    fun setVibrator(inRange: Boolean) {
        vibrating = inRange
        if (inRange) VibratorUtils.vibrate(100)
        else VibratorUtils.cancel()
    }

    private fun _showFloatBall(){
        if(initFloatBall){
            EasyFloat.show(TAG_FLOAT_BALL)
        }else {
            var plusView: View? = null
            EasyFloat.with(FunnyApplication.ctx)
                .setTag(TAG_FLOAT_BALL)
                .setLayout(R.layout.layout_float_ball) { view ->
                    view.findViewById<RoundImageView>(R.id.float_ball_image).apply {
                        setOnClickListener {
                            showTransWindow()
                        }
                        setOnLongClickListener {
                            VibratorUtils.vibrate()
                            if (!CaptureScreenService.hasMediaProjection) {
                                // 如果没有权限，则跳转到申请权限的界面
                                StartCaptureScreenActivity.start(null)
                            } else {
                                StartCaptureScreenActivity.start(CaptureScreenService.WHOLE_SCREEN_RECT)
                            }
                            true
                        }
                    }
                    plusView = view.findViewById<ImageView>(R.id.float_ball_plus).apply {
                        alpha = 0.5f
                    }
                }
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setImmersionStatusBar(true)
                .setGravity(Gravity.END or Gravity.BOTTOM, -20, -200)
                .registerCallback {
                    drag { view, motionEvent ->
                        FloatScreenCaptureUtils.registerDrag(
                            plusView = plusView, motionEvent = motionEvent
                        )
                        // 截屏的时候就不判定删除了
                        if (FloatScreenCaptureUtils.whetherInScreenCaptureMode) return@drag
                        DragUtils.registerDragClose(motionEvent, object : OnTouchRangeListener {
                            override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
                                setVibrator(inRange)
                                view.findViewById<TextView>(com.lzf.easyfloat.R.id.tv_delete).text =
                                    if (inRange) ResStrings.release_to_delete else ResStrings.remove_float_window

                                view.findViewById<ImageView>(com.lzf.easyfloat.R.id.iv_delete)
                                    .setImageResource(
                                        if (inRange) com.lzf.easyfloat.R.drawable.icon_delete_selected
                                        else com.lzf.easyfloat.R.drawable.icon_delete_normal
                                    )
                            }

                            override fun touchUpInRange() {
                                EasyFloat.dismiss(TAG_FLOAT_BALL)
                                AppConfig.sShowFloatWindow.value = false
                                initFloatBall = false
                                CaptureScreenService.stop()
                            }
                        }, showPattern = ShowPattern.ALL_TIME)
                    }
                    dragEnd {
                        FloatScreenCaptureUtils.registerDragEnd(plusView)
                    }
                }
                .show()
            initFloatBall = true
        }
    }

    fun showFloatBall(activity : Activity){
        if(!PermissionUtils.checkPermission(FunnyApplication.ctx)) {
            AlertDialog.Builder(activity)
                .setMessage(ResStrings.tip_grant_float_window_permission)
                .setPositiveButton(ResStrings.go_to_grant) { _, _ ->
                    PermissionUtils.requestPermission(activity, object : OnPermissionResult {
                        override fun permissionResult(isOpen: Boolean) {
                            showFloatBall(activity)
                        }
                    })
                }
                .setNegativeButton(ResStrings.cancel) { _, _ -> }
                .show()
        }else{
            _showFloatBall()
        }
    }

    fun hideAllFloatWindow(){
        floatTransWindow.hide()
        EasyFloat.hide(TAG_FLOAT_BALL)
    }

    fun dismissAll(){
        floatTransWindow.hide()
        EasyFloat.dismiss(TAG_FLOAT_BALL)
        FloatScreenCaptureUtils.dismiss()
        mainViewModel = null
        CaptureScreenService.stop()
    }

    fun isShowingFloatBall() = EasyFloat.isShow(TAG_FLOAT_BALL)
}