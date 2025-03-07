package com.funny.translation.translate.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import cn.qhplus.emo.photo.activity.PhotoPickResult
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.ui.GestureContent
import cn.qhplus.emo.photo.ui.GestureContentState
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.AppConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.main.components.EngineSelectDialog
import com.funny.translation.translate.ui.main.components.UpdateSelectedEngine
import com.funny.translation.translate.ui.widget.CustomCoilProvider
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.translate.utils.EngineManager
import com.funny.translation.ui.AutoResizedText
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.floatingActionBarModifier
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.min
import com.eygraber.uri.Uri as KMPUri

private const val TAG = "ImageTransScreen"

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
actual fun ImageTransScreen(
    imageUri: KMPUri?,
    sourceId: Int?,
    targetId: Int?,
    doClipFirst: Boolean
) {
    var currentPage by rememberStateOf(ImageTransPage.Main)
    val updateCurrentPage = remember {
        { page: ImageTransPage -> currentPage = page }
    }
    when (currentPage) {
        ImageTransPage.Main -> ImageTransMain(imageUri, sourceId, targetId, doClipFirst, updateCurrentPage)
        ImageTransPage.ResultList -> ImageTransResultList(updateCurrentPage)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun ImageTransMain(
    imageUri: KMPUri?,
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
//            vm.imageUri = null
            vm.cancelTranslateJob()
        }
    }

    val clipperLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { intent ->
                    val uri = UCrop.getOutput(intent)
                    vm.updateImageUri(uri)
                    val width = UCrop.getOutputImageWidth(intent)
                    val height = UCrop.getOutputImageHeight(intent)
                    vm.updateImgSize(width, height)
                    vm.translate()
                }
            }
        }

    val doClip = remember {
        { uri: Uri ->
            clipperLauncher.launch(
                UCrop.of(uri, DESTINATION_IMAGE_URI)
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
        val androidImageUri = Uri.decode(imageUri.toString()).toUri()
        // 如果不需要裁剪，那么直接翻译
        if (!doClipFirst) {
            vm.imageUri = androidImageUri
            val imageSize = BitmapUtil.getImageSizeFromUri(appCtx, androidImageUri.toString())
            if (imageSize == (-1 to -1)) return@LaunchedEffect
            vm.updateImgSize(imageSize.first, imageSize.second)
            vm.sourceLanguage = sourceId?.let { findLanguageById(it) } ?: Language.AUTO
            vm.targetLanguage = targetId?.let { findLanguageById(it) } ?: Language.CHINESE
            vm.translate()
        } else {
            // 反之调到裁剪页面
            doClip(androidImageUri)
        }
    }

    if (vm.imageUri != null) {
        ImageTranslationPart(vm = vm, updateCurrentPage = updateCurrentPage)
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
                            pickedItems = arrayListOf<Uri>().apply {
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
private fun LanguageSelectRow(
    modifier: Modifier,
    exchangeButtonTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sourceLanguage: Language,
    updateSourceLanguage: (Language) -> Unit,
    targetLanguage: Language,
    updateTargetLanguage: (Language) -> Unit,
    enabledLanguages: List<Language>,
    textColor: Color = Color.White
) {
    Row(modifier.horizontalScroll(rememberScrollState())) {
        LanguageSelect(
            Modifier.semantics {
                contentDescription = ResStrings.des_current_source_lang
            },
            language = sourceLanguage,
            languages = enabledLanguages,
            updateLanguage = updateSourceLanguage,
            textColor = textColor
        )
        ExchangeButton(tint = exchangeButtonTint) {
            val temp = sourceLanguage
            updateSourceLanguage(targetLanguage)
            updateTargetLanguage(temp)
        }
        LanguageSelect(
            Modifier.semantics {
                contentDescription = ResStrings.des_current_target_lang
            },
            language = targetLanguage,
            languages = enabledLanguages,
            updateLanguage = updateTargetLanguage,
            textColor = textColor
        )
    }
}

@Composable
private fun LanguageSelect(
    modifier: Modifier = Modifier,
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
    textColor: Color = Color.White
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    TextButton(
        modifier = modifier, onClick = {
            expanded = true
        }
    ) {
        Text(
            text = language.displayText,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
            color = textColor
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }, text = {
                    Text(it.displayText)
                })
            }
        }
    }
}

@Composable
private fun ImageTranslationPart(
    vm: ImageTransViewModel,
    updateCurrentPage: (ImageTransPage) -> Unit
) {
    val goBackTipDialogState = remember {
        mutableStateOf(false)
    }
    val currentEnabledLanguages by enabledLanguages.collectAsState()
    val goBack = remember {
        {
            if (vm.isTranslating()) goBackTipDialogState.value = true
            else vm.updateImageUri(null)
        }
    }

    SimpleDialog(
        openDialogState = goBackTipDialogState,
        ResStrings.tip,
        "当前翻译正在进行中，您确定要退出吗？",
        confirmButtonAction = {
            vm.updateImageUri(null)
        }
    )

    BackHandler(onBack = goBack)

    LaunchedEffect(vm.sourceLanguage, vm.targetLanguage, vm.translateEngine) {
        vm.updateShowTranslateButton(true)
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = goBack) {
                FixedSizeIcon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            LanguageSelectRow(
                modifier = Modifier,
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
                enabledLanguages = currentEnabledLanguages,
                textColor = MaterialTheme.colorScheme.onBackground
            )
            EngineSelect(
                engine = vm.translateEngine,
                updateEngine = vm::updateTranslateEngine,
                bindEngines = vm.bindEngines,
                modelEngines = vm.modelEngines
            )
        }
        ResultPart(modifier = Modifier.fillMaxSize(), vm = vm)
    }

    FloatButtonRow(
        showTranslateButton = vm.showTranslateButton,
        translateState = vm.translateState,
        translateAction = vm::translate,
        showResultState = vm.showResultState,
        gotoResultListAction = { updateCurrentPage(ImageTransPage.ResultList) }
    )
}

