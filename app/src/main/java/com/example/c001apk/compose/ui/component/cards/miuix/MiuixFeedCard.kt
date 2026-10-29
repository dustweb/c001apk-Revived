package com.example.c001apk.compose.ui.component.cards.miuix

import com.example.c001apk.compose.ui.component.design.contentPress
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.design.MiuixActionMenuButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.NineImageView
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getShareText
import com.example.c001apk.compose.util.makeToast
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import com.example.c001apk.compose.ui.component.design.AppGlyphs
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun MiuixFeedCard(
    modifier: Modifier,
    isFeedContent: Boolean,
    isFeedTop: Boolean,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)?,
    onLike: ((String, Int, LikeType) -> Unit)?,
    onDelete: ((String, LikeType, String?) -> Unit)?,
    onBlockUser: (String) -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = if (isFeedContent) 12.dp else 0.dp, vertical = if (isFeedContent) 6.dp else 0.dp)
            .testTag("miuix-feed-${data.id}").contentPress(!isFeedContent),
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.None,
        onClick = if (isFeedContent) null else ({ onViewFeed(data.id.orEmpty(), false) }),
        onLongPress = { onCopyText(data.message) },
    ) {
        if (!isFeedContent) {
            MiuixFeedHeader(data = data, onViewUser = onViewUser, isFeedContent = false,
                isFeedTop = isFeedTop, onReport = onReport, onDelete = onDelete, onBlockUser = onBlockUser)
            Spacer(Modifier.height(12.dp))
        }
        MiuixFeedMessage(data, onOpenLink, onViewFeed, onCopyText)
        MiuixFeedRows(data.relationRows, data.targetRow, onOpenLink)
        Spacer(Modifier.height(8.dp))
        MiuixFeedBottomInfo(
            isFeedContent = isFeedContent, ip = data.ipLocation.orEmpty(), dateline = data.dateline ?: 0,
            replyNum = data.replynum.orEmpty(), likeNum = data.likenum.orEmpty(), like = data.userAction?.like,
            onViewFeed = { onViewFeed(data.id.orEmpty(), true) },
            onLike = {
                if (CookieUtil.isLogin) onLike?.invoke(data.id.orEmpty(), data.userAction?.like ?: 0, LikeType.FEED)
                else context.makeToast("请先登录")
            },
            tag = data.id.orEmpty(),
        )
    }
}

