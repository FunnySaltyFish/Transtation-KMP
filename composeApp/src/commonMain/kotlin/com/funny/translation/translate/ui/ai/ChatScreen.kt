package com.funny.translation.translate.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.sendByMe
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavAnimatedVisibilityScope
import com.funny.translation.helper.LocalSharedTransitionScope
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.blurWithDrawer
import com.funny.translation.kmp.rememberTakePhotoLauncher
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.ai.componets.AddFilePanel
import com.funny.translation.translate.ui.ai.componets.ChatInputTextField
import com.funny.translation.translate.ui.ai.componets.MessageItem
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.translate.ui.long_text.ModelListPart
import com.funny.translation.translate.ui.long_text.components.AIPointText
import com.funny.translation.translate.ui.main.LocalWindowSizeState
import com.funny.translation.translate.ui.widget.AsyncImage
import com.funny.translation.translate.ui.widget.TaskButton
import com.funny.translation.translate.utils.rememberSelectImageLauncher
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.floatingActionBarModifier
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler

// Modified From https://github.com/prafullmishra/JetComposer/tree/master

private const val TAG = "ChatScreen"

@Composable
fun ChatScreen() {
    val vm: ChatViewModel = viewModel()
    val inputText by vm.inputText
    val chatBot = vm.chatBot
    val chatMessages = vm.messages
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var previewImageUri: String? by rememberStateOf(null)
    var isPreProcessing by rememberStateOf(false)
    val pickedItems = remember { mutableStateListOf<String>() }

    BackHandler(drawerState.currentValue == DrawerValue.Open) {
        scope.launch { drawerState.close() }
    }

    val drawerWidth = 360.dp
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        content = {
            ChatContent(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .blurWithDrawer(drawerState, drawerWidthPx),
                chatBot = chatBot,
                currentMessageProvider = { vm.currentMessage },
                chatMessages = chatMessages,
                inputText = inputText,
                onInputTextChanged = vm::updateInputText,
                pickedItems = pickedItems,
                openDrawerAction = { scope.launch { drawerState.open() } },
                sendAction = {
                    // 选择图片时，不直接发送消息，而是等待图片选择完成后再发送
                    if (pickedItems.isNotEmpty()) isPreProcessing = true
                    vm.ask(inputText, pickedItems, onFinishPreprocessing = {
                        isPreProcessing = false
                        pickedItems.clear()
                    })
                },
                clearAction = vm::clearMessages,
                removeMessageAction = vm::removeMessage,
                doRefreshAction = vm::doRefresh,
                previewImageAction = { previewImageUri = it }
            )
        },
        drawerContent = {
//            Box(
//                modifier = Modifier
//                    //.width(if (LocalWindowSizeState.current.isVertical) 360.dp else 600.dp)
//                    .width(360.dp)
//                    .fillMaxHeight()
//                    .background(
//                        MaterialTheme.colorScheme.primaryContainer,
//                        RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
//                    )
//                    .statusBarsPadding()
//                    .padding(12.dp),
//            )
            Settings(
                modifier = Modifier
                    .width(drawerWidth)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .statusBarsPadding()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                vm = vm
            )
        }
    )

    AnimatedVisibility(
        visible = previewImageUri != null,
        modifier = Modifier,
        enter = expandIn(expandFrom = Alignment.Center),
        exit = shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        CompositionLocalProvider(
            LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility
        ) {
            if (previewImageUri == null) return@CompositionLocalProvider
            ImagePreviewScreen(
                imageUri = previewImageUri!!,
                modifier = Modifier.fillMaxSize(),
                animatedContentScope = this@AnimatedVisibility,
                goBackAction = {
                    previewImageUri = null
                }
            )
        }
    }

    if (isPreProcessing) {
        // Show a loading indicator
        Column (
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ChatContent(
    modifier: Modifier,
    chatBot: ModelChatBot,
    currentMessageProvider: () -> ChatMessage?,
    chatMessages: SnapshotStateList<ChatMessage>,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    pickedItems: SnapshotStateList<String>,
    openDrawerAction: () -> Unit,
    sendAction: () -> Unit,
    clearAction: () -> Unit,
    removeMessageAction: (ChatMessage) -> Unit,
    doRefreshAction: SimpleAction,
    previewImageAction: (String) -> Unit
) {
    CommonPage(
        modifier = modifier,
        title = chatBot.name,
        actions = {
            AIPointText()
        }
    ) {
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        ChatMessageList(
            modifier = Modifier.weight(1f),
            currentMessageProvider = currentMessageProvider,
            chats = chatMessages,
            lazyListState = lazyListState,
            removeMessageAction = removeMessageAction,
            doRefreshAction = doRefreshAction,
            previewImageAction = previewImageAction
        )
        ChatBottomBar(
            text = inputText,
            onTextChanged = onInputTextChanged,
            sendAction = {
                if (chatMessages.size > 1) {
                    scope.launch {
                        lazyListState.animateScrollToItem(chatMessages.size - 1)
                    }
                }
                sendAction()
            },
            clearAction = clearAction,
            chatBot = chatBot,
            pickedItems = pickedItems,
            previewImageAction = previewImageAction,
            openDrawerAction = openDrawerAction
        )
    }


}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
//@Preview
private fun ColumnScope.ChatBottomBar(
    text: String = "",
    onTextChanged: (String) -> Unit = {},
    sendAction: (pickedItems: List<String>) -> Unit,
    clearAction: () -> Unit,
    chatBot: ModelChatBot,
    pickedItems: SnapshotStateList<String>,
    previewImageAction: (String) -> Unit,
    openDrawerAction: SimpleAction
) {
    var showAddFilePanel by rememberStateOf(false)

    val inputTypes = chatBot.model.inputFileTypes
    val context = LocalContext.current
    val onImagesSelected: (List<String>) -> Unit = { list ->
        list.forEach {
            if (pickedItems.size < inputTypes.maxImageNum) {
                if (!pickedItems.contains(it)) pickedItems.add(it)
            } else {
                context.toastOnUi("您已到达此模型单次最大图片数量限制（${inputTypes.maxImageNum}张）")
                return@forEach
            }
        }
    }

    val imageSelectLauncher = rememberSelectImageLauncher(
        maxNum = inputTypes.maxImageNum,
        pickedItems = pickedItems,
        onResult = onImagesSelected
    )

    var photoUri = remember {
        getPhotoUri()
    }

    val takePhotoLauncher = rememberTakePhotoLauncher { saved ->
        if (saved) onImagesSelected(listOf(photoUri))
    }

    AnimatedVisibility(
        visible = pickedItems.isNotEmpty()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            tonalElevation = 4.dp,
            shadowElevation = 0.dp
        ) {
            Column {
                CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility) {
                    UploadFilePreviewPanel(
                        modifier = Modifier.padding(12.dp),
                        selectedItems = pickedItems,
                        previewImageAction = previewImageAction
                    )
                }
                HorizontalDivider()
            }
        }
    }

    ChatInputTextField(
        modifier = Modifier.fillMaxWidth(),
        input = text,
        onValueChange = onTextChanged,
        sendAction = {
            sendAction(pickedItems)
        },
        clearAction = clearAction,
        chatBot = chatBot,
        pickedItems = pickedItems,
        showAddFilePanel = showAddFilePanel,
        updateShowAddFilePanel = { showAddFilePanel = it },
        openDrawerAction = openDrawerAction
    )

    AnimatedVisibility(
        visible = showAddFilePanel,
    ) {
        AddFilePanel(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            selectImageAction = {
                // 调用系统 API，选择图片
                imageSelectLauncher.launch(inputTypes.imageMimeList)
            },
            takePhotoAction = {
                photoUri = getPhotoUri()
                takePhotoLauncher.launch(photoUri)
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UploadFilePreviewPanel(
    modifier: Modifier,
    selectedItems: SnapshotStateList<String>,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current,
    previewImageAction: (String) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(selectedItems, key = { it }) {
            Box {
                with(sharedTransitionScope) {
                    // 显示图片
                    AsyncImage(
                        model = it,
                        modifier = Modifier.height(128.dp).clickable {
                            previewImageAction(it)
                        },
//                        .sharedElement(
//                            state = rememberSharedContentState(it),
//                            animatedVisibilityScope = animatedVisibilityScope
//                        ),
                        contentDescription = "Image",
                        contentScale = ContentScale.FillHeight
                    )
                }


                // 删除按钮
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp),
                    onClick = {
                        selectedItems.remove(it)
                    }
                ) {
                    FixedSizeIcon(
                        Icons.Filled.Cancel,
                        contentDescription = "Clear",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessageList(
    modifier: Modifier,
    lazyListState: LazyListState,
    currentMessageProvider: () -> ChatMessage?,
    chats: List<ChatMessage>,
    removeMessageAction: (ChatMessage) -> Unit,
    doRefreshAction: SimpleAction,
    previewImageAction: (String) -> Unit
) {
    val currentMessage = currentMessageProvider()
    val context = LocalContext.current
    val msgItem: @Composable LazyItemScope.(msg: ChatMessage, refreshAction: SimpleAction?) -> Unit =
        @Composable { msg, refreshAction ->
            MessageItem(
                modifier = Modifier.animateItemPlacement(),
                maxWidth = if (LocalWindowSizeState.current.isVertical) 300.dp else 600.dp,
                chatMessage = msg,
                copyAction = {
                    ClipBoardUtil.copy(msg.content)
                    context.toastOnUi(ResStrings.copied_to_clipboard)
                },
                deleteAction = {
                    removeMessageAction(msg)
                },
                refreshAction = refreshAction,
                previewImageAction = previewImageAction
            )
        }

    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier,
            state = lazyListState
        ) {
            itemsIndexed(
                chats,
                key = { _, msg -> msg.id },
                contentType = { _, msg -> msg.type }) { i, message ->
                msgItem(
                    message,
                    if (!message.sendByMe && i == chats.lastIndex) doRefreshAction else null
                )
            }
            if (currentMessage != null) {
                item {
                    msgItem(currentMessage, doRefreshAction)
                }
            }
        }

        val alpha by animateFloatAsState(if (lazyListState.canScrollForward) 1f else 0f)
        val scope = rememberCoroutineScope()
        FloatingActionButton(
            onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(chats.lastIndex)
                }
            },
            modifier = Modifier.floatingActionBarModifier().graphicsLayer {
                this.alpha = alpha
            }
        ) {
            FixedSizeIcon(Icons.Filled.ArrowCircleDown, contentDescription = "Menu")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(
    modifier: Modifier,
    vm: ChatViewModel,
) {
    Column(modifier) {
        // Prompt
        Category(title = "Prompt", helpText = ResStrings.chat_prompt_help) {
            var text by rememberStateOf(value = vm.systemPrompt)
            TextField(value = text, onValueChange = { text = it }, maxLines = 8)
            val showConfirmButton by remember {
                derivedStateOf { text != vm.systemPrompt }
            }

            AnimatedVisibility(visible = showConfirmButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { text = vm.systemPrompt },
                    ) {
                        Text(text = ResStrings.reset)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TaskButton(
                        onClick = { vm.checkPrompt(text) },
                        loading = vm.checkingPrompt
                    ) {
                        Text(text = ResStrings.check_and_modify)
                    }
                }
            }
        }

        Category(title = ResStrings.max_history_msg_num, helpText = ResStrings.max_history_msg_num_help) {
            var v by rememberStateOf(vm.maxHistoryMsgNum.toFloat())
            val startInteractionSource = remember { MutableInteractionSource() }
            Slider(
                value = v,
                onValueChange = { v = it },
                valueRange = 2f..16f,
                onValueChangeFinished = { vm.maxHistoryMsgNum = v.toInt() },
                steps = 14,
                interactionSource = startInteractionSource,
                thumb = {
                    Label(
                        label = {
                            PlainTooltip(
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(text = v.toInt().toString(), textAlign = TextAlign.Center)
                            }
                        },
                        interactionSource = startInteractionSource,
                    ) {
                        SliderDefaults.Thumb(
                            interactionSource = startInteractionSource,
                        )
                    }
                }
            )
        }

        ModelListPart(
            maxHeight = 600.dp
        )
    }
}

expect fun getPhotoUri(): String