@Composable
private fun FloatButtonRow(
    showTranslateButton: Boolean,
    translateState: LoadingState<ImageTranslationResult>,
    translateAction: SimpleAction,
    showResultState: MutableState<Boolean>,
    gotoResultListAction: SimpleAction
) {
    Row(
        modifier = Modifier.floatingActionBarModifier()
    ) {
        if (showTranslateButton) {
            FloatingActionButton(
                onClick = { translateAction() },
                modifier = Modifier
            ) {
                // 开始翻译
                FixedSizeIcon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Translate",
                    tint = Color.White
                )
            }
        }

        if (translateState.isSuccess) {
            if (showTranslateButton) Spacer(modifier = Modifier.width(8.dp))
            // 解析结果
            FloatingActionButton(
                onClick = { showResultState.value = !showResultState.value },
                modifier = Modifier
            ) {
                // 解析结果
                FixedSizeIcon(
                    imageVector = if (showResultState.value) Icons.Default.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Toggle Result",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (translateState.getAsNormal() != null) {
                // 更改透明度
                FloatingActionButton(
                    onClick = gotoResultListAction,
                    modifier = Modifier
                ) {
                    // 解析结果
                    FixedSizeIcon(
                        imageVector = Icons.Filled.ViewList,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultPart(modifier: Modifier, vm: ImageTransViewModel) {
    val density = LocalDensity.current
    val showResult by vm.showResultState
    // 图片为了铺满屏幕进行的缩放
    var imageInitialScale by remember { mutableFloatStateOf(1f) }
    var scaleByWidth by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val lazyListState = rememberLazyListState()

    val photoProvider = remember(vm.imageUri) {
        vm.imageUri?.let {
            CustomCoilProvider(it, it, vm.imgWidth.toFloat() / vm.imgHeight, lazyListState)
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

            if (vm.translateState.isLoading) {
                CircularProgressIndicator(
                    Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else if (vm.translateState.isSuccess) {
                val alpha by animateFloatAsState(targetValue = if (showResult) 1f else 0f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .background(Color.LightGray.copy(0.9f))
                        .clipToBounds()
                ) {
                    val result = vm.translateState.getOrNull() ?: return@GestureContent
                    if (result is ImageTranslationResult.Normal) {
                        NormalTransResult(
                            result, gestureState, lazyListState, imageInitialScale
                        )
                    } else if (result is ImageTranslationResult.Model) {
                        ModelTransResult(
                            result
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun EngineSelect(
    engine: ImageTranslationEngine,
    updateEngine: (ImageTranslationEngine) -> Unit,
    bindEngines: List<ImageTranslationEngine>,
    modelEngines: List<ImageTranslationEngine>
) {
    val showDialog = rememberStateOf(false)
    var selectEngine by rememberStateOf(engine)

    val states = remember(bindEngines, modelEngines) {
        Log.d("FloatWindowScreen", "remember states triggered")
        hashMapOf<TranslationEngine, MutableState<Boolean>>().apply {
            bindEngines.forEach { put(it, mutableStateOf(it == engine)) }
            modelEngines.forEach { put(it, mutableStateOf(it == engine)) }
        }
    }

    LaunchedEffect(selectEngine) {
        states.forEach {
            it.value.value = (it.key == selectEngine)
        }
        updateEngine(selectEngine)
    }

    EngineSelectDialog(
        showDialog = showDialog,
        bindEngines = bindEngines,
        jsEngines = emptyList(),
        modelEngines = modelEngines,
        selectStateProvider = { states[it] ?: rememberStateOf(false) },
        updateSelectedEngine = object : UpdateSelectedEngine {
            override fun add(engine: TranslationEngine) {
                selectEngine = engine as ImageTranslationEngine
            }

            override fun remove(engine: TranslationEngine) {
                states[engine]?.value = false
            }
        }
    )
    TextButton(onClick = { showDialog.value = true }) {
        Text(text = engine.name)
    }
}

private val DESTINATION_IMAGE_URI =
    CacheManager.cacheDir.resolve("temp_des_img.png").toUri()