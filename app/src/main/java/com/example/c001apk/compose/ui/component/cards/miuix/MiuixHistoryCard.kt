package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.FeedEntity
import com.example.c001apk.compose.ui.component.design.MenuAction
import com.example.c001apk.compose.ui.component.design.MiuixActionMenuButton
import com.example.c001apk.compose.ui.component.design.contentPress
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getShareText
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
internal fun MiuixHistoryCard(
    modifier: Modifier, data: FeedEntity, onViewUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit, onDelete: (String) -> Unit,
    onBlockUser: (String) -> Unit, onOpenLink: (String, String?) -> Unit,
    onViewFeed: (String, Boolean) -> Unit, onCopyText: (String?) -> Unit,
) {
    val context = LocalContext.current
    Card(modifier.fillMaxWidth().contentPress(), cornerRadius = 20.dp,
        insideMargin = PaddingValues(16.dp), pressFeedbackType = PressFeedbackType.None,
        onClick = { onViewFeed(data.id, false) }, onLongPress = { onCopyText(data.message) }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ContentAvatar(data.avatar, 42.dp, "history-avatar-${data.id}") { onViewUser(data.uid) }
            Column(Modifier.weight(1f).clickable { onViewUser(data.uid) }, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(data.uname, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOf(fromToday(data.pubDate.toLongOrNull() ?: 0), data.device.richToString())
                    .filter { it.isNotBlank() }.joinToString(" · "), fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MiuixActionMenuButton(actions = buildList {
                add(MenuAction("复制链接") { context.copyText(getShareText(ShareType.FEED, data.id)) })
                add(MenuAction("拉黑") { onBlockUser(data.uid) })
                if (CookieUtil.isLogin) add(MenuAction("举报") { onReport(data.id, ReportType.FEED) })
                add(MenuAction("删除") { onDelete(data.id) })
            })
        }
        Spacer(Modifier.height(12.dp))
        ContentRichText(data.message, onOpenLink, Modifier.fillMaxWidth())
        val media = remember(data.mediaJson) { com.example.c001apk.compose.logic.model.FeedMediaJson.decode(data.mediaJson) }
        if (media != null && media.images.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            com.example.c001apk.compose.ui.component.NineImageView(
                modifier = Modifier.fillMaxWidth().squircleClip(12.dp), pic = media.pic, picArr = media.images, feedType = media.feedType)
        }
    }
}
