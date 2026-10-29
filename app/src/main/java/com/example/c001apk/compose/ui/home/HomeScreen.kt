package com.example.c001apk.compose.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import top.yukonga.miuix.kmp.icon.extended.Ok
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.platform.LocalConfiguration
import top.yukonga.miuix.kmp.icon.extended.Close
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.c001apk.compose.ui.component.design.CircularProgressIndicator
import com.example.c001apk.compose.ui.component.design.HorizontalDivider
import com.example.c001apk.compose.ui.component.design.Icon
import com.example.c001apk.compose.ui.component.design.IconButton
import com.example.c001apk.compose.ui.component.design.ListItem
import androidx.compose.material3.MaterialTheme
import com.example.c001apk.compose.ui.component.design.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import com.example.c001apk.compose.ui.component.design.SecondaryScrollableTabRow
import com.example.c001apk.compose.ui.component.design.Surface
import com.example.c001apk.compose.ui.component.design.Switch
import com.example.c001apk.compose.ui.component.design.Tab
import androidx.compose.material3.TabRowDefaults
import com.example.c001apk.compose.ui.component.design.Text
import com.example.c001apk.compose.ui.component.design.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.PaddingValues
import com.example.c001apk.compose.ui.component.design.LocalHomePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.c001apk.compose.ui.component.settings.PreferenceTopBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.zIndex
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.design.FullScreenDialog as Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.ui.home.app.AppListScreen
import com.example.c001apk.compose.ui.home.feed.HomeFeedScreen
import com.example.c001apk.compose.ui.home.feed.HomeFeedActions
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filterNotNull

/**
 * Created by bggRGjQaUbCoE on 2024/6/5
 */

enum class TabType {
    FOLLOW, APP, FEED, HOT, TOPIC, PRODUCT, COOLPIC
}

