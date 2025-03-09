package com.funny.translation.translate.ui.image

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cn.qhplus.emo.photo.activity.PhotoPickResult
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import com.funny.compose.loading.LoadingState
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
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.zoom.ReadMode
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
            val targetImagePath = "${System.currentTimeMillis()}.jpg"
            clipperLauncher.launch(
                UCrop.of(uri, DESTINATION_IMAGE_FOLDER.resolve(targetImagePath).toUri())
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

    val zoomState = rememberSketchZoomState()
    val zoomableState = zoomState.zoomable
    val contentSize = zoomableState.contentSize

    LaunchedEffect(contentSize) {
        if (contentSize == IntSize.Zero) return@LaunchedEffect
        val sw = contentSize.width.toFloat() / vm.imgWidth
        val sh = contentSize.height.toFloat() / vm.imgHeight
        vm.imageInitialScale = min(sw, sh)
        Log.d(TAG, "ResultPart: contentSize: ${contentSize}, img: ${vm.imgWidth}*${vm.imgHeight}, sw*sh: $sw*$sh")
    }

    LaunchedEffect(zoomState.zoomable) {
        zoomState.zoomable.readMode = ReadMode()
    }

    Box(modifier
        .fillMaxSize()
        .drawBehind { drawRect(Color.Black) }
    ) {
        SketchZoomAsyncImage(
            uri = vm.imageUri,
            contentDescription = "image",
            modifier = Modifier
                .fillMaxSize(),
            zoomState = zoomState
        )

        val result = vm.translateState.getOrNull() ?: return
        if (result is ImageTranslationResult.Normal) {
            NormalTransResult(
                result, showResult, vm.translateState, contentSize, zoomableState, vm.imageInitialScale
            )
        } else if (result is ImageTranslationResult.Model) {
            ModelTransResult(
                result, showResult, vm.translateStage
            )
        }
    }
}

typealias Part = com.funny.translation.translate.ImageTranslationPart

@Preview
@Composable
fun PreviewResultPart() {
    val vm = remember {
        ImageTransViewModel().apply {
            imageUri = "file:///storage/emulated/0/Android/data/com.funny.translation.debug/cache/temp_des_img.png"
            translateState = LoadingState.Success(ImageTranslationResult.Normal(
                // content=[ImageTranslationPart(source=17:42 , target=17:42 , x=68, y=49, width=97, height=34), ImageTranslationPart(source=1.73 HD HD MB/s 47 , target=1.73高清MB/s 47, x=673, y=47, width=407, height=37), ImageTranslationPart(source=√ , target=√ , x=58, y=177, width=26, height=21), ImageTranslationPart(source=Research | OpenAl , target=研究| OpenAl, x=143, y=165, width=411, height=52), ImageTranslationPart(source=OpenAl , target=OpenAl, x=58, y=314, width=241, height=66), ImageTranslationPart(source=Research , target=研究, x=509, y=554, width=179, height=34), ImageTranslationPart(source=Pioneering research on the path to AGI , target=AGI之路的开创性研究, x=167, y=712, width=870, height=211), ImageTranslationPart(source=We believe our research will eventually lead to artificial general intelligence, a system that can solve human-level problems. Building safe and beneficial AGI is our mission. , target=我们相信，我们的研究最终将导致通用人工智能，一个可以解决人类层面问题的系统。构建安全且有益的AGI是我们的使命。, x=74, y=1034, width=1052, height=297), ImageTranslationPart(source=View research index , target=查看研究索引, x=162, y=1490, width=403, height=35), ImageTranslationPart(source=Learn about safety > , target=了解安全>, x=680, y=1491, width=409, height=40), ImageTranslationPart(source=。。榮 , target=。。榮 , x=106, y=2212, width=41, height=91), ImageTranslationPart(source=Ask ChatGPT , target=询问ChatGPT, x=357, y=2339, width=299, height=40), ImageTranslationPart(source=个 , target=个 , x=804, y=2335, width=45, height=49), ImageTranslationPart(source=< , target=< , x=102, y=2517, width=33, height=63), ImageTranslationPart(source=> , target=> , x=340, y=2515, width=35, height=65), ImageTranslationPart(source=1 , target=1., x=830, y=2532, width=17, height=29)])
                content = listOf(
                    Part(source = "17:42", target = "17:42", x = 68, y = 49, width = 97, height = 34),
                    Part(source = "1.73 HD HD MB/s 47", target = "1.73高清MB/s 47", x = 673, y = 47, width = 407, height = 37),
                    Part(source = "√", target = "√", x = 58, y = 177, width = 26, height = 21),
                    Part(source = "Research | OpenAl", target = "研究| OpenAl", x = 143, y = 165, width = 411, height = 52),
                    Part(source = "OpenAl", target = "OpenAl", x = 58, y = 314, width = 241, height = 66),
                )
            ))
            showResultState.value = true
            val size = BitmapUtil.getImageSizeFromUri(appCtx, imageUri!!)
            imgWidth = size.first
            imgHeight = size.second
        }
    }

    ResultPart(Modifier, vm)
}

//    val lazyListState = rememberLazyListState()
//
//    val photoProvider = remember(vm.imageUri) {
//        vm.imageUri?.let {
//            val uri = it.toUri()
//            CustomCoilProvider(uri, uri, vm.imgWidth.toFloat() / vm.imgHeight, lazyListState)
//        }
//    }
//
//    photoProvider?.let {
//        val gestureState = remember(it) {
//            GestureContentState(
//                ratio = it.ratio,
//                isLongContent = it.isLongImage(),
//            )
//        }
//        LaunchedEffect(key1 = gestureState.layoutInfo) {
//            gestureState.layoutInfo?.let { layoutInfo ->
//                val sw = layoutInfo.px.contentWidth / vm.imgWidth
//                val sh = layoutInfo.px.contentHeight / vm.imgHeight
//                if (gestureState.isLongContent) {
//                    scaleByWidth = true
//                    imageInitialScale = sw
//                } else {
//                    scaleByWidth = sw < sh
//                    imageInitialScale = min(sw, sh)
//                    Log.d(
//                        TAG,
//                        "ResultPart: size: ${layoutInfo.contentWidth}, ${layoutInfo.contentHeight}, img: ${vm.imgWidth}, ${vm.imgHeight}"
//                    )
//                    Log.d(
//                        TAG,
//                        "ResultPart: sw: $sw, sh: $sh, imageInitialScale: $imageInitialScale, scaleByWidth: $scaleByWidth"
//                    )
//                }
//            }
//        }
//        GestureContent(
//            modifier = modifier
//                .fillMaxSize()
//                .drawBehind { drawRect(Color.Black) },
//            state = gestureState,
//        ) { _ ->
//            // imageGestureScale = gestureScale
//            // imageOffsetRect = rect
//            // Log.d(TAG, "ResultPart: gestureScale: $gestureScale, rect: $rect")
//            photoProvider.photo().Compose(
//                contentScale = ContentScale.Fit, //if (scaleByWidth) ContentScale.FillWidth else ContentScale.FillHeight,
//                isContainerDimenExactly = true,
//                onSuccess = {},
//                onError = { context.toastOnUi(ResStrings.failed_to_load_image) }
//            )
//
//
//            val result = vm.translateState.getOrNull() ?: return@GestureContent
//            if (result is ImageTranslationResult.Normal) {
//                NormalTransResult(
//                    result, showResult, vm.translateState, lazyListState, imageInitialScale
//                )
//            } else if (result is ImageTranslationResult.Model) {
//                ModelTransResult(
//                    result, showResult, vm.translateStage
//                )
//            }
//        }
//    }
//}