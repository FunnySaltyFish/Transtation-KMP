package com.funny.translation.translate.ui.main

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.toUri
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.ImageTranslationPart
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.engine.selectKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

enum class ImageTransPage {
    Main, ResultList
}

class ImageTransViewModel : ViewModel() {
    var currentPage by mutableStateOf(ImageTransPage.ResultList)

    var imageUri: Uri? by mutableStateOf(null)
    var translateEngine: ImageTranslationEngine by mutableStateOf(ImageTranslationEngines.Baidu)
    private var translateJob: Job? = null
    var translateState: LoadingState<ImageTranslationResult> by mutableStateOf(LoadingState.Loading)

    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_target_lang", Language.CHINESE)

    var imgWidth = 0
    var imgHeight = 0

    var allEngines = arrayListOf(ImageTranslationEngines.Baidu, ImageTranslationEngines.Tencent)

    // 下面是处理结果的相关
    var selectedResultParts = mutableStateListOf<ImageTranslationPart>()

    init {
        translateEngine = DefaultData.bindImageEngines.firstOrNull {
            DataSaverUtils.readData(it.selectKey, false)
        } ?: ImageTranslationEngines.Baidu

        // mock data
        translateState = LoadingState.Success(
            ImageTranslationResult(
                source = "source",
                target = "target",
                content = listOf(
                    ImageTranslationPart("白日依山尽", "The white sun sets behind the mountains"),
                    ImageTranslationPart("黄河入海流", "The Yellow River flows into the sea"),
                    ImageTranslationPart("欲穷千里目", "If you want to see a thousand miles"),
                    ImageTranslationPart("更上一层楼", "Climb to a higher level")
                )
            )
        )
    }

    // 翻译相关
    fun translate() {
        imageUri ?: return
        translateJob?.cancel()
        Log.d(TAG, "translate: start")
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            translateState = LoadingState.Loading
            // 延迟一下再开始翻译，防止语言选的不对临时变更
            delay(1000)
            kotlin.runCatching {
                val bytes = BitmapUtil.getBitmapFromUri(
                    appCtx,
                    4096,
                    4096,
                    2 * 1024 * 1024,
                    imageUri!!.toUri()
                )
                bytes ?: return@launch
                Log.d(TAG, "translate: imageSize: ${bytes.size}")
                with(GlobalTranslationConfig) {
                    this.sourceLanguage = this@ImageTransViewModel.sourceLanguage
                    this.targetLanguage = this@ImageTransViewModel.targetLanguage
                    this.sourceString = ""
                }
                translateEngine.createTask(bytes, sourceLanguage, targetLanguage).apply {
                    this.translate()
                }.result
            }.onSuccess {
                val user = AppConfig.userInfo.value
                if (user.img_remain_points > 0)
                    AppConfig.userInfo.value =
                        user.copy(img_remain_points = user.img_remain_points - translateEngine.getPoint())
                translateState = LoadingState.Success(it)
            }.onFailure {
                it.printStackTrace()
                translateState = LoadingState.Failure(it)
                appCtx.toastOnUi("翻译错误！原因是：${it.message}")
            }
        }
    }

    fun cancel() {
        translateJob?.cancel()
        translateState = LoadingState.Loading
    }

    // 处理结果相关
    fun updateSelectedResultParts(part: ImageTranslationPart, newSelectState: Boolean) {
        if (newSelectState) {
            selectedResultParts.add(part)
        } else {
            selectedResultParts.remove(part)
        }
    }

    fun clearSelectedResultParts() {
        selectedResultParts.clear()
    }

    fun copySelectedResultParts() {
        val text = selectedResultParts.joinToString("\n") { it.target }
        ClipBoardUtil.copy(text)
    }

    fun selectAllResultParts() {
        selectedResultParts.clear()
        selectedResultParts.addAll(
            translateState.getOrNull<ImageTranslationResult>()?.content ?: emptyList()
        )
    }


    fun isTranslating() = translateJob?.isActive == true
    fun updateImageUri(uri: Uri?) {
        imageUri = uri
    }

    fun updateSourceLanguage(language: Language) {
        sourceLanguage = language
    }

    fun updateTargetLanguage(language: Language) {
        targetLanguage = language
    }

    fun updateImgSize(w: Int, h: Int) {
        imgWidth = w; imgHeight = h
    }

    fun updateTranslateEngine(new: ImageTranslationEngine) {
        if (translateEngine != new) {
            DataSaverUtils.saveData(translateEngine.selectKey, false)
            translateEngine = new
            DataSaverUtils.saveData(new.selectKey, true)
        }
    }

    companion object {
        private const val TAG = "ImageTransVM"
    }
}