private fun tabTitle(type: TabType): String {
    return when (type) {
        TabType.FOLLOW -> "关注"
        TabType.APP -> "应用"
        TabType.FEED -> "动态"
        TabType.HOT -> "热榜"
        TabType.TOPIC -> "话题"
        TabType.PRODUCT -> "数码"
        TabType.COOLPIC -> "酷图"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTabEditorDialog(
    orderedTabs: List<TabType>,
    enabledTabs: Set<TabType>,
    onDismiss: () -> Unit,
    onConfirm: (List<TabType>, Set<TabType>) -> Unit,
) {
    val miuix = com.example.c001apk.compose.ui.theme.isMiuix()
    val pageScroll = MiuixScrollBehavior()
    var selectedTabs by remember(enabledTabs) { mutableStateOf(enabledTabs) }
    val tabs = remember(orderedTabs) {
        mutableStateListOf<TabType>().apply { addAll(orderedTabs) }
    }
    val listState = rememberLazyListState()
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var draggedItemOffset by remember { mutableFloatStateOf(0f) }
    val dragScope = rememberCoroutineScope()
    val returnOffset = remember { Animatable(0f) }
    var returningTab by remember { mutableStateOf<TabType?>(null) }
    var returnJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun finishNativeDrag() {
        val released = tabs.getOrNull(draggedItemIndex)
        val offset = draggedItemOffset
        if (released != null) {
            returningTab = released
            returnJob?.cancel()
            returnJob = dragScope.launch {
                returnOffset.snapTo(offset)
                returnOffset.animateTo(0f, spring(dampingRatio = .85f, stiffness = 400f))
                if (returningTab == released) returningTab = null
            }
        }
        draggedItemIndex = -1
        draggedItemOffset = 0f
    }


    fun moveDraggedTab(change: androidx.compose.ui.input.pointer.PointerInputChange, amount: androidx.compose.ui.geometry.Offset) {
        val currentIndex = draggedItemIndex
        if (currentIndex == -1) return
        change.consume()
        draggedItemOffset += amount.y
        val currentItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex } ?: return
        val center = currentItem.offset + draggedItemOffset + currentItem.size / 2f
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.index != currentIndex && center.toInt() in it.offset..(it.offset + it.size)
        } ?: return
        tabs.add(target.index, tabs.removeAt(currentIndex))
        draggedItemIndex = target.index
        draggedItemOffset += currentItem.offset - target.offset
    }


    val editorContent: @Composable (Modifier) -> Unit = { panelModifier ->
        Surface(modifier = panelModifier) {
            Scaffold(
                modifier = Modifier,
                contentWindowInsets = if (miuix) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
                topBar = {
                    if (!miuix) PreferenceTopBar(
                        title = "编辑首页板块", scrollBehavior = pageScroll, onBack = onDismiss,
                        actions = {
                            if (miuix) IconButton(onClick = rememberHapticClick { onConfirm(tabs.toList(), selectedTabs) }) {
                                Icon(top.yukonga.miuix.kmp.icon.MiuixIcons.Medium.Ok, contentDescription = "完成")
                            } else TextButton(onClick = { onConfirm(tabs.toList(), selectedTabs) }) {
                                Text("完成")
                            }
                        },
                    )
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (miuix) top.yukonga.miuix.kmp.basic.SmallTitle("已启用 ${selectedTabs.size} / ${tabs.size} · 长按调整顺序")
                    else Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "首页板块",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "已启用 ${selectedTabs.size} / ${tabs.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = com.example.c001apk.compose.ui.component.design.secondaryTextColor(),
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                            .pointerInput(miuix, tabs) {
                            if (!miuix) detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    draggedItemIndex = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { item ->
                                            offset.y.toInt() in item.offset..(item.offset + item.size)
                                        }
                                        ?.index
                                        ?: -1
                                    draggedItemOffset = 0f
                                },
                                onDragCancel = {
                                    draggedItemIndex = -1
                                    draggedItemOffset = 0f
                                },
                                onDragEnd = {
                                    draggedItemIndex = -1
                                    draggedItemOffset = 0f
                                },
                                onDrag = ::moveDraggedTab,
                            )
                            },
                    ) {
                        itemsIndexed(tabs, key = { _, type -> type.name }) { index, type ->
                            val checked = type in selectedTabs
                            val canToggle = !checked || selectedTabs.size > 1
                            if (miuix) {
                                val feedback = rememberHapticClick {}
                                val dragging = index == draggedItemIndex
                                val settling = returningTab == type
                                val scale by animateFloatAsState(if (dragging) 1.02f else 1f,
                                    spring(dampingRatio = .85f, stiffness = 400f, visibilityThreshold = .0001f), label = "tab-drag-scale")
                                top.yukonga.miuix.kmp.basic.Card(
                                    Modifier.animateItem(placementSpec = if (dragging || settling) null else spring(dampingRatio = 1f, stiffness = 450f))
                                        .padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth()
                                        .zIndex(if (dragging || settling) 1f else 0f)
                                        .graphicsLayer {
                                            translationY = if (dragging) draggedItemOffset else if (settling) returnOffset.value else 0f
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                    pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.None,
                                ) {
                                    androidx.compose.runtime.CompositionLocalProvider(
                                        androidx.compose.ui.platform.LocalHapticFeedback provides com.example.c001apk.compose.ui.component.design.NoNativeHaptics
                                    ) {
                                        top.yukonga.miuix.kmp.preference.SwitchPreference(
                                            title = tabTitle(type), checked = checked, enabled = canToggle,
                                            startAction = {
                                                Box(Modifier.size(40.dp).pointerInput(type) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = {
                                                            returnJob?.cancel()
                                                            returningTab = null
                                                            feedback()
                                                            draggedItemIndex = tabs.indexOf(type)
                                                            draggedItemOffset = 0f
                                                        },
                                                        onDragCancel = ::finishNativeDrag,
                                                        onDragEnd = ::finishNativeDrag,
                                                        onDrag = ::moveDraggedTab,
                                                    )
                                                }, contentAlignment = Alignment.Center) {
                                                    Icon(com.example.c001apk.compose.ui.component.design.AppGlyphs.DragGrip, "长按拖动排序")
                                                }
                                            },
                                            onCheckedChange = { enabled ->
                                                feedback()
                                                selectedTabs = if (enabled) selectedTabs + type else selectedTabs - type
                                            },
                                        )
                                    }
                                }
                            } else {
                                ListItem(
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "长按拖动排序",
                                            tint = MaterialTheme.colorScheme.outline,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            translationY = if (index == draggedItemIndex) draggedItemOffset else 0f
                                        }
                                        .clickable(enabled = canToggle) {
                                            selectedTabs = if (checked) {
                                                selectedTabs - type
                                            } else {
                                                selectedTabs + type
                                            }
                                        },
                                    headlineContent = { Text(tabTitle(type)) },
                                    supportingContent = {
                                        Text(
                                            text = if (checked) "已显示" else "已隐藏",
                                            color = com.example.c001apk.compose.ui.component.design.secondaryTextColor(),
                                        )
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = checked,
                                            enabled = canToggle,
                                            onCheckedChange = { enabled ->
                                                selectedTabs = if (enabled) {
                                                    selectedTabs + type
                                                } else {
                                                    selectedTabs - type
                                                }
                                            },
                                        )
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (miuix) {
        var visible by remember { mutableStateOf(true) }
        var save by remember { mutableStateOf(false) }
        val maxHeight = LocalConfiguration.current.screenHeightDp.dp * .78f
        com.example.c001apk.compose.ui.component.design.GlobalPopupBlur(visible)
        top.yukonga.miuix.kmp.window.WindowBottomSheet(
            show = visible, title = "编辑首页板块",
            onDismissRequest = { visible = false },
            onDismissFinished = { if (save) onConfirm(tabs.toList(), selectedTabs) else onDismiss() },
            insideMargin = DpSize(0.dp, 0.dp),
            allowDismiss = draggedItemIndex == -1,
            enableNestedScroll = draggedItemIndex == -1,
            startAction = {
                IconButton(modifier = Modifier.padding(start = 16.dp), onClick = rememberHapticClick { visible = false }) {
                    Icon(top.yukonga.miuix.kmp.icon.MiuixIcons.Medium.Close, "关闭", tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface)
                }
            },
            endAction = {
                IconButton(modifier = Modifier.padding(end = 16.dp), onClick = rememberHapticClick { save = true; visible = false }) {
                    Icon(top.yukonga.miuix.kmp.icon.MiuixIcons.Medium.Ok, "完成", tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary)
                }
            },
        ) { editorContent(Modifier.fillMaxWidth().heightIn(max = maxHeight)) }
    } else Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        editorContent(Modifier.fillMaxSize())
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val scope = rememberCoroutineScope()
    val homeFeedListState = rememberLazyListState()

    val menus by viewModel.homeMenus.collectAsStateWithLifecycle()
    val storedMenus = menus ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val orderedTabs = remember(storedMenus) {
        if (storedMenus.isEmpty()) {
            TabType.entries
        } else {
            val storedOrder = storedMenus
                .asSequence()
                .sortedBy(HomeMenu::position)
                .mapNotNull { menu ->
                    runCatching { TabType.valueOf(menu.title) }.getOrNull()
                }
                .distinct()
                .toList()
            storedOrder + TabType.entries.filterNot(storedOrder::contains)
        }
    }
    val enabledTabs = remember(storedMenus) {
        if (storedMenus.isEmpty()) {
            TabType.entries.toSet()
        } else {
            storedMenus
                .asSequence()
                .filter(HomeMenu::isEnable)
                .mapNotNull { menu -> runCatching { TabType.valueOf(menu.title) }.getOrNull() }
                .toSet()
        }
    }
    val tabList = remember(orderedTabs, enabledTabs) {
        orderedTabs.filter(enabledTabs::contains).ifEmpty { listOf(TabType.FEED) }
    }
    var selectedTabType by rememberSaveable { mutableStateOf(TabType.FEED) }
    val initialPage = tabList.indexOf(selectedTabType).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = {
            tabList.size
        }
    )
    val context = LocalContext.current
    var showTabEditor by rememberSaveable { mutableStateOf(false) }
    val selectedTabIndex = pagerState.currentPage.coerceIn(tabList.indices)
    val miuix = com.example.c001apk.compose.ui.theme.isMiuix()
    val onTabSelected: (Int) -> Unit = { index ->
        if (pagerState.currentPage == index) onRefresh()
        selectedTabType = tabList[index]
        scope.launch { pagerState.animateScrollToPage(index) }
    }

    LaunchedEffect(pagerState, tabList) {
        val targetType = selectedTabType.takeIf(tabList::contains)
            ?: TabType.FEED.takeIf(tabList::contains)
            ?: tabList.first()
        selectedTabType = targetType
        val targetIndex = tabList.indexOf(targetType)
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
        // 先按板块名称恢复位置，再记录分页结果，避免旧页码覆盖自定义排序。
        snapshotFlow {
            pagerState.settledPage.takeUnless { pagerState.isScrollInProgress }
        }.filterNotNull().collect { page ->
            tabList.getOrNull(page)?.let { selectedTabType = it }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (miuix) {
                MiuixHomeHeader(
                    labels = tabList.map { tabTitle(it) },
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = onTabSelected,
                    onSearch = onSearch,
                    onEditTabs = { showTabEditor = true },
                )
            }
        },
        floatingActionButton = {
            HomeFeedActions(
                listState = homeFeedListState,
                isActive = tabList.getOrNull(selectedTabIndex) == TabType.FEED,
                canPublish = isLogin,
                onPublish = {
                    val intent = Intent(context, ReplyActivity::class.java)
                    intent.putExtra("type", "createFeed")
                    val animationBundle = ActivityOptionsCompat.makeCustomAnimation(
                        context,
                        R.anim.anim_bottom_sheet_slide_up,
                        R.anim.anim_bottom_sheet_slide_down
                    ).toBundle()
                    ContextCompat.startActivity(context, intent, animationBundle)
                },
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize().padding(top = if (miuix) 0.dp else paddingValues.calculateTopPadding()),
        ) {
            if (!miuix) Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SecondaryScrollableTabRow(
                    labels = tabList.map { tabTitle(it) },
                    onTabSelected = onTabSelected,
                    modifier = Modifier.weight(1f),
                    selectedTabIndex = selectedTabIndex,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(selectedTabIndex, matchContentSize = true)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    },
                    divider = {}
                ) {
                    tabList.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = rememberHapticClick { onTabSelected(index) },
                            text = { Text(text = tabTitle(tab)) }
                        )
                    }
                }
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onSearch() },
                ) {
                    Icon(
                        modifier = Modifier.size(21.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                    )
                }
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = rememberHapticClick { showTabEditor = true },
                ) {
                    Icon(
                        modifier = Modifier.size(21.dp),
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "编辑首页板块",
                    )
                }
            }

            if (!com.example.c001apk.compose.ui.theme.isMiuix()) HorizontalDivider()

            CompositionLocalProvider(LocalHomePadding provides if (miuix) paddingValues else PaddingValues()) {
            HorizontalPager(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = pagerState,
                key = { tabList[it].name },
            ) { index ->

                when (val type = tabList[index]) {
                    TabType.FOLLOW, TabType.FEED, TabType.HOT, TabType.COOLPIC ->
                        HomeFeedScreen(
                            refreshState = refreshState,
                            resetRefreshState = resetRefreshState,
                            type = type,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onReport = onReport,
                            lazyListState = if (type == TabType.FEED) homeFeedListState
                                else rememberLazyListState(),
                        )

                    TabType.APP -> AppListScreen(
                        refreshState = refreshState,
                        resetRefreshState = resetRefreshState,
                        onViewApp = onViewApp,
                        onCheckUpdate = onCheckUpdate,
                    )

                    TabType.TOPIC, TabType.PRODUCT -> HomeTopicScreen(
                        type = type,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText
                    )
                }

            }
            }

        }

    }

    if (showTabEditor) {
        HomeTabEditorDialog(
            orderedTabs = orderedTabs,
            enabledTabs = enabledTabs,
            onDismiss = { showTabEditor = false },
            onConfirm = { newOrder, newEnabledTabs ->
                viewModel.setTabs(newOrder, newEnabledTabs)
                showTabEditor = false
            },
        )
    }

}
