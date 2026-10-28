package com.example.c001apk.compose.ui.main

import com.example.c001apk.compose.ui.component.design.PageBackAction
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.SizeTransform
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.design.AppNavigationScaffold
import com.example.c001apk.compose.ui.component.design.NavigationDestination
import com.example.c001apk.compose.ui.component.SlideTransition
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.MiuixPageMotion
import com.example.c001apk.compose.ui.theme.isMiuix
import com.example.c001apk.compose.ui.home.HomeScreen
import com.example.c001apk.compose.ui.message.MessageScreen
import com.example.c001apk.compose.ui.settings.SettingsScreen
import com.example.c001apk.compose.util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
@Composable
fun MainScreen(
    selectIndex: Int,
    setSelectIndex: (Int) -> Unit,
    badge: Int,
    resetBadge: () -> Unit,
    onParamsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onThemeSettingsClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onLogin: () -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewBlackList: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    widthSizeClass: WindowWidthSizeClass,
    isActive: Boolean = true,
) {

    val screens = listOf(
        Router.HOME,
        Router.MESSAGE,
        Router.SETTINGS
    )

    val savableStateHolder = rememberSaveableStateHolder()
    val performHapticClick = rememberHapticClick {}
    var refreshState by remember { mutableStateOf(false) }
    var tabDirection by remember { mutableIntStateOf(1) }
    val miuix = isMiuix()

    AppNavigationScaffold(
        selected = selectIndex,
        destinations = screens.map {
            NavigationDestination(stringResource(it.stringId!!), it.unselectedIcon!!, it.selectedIcon!!)
        },
        compact = widthSizeClass == WindowWidthSizeClass.Compact,
        badge = badge,
        onSelect = { index ->
            performHapticClick()
            if (index != selectIndex) tabDirection = if (index > selectIndex) 1 else -1
            if (selectIndex == 0 && index == 0) refreshState = true
            else if (index == 1 && badge != 0) resetBadge()
            setSelectIndex(index)
        },
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize().clipToBounds(),
            label = "home-content",
            targetState = selectIndex,
            transitionSpec = {
                if (miuix) {
                    MiuixPageMotion.tabEnter(tabDirection)
                        .togetherWith(MiuixPageMotion.tabExit(tabDirection))
                        .using(SizeTransform(clip = true))
                } else {
                    SlideTransition.slideLeft.enterTransition()
                        .togetherWith(SlideTransition.slideLeft.exitTransition())
                }
            },
        ) { page ->
            savableStateHolder.SaveableStateProvider(
                key = page,
                content = {
                    when (page) {
                        0 -> HomeScreen(
                            refreshState = refreshState,
                            onRefresh = {
                                refreshState = true
                            },
                            resetRefreshState = {
                                refreshState = false
                            },
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onSearch = onSearch,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewApp = onViewApp,
                            onCheckUpdate = onCheckUpdate,
                            onReport = onReport,
                        )

                        1 -> MessageScreen(
                            onLogin = onLogin,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewFFFList = onViewFFFList,
                            onReport = onReport,
                            onViewNotice = onViewNotice,
                            onViewHistory = onViewHistory,
                        )

                        2 -> SettingsScreen(
                            onParamsClick = onParamsClick,
                            onAboutClick = onAboutClick,
                            onThemeSettingsClick = onThemeSettingsClick,
                            onViewBlackList = onViewBlackList,
                        )
                    }
                }
            )
        }
    }

    PageBackAction(enabled = isActive && selectIndex != 0) {
        tabDirection = -1
        setSelectIndex(0)
    }

}
