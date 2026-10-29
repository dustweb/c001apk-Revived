package com.example.c001apk.compose.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.CardDefaults
import com.example.c001apk.compose.ui.component.design.DropdownMenu
import com.example.c001apk.compose.ui.component.design.DropdownMenuItem
import com.example.c001apk.compose.ui.component.design.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.c001apk.compose.ui.component.design.HorizontalDivider
import com.example.c001apk.compose.ui.component.design.Icon
import com.example.c001apk.compose.ui.component.design.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.c001apk.compose.ui.component.design.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import com.example.c001apk.compose.ui.component.design.Text
import com.example.c001apk.compose.ui.component.design.TopAppBar
import com.example.c001apk.compose.ui.component.design.VerticalDivider
import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.design.MenuAction
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.icon.extended.File
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.OSSUploadPrepareModel
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.ChatEditText
import com.example.c001apk.compose.ui.component.cards.CardIndicator
import com.example.c001apk.compose.ui.component.cards.ChatLeftCard
import com.example.c001apk.compose.ui.component.cards.ChatRightCard
import com.example.c001apk.compose.ui.component.cards.ChatTimeCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.EmojiUtils
import com.example.c001apk.compose.util.EmojiUtils.coolBList
import com.example.c001apk.compose.util.EmojiUtils.emojiList
import com.example.c001apk.compose.util.OSSUtil.getImageDimensionsAndMD5
import com.example.c001apk.compose.util.OSSUtil.toHex
import com.example.c001apk.compose.util.OssUploadUtil.ossUpload
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.makeToast
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.c001apk.compose.ui.component.MoreMenuButton

