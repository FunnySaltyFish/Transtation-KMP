package com.funny.translation.translate.ui.image

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cn.qhplus.emo.photo.activity.PhotoPickResult
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.ui.GestureContent
import cn.qhplus.emo.photo.ui.GestureContentState
import com.funny.translation.AppConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.widget.CustomCoilProvider
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
actual fun ImageTransMain(
    imageUri: String?,
    sourceId: Int?,
    targetId: Int?,
    doClipFirst: Boolean,
    updateCurrentPage: (ImageTransPage) -> Unit
) {
    val vm = viewModel<ImageTransViewModel>()
    val context = LocalKMPContext.current
    val imagePickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    var photoName by rememberSaveable { mutableStateOf("") }
    val currentEnabledLanguages by enabledLanguages.collectAsState()

    DisposableEffect(key1 = Unit) {
        if (!AppConfig.userInfo.value.isValid()) {
            context.toastOnUi(ResStrings.login_to_use_image_translation, Toast.LENGTH_LONG)
        }
        onDispose {
            vm.cancelTranslateJob()
        }
    }

    val clipperLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { intent ->
                    val uri = UCrop.getOutput(intent)
                    vm.updateImageUri(uri.toString())
                    val width = UCrop.getOutputImageWidth(intent)
                    val height = UCrop.getOutputImageHeight(intent)
                    vm.updateImgSize(width, height)
                    vm.translate()
                }
            }
        }

    val doClip = remember {
        { uri: android.net.Uri ->
            clipperLauncher.launch(
                UCrop.of(uri, DESTINATION_IMAGE_URI.toUri())
                    .withOptions(UCrop.Options().apply {
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    })
                    .getIntent(context)
            )
            imagePickResult.value = null
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.getPhotoPickResult()?.let { result ->
                    imagePickResult.value = result
                    val img = result.list[0]
                    photoName = img.name
                    doClip(img.uri)
                }
            }
        }

    // 如果进入页面时参数携带了图片uri
    LaunchedEffect(key1 = imageUri) {
        if (imageUri == null) return@LaunchedEffect
        // 如果已经翻译成功，那么不再进行翻译（这个是防止从结果列表返回到这个页面时再次翻译）
        if (vm.translateState.isSuccess) return@LaunchedEffect

        // 先对 uri 解码
        val androidImageUri = imageUri
        // 如果不需要裁剪，那么直接翻译
        if (!doClipFirst) {
            vm.imageUri = androidImageUri
            val imageSize = BitmapUtil.getImageSizeFromUri(appCtx, androidImageUri)
            if (imageSize == (-1 to -1)) return@LaunchedEffect
            vm.updateImgSize(imageSize.first, imageSize.second)
            vm.sourceLanguage = sourceId?.let { findLanguageById(it) } ?: Language.AUTO
            vm.targetLanguage = targetId?.let { findLanguageById(it) } ?: Language.CHINESE
            vm.translate()
        } else {
            // 反之调到裁剪页面
            doClip(androidImageUri.toUri())
        }
    }

    if (vm.imageUri != null) {
        ImageTranslationPart(
            vm = vm,
            updateCurrentPage = updateCurrentPage
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraCapture(
                modifier = Modifier.fillMaxSize(),
                onSavedImageFile = { uri ->
                    photoName = "photo_${System.currentTimeMillis()}.jpg"
                    doClip(uri)
                },
                onError = {
                    it.printStackTrace()
                    context.toastOnUi(ResStrings.failed_to_take_photo)
                },
                startChooseImage = {
                    pickLauncher.launch(
                        PhotoPickerActivity.intentOf(
                            context,
                            CoilMediaPhotoProviderFactory::class.java,
                            CustomPhotoPickerActivity::class.java,
                            pickedItems = arrayListOf<android.net.Uri>().apply {
                                imagePickResult.value?.list?.mapTo(
                                    this
                                ) { it.uri }
                            },
                            pickLimitCount = 1,
                        )
                    )
                }
            )
            LanguageSelectRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(0.dp, 24.dp),
                exchangeButtonTint = Color.White,
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
                enabledLanguages = currentEnabledLanguages
            )
        }
    }
}

@Composable
actual fun ResultPart(modifier: Modifier, vm: ImageTransViewModel) {
    val showResult by vm.showResultState
    // 图片为了铺满屏幕进行的缩放
    var imageInitialScale by remember { mutableFloatStateOf(1f) }
    var scaleByWidth by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val lazyListState = rememberLazyListState()

    val photoProvider = remember(vm.imageUri) {
        vm.imageUri?.let {
            val uri = it.toUri()
            CustomCoilProvider(uri, uri, vm.imgWidth.toFloat() / vm.imgHeight, lazyListState)
        }
    }

    photoProvider?.let {
        val gestureState = remember(it) {
            GestureContentState(
                ratio = it.ratio,
                isLongContent = it.isLongImage(),
            )
        }
        LaunchedEffect(key1 = gestureState.layoutInfo) {
            gestureState.layoutInfo?.let { layoutInfo ->
                val sw = layoutInfo.px.contentWidth / vm.imgWidth
                val sh = layoutInfo.px.contentHeight / vm.imgHeight
                if (gestureState.isLongContent) {
                    scaleByWidth = true
                    imageInitialScale = sw
                } else {
                    scaleByWidth = sw < sh
                    imageInitialScale = min(sw, sh)
                    Log.d(
                        TAG,
                        "ResultPart: size: ${layoutInfo.contentWidth}, ${layoutInfo.contentHeight}, img: ${vm.imgWidth}, ${vm.imgHeight}"
                    )
                    Log.d(
                        TAG,
                        "ResultPart: sw: $sw, sh: $sh, imageInitialScale: $imageInitialScale, scaleByWidth: $scaleByWidth"
                    )
                }
            }
        }
        GestureContent(
            modifier = modifier
                .fillMaxSize()
                .drawBehind { drawRect(Color.Black) },
            state = gestureState,
        ) { _ ->
            // imageGestureScale = gestureScale
            // imageOffsetRect = rect
            // Log.d(TAG, "ResultPart: gestureScale: $gestureScale, rect: $rect")
            photoProvider.photo().Compose(
                contentScale = ContentScale.Fit, //if (scaleByWidth) ContentScale.FillWidth else ContentScale.FillHeight,
                isContainerDimenExactly = true,
                onSuccess = {},
                onError = { context.toastOnUi(ResStrings.failed_to_load_image) }
            )


            val result = vm.translateState.getOrNull() ?: return@GestureContent
            if (result is ImageTranslationResult.Normal) {
                NormalTransResult(
                    result, showResult, vm.translateState, lazyListState, imageInitialScale
                )
            } else if (result is ImageTranslationResult.Model) {
                ModelTransResult(
                    result, showResult, vm.translateStage
                )
            }
        }
    }
}