@Composable
fun MiuixFeedHeader(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    isFeedContent: Boolean,
    isFeedTop: Boolean,
    onReport: ((String, ReportType) -> Unit)? = null,
    onDelete: ((String, LikeType, String?) -> Unit)? = null,
    onBlockUser: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    Row(modifier.fillMaxWidth().padding(vertical = if (isFeedContent && !isFeedTop) 12.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ContentAvatar(data.userAvatar, if (isFeedTop) 32.dp else 42.dp, "feed-avatar-${data.id}") {
            onViewUser(data.uid.orEmpty())
        }
        Column(Modifier.weight(1f).clickable { onViewUser(data.uid.orEmpty()) },
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(data.username.orEmpty().richToString(), fontSize = if (isFeedTop) 15.sp else 16.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MiuixTheme.colorScheme.onBackground)
            val description = listOfNotNull(
                (if (isFeedContent) fromToday(data.dateline ?: 0) else data.infoHtml?.richToString())?.takeIf { it.isNotBlank() },
                data.deviceTitle?.richToString()?.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (description.isNotEmpty()) Text(description, fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1, overflow = TextOverflow.Ellipsis, style = MiuixTheme.textStyles.footnote1)
        }
        if (data.isStickTop == 1 && !isFeedContent) {
            Surface(shape = RoundedCornerShape(7.dp), color = MiuixTheme.colorScheme.tertiaryContainer) {
                Text("置顶", Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    color = MiuixTheme.colorScheme.primary, fontSize = 11.sp)
            }
        }
        if (!isFeedContent) {
            MiuixActionMenuButton(
                modifier = Modifier.testTag("feed-menu-${data.id}"),
                contentDescription = "动态操作", actions = buildList {
                add(ContentAction("复制链接", MiuixIcons.Medium.Copy) { context.copyText(getShareText(ShareType.FEED, data.id.orEmpty())) })
                onBlockUser?.let { add(ContentAction("拉黑", MiuixIcons.Medium.Blocklist) { it(data.uid.orEmpty()) }) }
                if (CookieUtil.isLogin) onReport?.let { add(ContentAction("举报", MiuixIcons.Medium.Report) { it(data.id.orEmpty(), ReportType.FEED) }) }
                if (!data.uid.isNullOrEmpty() && data.uid == CookieUtil.uid) onDelete?.let {
                    add(ContentAction("删除", MiuixIcons.Medium.Delete) { it(data.id.orEmpty(), LikeType.FEED, null) })
                }
            })
        }
    }
}

@Composable
fun MiuixFeedBottomInfo(
    modifier: Modifier = Modifier,
    isFeedContent: Boolean,
    ip: String,
    dateline: Long,
    replyNum: String,
    likeNum: String,
    onViewFeed: () -> Unit,
    onLike: () -> Unit,
    like: Int?,
    tag: String = "detail",
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(if (isFeedContent) ip.takeIf { it.isNotBlank() }?.let { "发布于 $it" }.orEmpty() else fromToday(dateline),
            modifier = Modifier.weight(1f), color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        ContentActionButton(MiuixIcons.Medium.Reply, "回复", replyNum, "feed-replies-$tag", onClick = onViewFeed)
        ContentActionButton(if (like == 1) AppGlyphs.LikeFilled else AppGlyphs.Like,
            "点赞", likeNum, "feed-like-$tag", like == 1, onLike)
    }
}

@Composable
fun MiuixFeedRows(
    relationRows: List<HomeFeedResponse.RelationRows>?,
    targetRow: HomeFeedResponse.TargetRow?,
    onOpenLink: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = buildList {
        targetRow?.let { add(HomeFeedResponse.RelationRows(it.id.orEmpty(), it.logo, it.title, it.url, it.targetType.toString())) }
        addAll(relationRows.orEmpty())
    }
    if (rows.isEmpty()) return
    LazyRow(modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rows.size) { index ->
            val row = rows[index]
            Surface(onClick = { onOpenLink(row.url.orEmpty(), row.title) },
                shape = RoundedCornerShape(12.dp), color = MiuixTheme.colorScheme.surface) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!row.logo.isNullOrEmpty()) CoilLoader(url = row.logo, modifier = Modifier.size(20.dp).squircleClip(5.dp))
                    Text(row.title.orEmpty().richToString(), fontSize = 13.sp, maxLines = 1,
                        color = MiuixTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun MiuixFeedMessage(
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onCopyText: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!data.messageTitle.isNullOrBlank()) ContentRichText(data.messageTitle, onOpenLink, size = 18f, bold = true)
        if (!data.message.isNullOrBlank()) ContentRichText(data.message, onOpenLink, Modifier.fillMaxWidth().testTag("feed-body-${data.id}"))
        if (!data.picArr.isNullOrEmpty()) NineImageView(Modifier.fillMaxWidth().squircleClip(12.dp),
            data.pic, data.picArr, data.feedType)
        if (!data.forwardSourceType.isNullOrEmpty()) {
            val source = data.forwardSourceFeed
            Card(cornerRadius = 14.dp, insideMargin = PaddingValues(12.dp),
                colors = top.yukonga.miuix.kmp.basic.CardColors(MiuixTheme.colorScheme.surface, MiuixTheme.colorScheme.onBackground),
                onClick = source?.let { { onOpenLink(it.url.orEmpty(), null) } },
                onLongPress = source?.let { { onCopyText(it.message) } }) {
                if (source == null) Text("动态已被删除", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                else {
                    ContentRichText("<a href=\"/u/${source.uid}\">@${source.username}</a>：${source.messageTitle?.takeIf { it.isNotBlank() } ?: source.message.orEmpty()}",
                        onOpenLink, size = 14f)
                    if (!source.messageTitle.isNullOrEmpty() && !source.message.isNullOrEmpty())
                        ContentRichText(source.message, onOpenLink, Modifier.padding(top = 6.dp), size = 14f)
                    if (!source.picArr.isNullOrEmpty()) NineImageView(
                        Modifier.fillMaxWidth().padding(top = 8.dp).squircleClip(10.dp),
                        source.pic, source.picArr, source.feedType)
                }
            }
        }
        data.replyRows?.firstOrNull()?.let { reply ->
            Surface(onClick = { onViewFeed(data.id.orEmpty(), true) },
                shape = RoundedCornerShape(14.dp), color = MiuixTheme.colorScheme.surface) {
                val imageLink = reply.pic?.takeIf { it.isNotBlank() }?.let {
                    " <a class=\"feed-forward-pic\" href=\"$it\">查看图片(${reply.picArr?.size ?: 1})</a>"
                }.orEmpty()
                ContentRichText("<a href=\"/u/${reply.uid}\">${reply.username}</a>：${reply.message.orEmpty()}$imageLink",
                    onOpenLink, Modifier.fillMaxWidth().padding(12.dp), size = 14f, images = reply.picArr)
            }
        }
        if (!data.extraUrl.isNullOrEmpty()) {
            Surface(onClick = { onOpenLink(data.extraUrl, data.extraTitle) },
                shape = RoundedCornerShape(14.dp), color = MiuixTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!data.extraPic.isNullOrEmpty()) CoilLoader(url = data.extraPic,
                        modifier = Modifier.size(44.dp).squircleClip(10.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!data.extraTitle.isNullOrBlank()) Text(data.extraTitle, fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(data.extraUrl, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }
        }
    }
}