/**
 * Created by bggRGjQaUbCoE on 2024/6/19
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    ukey: String,
    uid: String,
    username: String,
    onViewUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onOpenLink: (String, String?) -> Unit,
) {

    val viewModel =
        hiltViewModel<ChatViewModel, ChatViewModel.ViewModelFactory>(key = ukey) { factory ->
            factory.create(ukey = ukey)
        }

    val context = LocalContext.current
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var clearText by remember { mutableStateOf(false) }
    var clearFocus by remember { mutableStateOf(false) }
    val windowInsets = WindowInsets.navigationBars

    fun handlePickedImage(uri: android.net.Uri) {
        try {
            val result = getImageDimensionsAndMD5(context.contentResolver, uri)
            val md5Byte = result.second
            val md5 = md5Byte?.toHex() ?: ""
            val width = result.first?.first ?: 0
            val height = result.first?.second ?: 0
            val type = result.first?.third ?: ""

            viewModel.uriList = listOf(uri)
            viewModel.md5List = listOf(md5Byte)
            viewModel.typeList = listOf(type)
            val list = listOf(
                OSSUploadPrepareModel(
                    name = "${
                        UUID.randomUUID().toString().replace("-", "")
                    }.${if (type.startsWith("image/")) type.substring(6) else type}",
                    resolution = "${width}x${height}",
                    md5 = md5,
                )
            )
            viewModel.onPostOSSUploadPrepare(uid, list)
        } catch (e: Exception) {
            e.message?.let {
                context.makeToast(it)
            }
            e.printStackTrace()
        }
    }

    LaunchedEffect(key1 = viewModel.scroll) {
        if (viewModel.scroll) {
            clearText = true
            delay(150)
            lazyListState.scrollToItem(0)
            viewModel.reset()
        }
    }

    fun onClearFocus() {
        clearFocus = true
    }

    val pickContent =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uri1 ->
                handlePickedImage(uri1)
            }
        }

    val pickDocument =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { uri1 ->
                handlePickedImage(uri1)
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton {
                        clearFocus = true
                        onBackClick()
                    }
                },
                title = username,
                actions = {
                    Box {
                        MoreMenuButton { dropdownMenuExpanded = true }
                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false }) {
                            listOf(
                                stringResource(id = R.string.menu_check),
                                "拉黑",
                                stringResource(id = R.string.menu_report)
                            ).forEachIndexed { index, menu ->
                                DropdownMenuItem(
                                    text = { Text(text = menu) },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        when (index) {
                                            0 -> {
                                                onClearFocus()
                                                onViewUser(uid)
                                            }

                                            1 -> viewModel.onBlockUser(uid)
                                            2 -> {
                                                onClearFocus()
                                                onReport(uid, ReportType.USER)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
    ) { paddingValues ->
        val blur = com.example.c001apk.compose.ui.component.design.isMiuixBlurEnabled()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (blur) 0.dp else paddingValues.calculateTopPadding(),
                )
                .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Start))
        ) {
            if (!com.example.c001apk.compose.ui.theme.isMiuix()) HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(top = if (blur) paddingValues.calculateTopPadding() else 0.dp),
                state = lazyListState,
            ) {
                when (viewModel.loadingState) {
                    LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                        item(key = "loadingState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                LoadingCard(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 10.dp),
                                    state = viewModel.loadingState,
                                    onClick = if (viewModel.loadingState is LoadingState.Loading) null
                                    else viewModel::loadMore
                                )
                            }
                        }
                    }

                    is LoadingState.Success -> {
                        itemsIndexed(
                            items = (viewModel.loadingState as LoadingState.Success).response,
                            key = { _, item -> item.entityId + item.dateline },
                        ) { index, item ->
                            when (item.entityType) {
                                "message" -> when (item.fromuid) {
                                    CookieUtil.uid ->
                                        ChatRightCard(
                                            data = item,
                                            onGetImageUrl = viewModel::onGetImageUrl,
                                            onLongClick = { id, msg, url ->
                                                onClearFocus()
                                                viewModel.deleteId = id
                                                viewModel.message = msg
                                                viewModel.pic = url
                                                showDialog = true
                                            },
                                            onViewUser = { uid ->
                                                onClearFocus()
                                                onViewUser(uid)
                                            },
                                            onOpenLink = onOpenLink,
                                            onClearFocus = ::onClearFocus
                                        )

                                    else ->
                                        ChatLeftCard(
                                            data = item,
                                            onGetImageUrl = viewModel::onGetImageUrl,
                                            onLongClick = { id, msg, url ->
                                                onClearFocus()
                                                viewModel.deleteId = id
                                                viewModel.message = msg
                                                viewModel.pic = url
                                                showDialog = true
                                            },
                                            onViewUser = { uid ->
                                                onClearFocus()
                                                onViewUser(uid)
                                            },
                                            onOpenLink = onOpenLink,
                                            onClearFocus = ::onClearFocus
                                        )
                                }

                                "messageExtra" -> ChatTimeCard(title = item.title.orEmpty())
                            }

                            if (index == (viewModel.loadingState as LoadingState.Success).response.lastIndex && !viewModel.isEnd) {
                                viewModel.loadMore()
                            }
                        }
                    }
                }
            }

            ChatBottom(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (com.example.c001apk.compose.ui.theme.isMiuix()) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.background else cardBg())
                    .padding(bottom = androidx.compose.animation.core.animateDpAsState(
                        targetValue = with(androidx.compose.ui.platform.LocalDensity.current) {
                            androidx.compose.foundation.layout.WindowInsets.ime.getBottom(this).toDp()
                        },
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        ),
                        label = "ChatImeAnimation"
                    ).value)
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Bottom)),
                onPickImage = {
                    onClearFocus()
                    pickContent.launch("image/*")
                },
                onPickOtherImage = {
                    onClearFocus()
                    pickDocument.launch(arrayOf("image/*"))
                },
                onSendMessage = {
                    viewModel.onSendMessage(uid, it, EMPTY_STRING)
                },
                clearFocus = clearFocus,
                clearText = clearText,
                resetClear = {
                    clearText = false
                },
                resetFocus = {
                    clearFocus = false
                },
                onClearFocus = ::onClearFocus,
                viewModel = viewModel,
            )
        }

    }

    when {
        viewModel.showUploadDialog -> {
            LoadingDialog()
        }

        showDialog -> {
            if (com.example.c001apk.compose.ui.theme.isMiuix()) {
                com.example.c001apk.compose.ui.component.design.ActionDialog("消息操作", buildList {
                    add(MenuAction("删除") { viewModel.onDeleteMsg() })
                    if (viewModel.pic.isEmpty()) add(MenuAction("复制") { context.copyText(viewModel.message) })
                }, { showDialog = false })
            } else Dialog(onDismissRequest = { showDialog = false }) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "删除",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    viewModel.onDeleteMsg()
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (viewModel.pic.isEmpty()) {
                            Text(
                                text = "复制",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDialog = false
                                        context.copyText(viewModel.message)
                                    }
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }

    viewModel.uploadImage?.let { responseData ->
        LaunchedEffect(responseData) {
            withContext(Dispatchers.IO) {
                ossUpload(
                    context = context,
                    responseData = responseData,
                    uriList = viewModel.uriList,
                    typeList = viewModel.typeList,
                    md5List = viewModel.md5List,
                    iOnSuccess = {
                        viewModel.onSendMessage(
                            uid,
                            EMPTY_STRING,
                            "/${responseData.fileInfo.getOrNull(0)?.uploadFileName}"
                        )
                    },
                    iOnFailure = {
                        viewModel.showUploadDialog = false
                        context.makeToast("图片上传失败")
                    },
                    closeDialog = {
                        viewModel.showUploadDialog = false
                    }
                )
            }
            viewModel.resetUploadImage()
        }
    }

}

@Composable
fun ChatBottom(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onPickImage: () -> Unit,
    onPickOtherImage: () -> Unit,
    onSendMessage: (String) -> Unit,
    clearText: Boolean,
    resetClear: () -> Unit,
    clearFocus: Boolean,
    resetFocus: () -> Unit,
    onClearFocus: () -> Unit,
) {
    val recentList by
    viewModel.recentEmojiData.collectAsStateWithLifecycle(initialValue = emptyList())
    var shouldShowSendBtn by remember { mutableStateOf(false) }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var clickedEmoji by remember { mutableStateOf<String?>(null) }
    var textInput by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(EMPTY_STRING) }
    var pagerState: PagerState
    val context = LocalContext.current

    val miuix = com.example.c001apk.compose.ui.theme.isMiuix()
    val actionTint = if (miuix) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantActions
        else MaterialTheme.colorScheme.onSurfaceVariant
    val inputRenderer = androidx.compose.runtime.rememberUpdatedState<@Composable (Modifier) -> Unit> { inputModifier ->
        ChatEditText(modifier = inputModifier, initialText = textInput,
                showSendBtn = { shouldShowSendBtn = it },
                updateInputText = {
                    textInput = it
                },
                clearText = clearText,
                resetClear = resetClear,
                clearFocus = clearFocus,
                resetFocus = resetFocus,
                clickedEmoji = clickedEmoji,
                resetEmoji = {
                    clickedEmoji = null
                },
                onCloseEmojiPanel = {
                    showEmojiPanel = false
                },
            )
    }
    val input = remember { androidx.compose.runtime.movableContentOf<Modifier> { inputRenderer.value(it) } }

    Column(modifier = modifier) {
        if (miuix) {
            top.yukonga.miuix.kmp.basic.Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                insideMargin = PaddingValues(8.dp),
                pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.None,
            ) {
                input(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onClearFocus(); showEmojiPanel = !showEmojiPanel }) {
                        Icon(Icons.Outlined.EmojiEmotions, "表情", tint = actionTint)
                    }
                    IconButton(onClick = onPickImage) { Icon(Icons.Outlined.AddPhotoAlternate, "选择图片", tint = actionTint) }
                    IconButton(onClick = onPickOtherImage) {
                        Icon(top.yukonga.miuix.kmp.icon.MiuixIcons.Medium.File, "选择文件中的图片", tint = actionTint)
                    }
                    Spacer(Modifier.weight(1f))
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "发送", enabled = shouldShowSendBtn, onClick = { onSendMessage(textInput) },
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        } else Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(modifier = Modifier.size(48.dp), onClick = {
                        onClearFocus()
                        showEmojiPanel = !showEmojiPanel
                    }) {
                Icon(imageVector = Icons.Outlined.EmojiEmotions, contentDescription = "表情",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            input(Modifier.weight(1f).padding(vertical = 6.dp)
                .clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 6.dp))
            if (!shouldShowSendBtn) {
                Row {
                    IconButton(modifier = Modifier.size(48.dp), onClick = {
                                onPickImage()
                            }) {
                        Icon(imageVector = Icons.Outlined.AddPhotoAlternate, contentDescription = "选择图片",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {
                                onPickOtherImage()
                            }) {
                        if (com.example.c001apk.compose.ui.theme.isMiuix()) Icon(
                            top.yukonga.miuix.kmp.icon.MiuixIcons.Medium.File,
                            contentDescription = "选择文件中的图片",
                        ) else Image(
                            painter = androidx.compose.ui.res.painterResource(
                                id = R.drawable.outline_note_alt_24
                            ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                IconButton(modifier = Modifier.size(48.dp), onClick = {
                            onSendMessage(textInput)
                        }) {
                    Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "发送消息",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (showEmojiPanel) {
            pagerState = rememberPagerState(
                initialPage = if (recentList.isEmpty()) 1 else 0,
                pageCount = { 3 }
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) { index ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    val pagerState1 = rememberPagerState {
                        when (index) {
                            0 -> 1
                            1 -> 4
                            2 -> 2
                            else -> 0
                        }
                    }
                    HorizontalPager(
                        state = pagerState1,
                        modifier = Modifier.fillMaxWidth()
                    ) { index0 ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(28) {
                                val emojiName = when (index) {
                                    0 -> recentList.getOrNull(it)?.data
                                    1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.first
                                    2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.first
                                    else -> null
                                }
                                val emojiRes = when (index) {
                                    0 -> emojiName?.let { name ->
                                        EmojiUtils.emojiMap.getValue(name)
                                    }

                                    1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.second
                                    2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.second
                                    else -> null
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            emojiName?.let { name ->
                                                clickedEmoji = name
                                                if (pagerState.currentPage != 0) {
                                                    viewModel.updateRecentEmoji(
                                                        name,
                                                        recentList.size,
                                                        recentList.lastOrNull()?.data
                                                    )
                                                }
                                            }
                                            if (it == 27) clickedEmoji = "[c001apk]"
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (emojiRes != null) {
                                        Image(
                                            painter = rememberDrawablePainter(
                                                drawable = ResourcesCompat.getDrawable(
                                                    context.resources,
                                                    emojiRes,
                                                    context.theme
                                                )
                                            ),
                                            contentDescription = emojiName,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else if (it == 27) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                            contentDescription = "删除一个字符",
                                            tint = actionTint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                            }
                        }
                    }
                    CardIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp),
                        pagerState = pagerState1,
                        defColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    )
                }
            }
            if (!com.example.c001apk.compose.ui.theme.isMiuix()) HorizontalDivider()
            if (com.example.c001apk.compose.ui.theme.isMiuix()) {
                val scope = rememberCoroutineScope()
                com.example.c001apk.compose.ui.component.design.FilledTabs(
                    tabs = listOf("最近", "默认", "酷币"), selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            } else Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextIndicator(
                    index = 0,
                    title = "最近",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 1,
                    title = "默认",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 2,
                    title = "酷币",
                    pagerState = pagerState,
                )
            }
        }
    }
}

@Composable
fun RowScope.TextIndicator(
    modifier: Modifier = Modifier,
    index: Int,
    title: String,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()
    Text(
        text = title,
        modifier = modifier
            .weight(1f)
            .background(
                if (pagerState.currentPage == index)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent
            )
            .clickable {
                scope.launch {
                    pagerState.scrollToPage(index)
                }
            }
            .padding(vertical = 10.dp),
        color = if (pagerState.currentPage == index)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleSmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoadingDialog() {
    com.example.c001apk.compose.ui.component.design.ProgressDialog("正在上传")
}
