package com.example.c001apk.compose.ui.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import com.example.c001apk.compose.ui.component.design.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import com.example.c001apk.compose.ui.component.design.Scaffold
import com.example.c001apk.compose.ui.component.design.Text
import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.design.MenuAction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.c001apk.compose.ui.component.settings.PreferenceTopBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.ui.component.CommonScreen
import com.example.c001apk.compose.ui.ffflist.FFFContentViewModel
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast

/**
 * Created by bggRGjQaUbCoE on 2024/6/13
 */

enum class NoticeType {
    AT, COMMENT, LIKE, FOLLOW, MESSAGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(
    onBackClick: () -> Unit,
    type: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewChat: (String, String, String) -> Unit,
) {

    val viewModel =
        hiltViewModel<NoticeViewModel, NoticeViewModel.ViewModelFactory>(key = type) { factory ->
            factory.create(
                url = when (type) {
                    NoticeType.AT.name -> "/v6/notification/atMeList"
                    NoticeType.COMMENT.name -> "/v6/notification/atCommentMeList"
                    NoticeType.LIKE.name -> "/v6/notification/feedLikeList"
                    NoticeType.FOLLOW.name -> "/v6/notification/contactsFollowList"
                    NoticeType.MESSAGE.name -> "/v6/message/list"
                    else -> throw IllegalArgumentException("invalid type: $type")
                }
            )
        }

    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }
    var showMessageDialog by remember { mutableStateOf(false) }

    val pageScroll = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().then(if (com.example.c001apk.compose.ui.theme.isMiuix()) Modifier.nestedScroll(pageScroll.nestedScrollConnection) else Modifier),
        topBar = {
            PreferenceTopBar(
                scrollBehavior = pageScroll,
                onBack = onBackClick,
                title = when (type) {
                            NoticeType.AT.name -> "@我的动态"
                            NoticeType.COMMENT.name -> "@我的评论"
                            NoticeType.LIKE.name -> "我收到的赞"
                            NoticeType.FOLLOW.name -> "好友关注"
                            NoticeType.MESSAGE.name -> "私信"
                            else -> throw IllegalArgumentException("invalid type: $type")
                        }
            )
        }
    ) { paddingValues ->

        CommonScreen(
            viewModel = viewModel,
            refreshState = null,
            resetRefreshState = {},
            paddingValues = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateLeftPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding(),
            ),
            needTopPadding = true,
            onViewUser = onViewUser,
            onViewFeed = onViewFeed,
            onOpenLink = onOpenLink,
            onCopyText = onCopyText,
            onReport = onReport,
            onHandleMessage = { ukey, isTop ->
                viewModel.ukey = ukey
                viewModel.isTop = isTop
                showMessageDialog = true
            },
            onViewChat = { ukey, uid, username ->
                viewModel.resetUnRead(ukey)
                onViewChat(ukey, uid, username)
            }
        )
    }

    when {
        showMessageDialog -> {
            if (com.example.c001apk.compose.ui.theme.isMiuix()) {
                com.example.c001apk.compose.ui.component.design.ActionDialog("对话操作", listOf(
                    MenuAction(if (viewModel.isTop == 1) "移除置顶" else "置顶") { viewModel.onHandleMessage(FFFContentViewModel.ActionType.TOP) },
                    MenuAction("删除此对话") { viewModel.onHandleMessage(FFFContentViewModel.ActionType.DELETE) },
                ), { showMessageDialog = false })
            } else Dialog(onDismissRequest = { showMessageDialog = false }) {
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
                            text = if (viewModel.isTop == 1) "移除置顶" else "置顶",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMessageDialog = false
                                    viewModel.onHandleMessage(FFFContentViewModel.ActionType.TOP)
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "删除此对话",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMessageDialog = false
                                    viewModel.onHandleMessage(FFFContentViewModel.ActionType.DELETE)
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
