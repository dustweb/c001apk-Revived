package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.design.MiuixActionMenuButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.c001apk.compose.ui.component.cards.parseGeneratedRelation
import com.example.c001apk.compose.ui.component.cards.replyBody
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.makeToast
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import com.example.c001apk.compose.ui.component.design.AppGlyphs
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun MiuixReplyCard(
    modifier: Modifier,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    onShowTotalReply: ((String, String, String?) -> Unit)?,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)?,
    isTotalReply: Boolean,
    isTopReply: Boolean,
    isReply2Reply: Boolean,
    onLike: ((String, Int, LikeType) -> Unit)?,
    onDelete: ((String, LikeType, String?) -> Unit)?,
    onBlockUser: (String, String?) -> Unit,
    onReply: ((String, String, String, String?, String?) -> Unit)?,
    replyUserAvatars: Map<String, String>,
) {
    val context = LocalContext.current
    val likeNotice = data.likeUserInfo != null
    val feedReply = data.fetchType == "feed_reply"
    val authorName = data.likeUserInfo?.username ?: data.userInfo?.username ?: data.username.orEmpty().richToString()
    val avatar = data.likeUserInfo?.userAvatar ?: data.userInfo?.userAvatar ?: data.userAvatar
    val authorUid = data.likeUserInfo?.uid ?: data.uid.orEmpty()
    val reply: () -> Unit = {
        if (CookieUtil.isLogin) onReply?.invoke(data.id.orEmpty(), data.uid.orEmpty(),
            data.userInfo?.username ?: data.username.orEmpty().richToString(), null,
            data.userInfo?.userAvatar ?: data.userAvatar)
        else context.makeToast("请先登录")
    }
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = if (feedReply) 5.dp else 0.dp)
            .testTag("miuix-comment-${data.id}"),
        cornerRadius = 20.dp, insideMargin = PaddingValues(16.dp),
        pressFeedbackType = if (likeNotice) PressFeedbackType.None else PressFeedbackType.Sink,
        onClick = if (likeNotice) null else if (feedReply) reply else ({ onOpenLink(data.url.orEmpty(), null) }),
        onLongPress = { onCopyText(data.message) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ContentAvatar(avatar, 36.dp, "comment-avatar-${data.id}") { onViewUser(authorUid) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val relation = if (isTotalReply && data.ruid != "0") parseGeneratedRelation(data.username) else null
                if (relation == null) {
                    Text(authorName, Modifier.clickable { onViewUser(authorUid) },
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, color = MiuixTheme.colorScheme.onBackground)
                } else {
                    ReplyIdentity(relation.first, relation.second, replyUserAvatars[data.ruid], onOpenLink)
                }
                val metadata = listOfNotNull(
                    fromToday(if (likeNotice) data.likeTime ?: 0 else data.dateline ?: 0),
                    data.ipLocation?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                Text(metadata, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            if (data.isFeedAuthor == 1 || isTopReply) {
                Surface(shape = RoundedCornerShape(7.dp), color = MiuixTheme.colorScheme.tertiaryContainer) {
                    Text(if (isTopReply) "原评论" else "楼主", Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = MiuixTheme.colorScheme.primary)
                }
            }
            if (!likeNotice) {
                MiuixActionMenuButton(
                    modifier = Modifier.testTag("comment-menu-${data.id}"),
                    contentDescription = "评论操作", actions = buildList {
                        add(ContentAction("复制", MiuixIcons.Copy) { onCopyText(data.message) })
                        add(ContentAction("拉黑", MiuixIcons.Blocklist) { onBlockUser(data.uid.orEmpty(), null) })
                        onShowTotalReply?.let {
                            add(ContentAction("显示回复", MiuixIcons.Reply) { it(data.id.orEmpty(), data.uid.orEmpty(), null) })
                        }
                        if (CookieUtil.isLogin) onReport?.let {
                            add(ContentAction("举报", MiuixIcons.Report) { it(data.id.orEmpty(), ReportType.REPLY) })
                        }
                        if (!data.uid.isNullOrEmpty() && data.uid == CookieUtil.uid) onDelete?.let {
                            add(ContentAction("删除", MiuixIcons.Delete) { it(data.id.orEmpty(), LikeType.REPLY, null) })
                        }
                })
            }
        }
        Spacer(Modifier.height(12.dp))
        val message = when {
            likeNotice -> "赞了你的${data.infoHtml?.takeIf { it.isNotBlank() } ?: "评论"}"
            isReply2Reply -> replyBody(data.message)
            !feedReply && !data.ruid.isNullOrEmpty() && data.ruid != "0" ->
                "回复<a href=\"/u/${data.ruid}\">${data.rusername.orEmpty()}</a>：${data.message.orEmpty()}"
            else -> data.message.orEmpty()
        }
        ContentRichText(message, onOpenLink, Modifier.fillMaxWidth())
        if (likeNotice) {
            ReplySource(data.username, data.message, data.pic, null, onOpenLink)
        } else {
            if (!data.picArr.isNullOrEmpty()) NineImageView(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).squircleClip(12.dp),
                pic = data.pic, picArr = data.picArr, feedType = data.feedType)
            data.feed?.let { ReplySource(it.username, it.message, it.pic, it.url, onOpenLink) }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                ContentActionButton(MiuixIcons.Reply, "回复", data.replynum.orEmpty(), "comment-reply-${data.id}", onClick = reply)
                ContentActionButton(if (data.userAction?.like == 1) AppGlyphs.LikeFilled else AppGlyphs.Like,
                    "点赞", data.likenum.orEmpty(), "comment-like-${data.id}",
                    active = data.userAction?.like == 1) {
                    if (CookieUtil.isLogin) onLike?.invoke(data.id.orEmpty(), data.userAction?.like ?: 0, LikeType.REPLY)
                    else context.makeToast("请先登录")
                }
            }
            if (!isTotalReply && (!data.replyRows.isNullOrEmpty() || (data.replyRowsMore ?: 0) > 0)) {
                Spacer(Modifier.height(8.dp))
                MiuixReplyRows(
                    data = data.replyRows, replyRowsMore = data.replyRowsMore ?: 0,
                    replyNum = data.replynum.orEmpty(), replyUserAvatars = replyUserAvatars,
                    showReplyTargetAvatar = false,
                    onShowTotalReply = { id -> onShowTotalReply?.invoke(id ?: data.id.orEmpty(), data.uid.orEmpty(),
                        if (!id.isNullOrEmpty()) data.id.orEmpty() else null) },
                    onOpenLink = onOpenLink, onCopyText = onCopyText, onReport = onReport,
                    onBlockUser = { uid -> onBlockUser(uid, if (uid == data.uid) null else data.id.orEmpty()) },
                    onDelete = { id, type -> onDelete?.invoke(id, type, data.id.orEmpty()) },
                    onReply = { rid, name, replyAvatar ->
                        onReply?.invoke(rid, data.uid.orEmpty(), name, data.id.orEmpty(), replyAvatar)
                    },
                )
            }
        }
    }
}

@Composable
private fun ReplyIdentity(author: String, target: String, avatar: String?, onOpenLink: (String, String?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ContentRichText(author, onOpenLink, Modifier.weight(1f), size = 14f, maxLines = 1)
        Text("回复", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        if (!avatar.isNullOrBlank()) CoilLoader(url = avatar, modifier = Modifier.size(16.dp).clip(CircleShape))
        ContentRichText(target, onOpenLink, Modifier.weight(1f), size = 14f, maxLines = 1)
    }
}

@Composable
private fun ReplySource(author: String?, message: String?, picture: String?, url: String?, onOpenLink: (String, String?) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        .then(if (!url.isNullOrEmpty()) Modifier.clickable { onOpenLink(url, null) } else Modifier),
        shape = RoundedCornerShape(14.dp), color = MiuixTheme.colorScheme.surface) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!picture.isNullOrBlank()) CoilLoader(url = picture, modifier = Modifier.size(48.dp).squircleClip(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("@${author.orEmpty().richToString()}", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                ContentRichText(message, onOpenLink, size = 13f, maxLines = 2, secondary = true)
            }
        }
    }
}

@Composable
fun MiuixReplyRows(
    modifier: Modifier = Modifier,
    data: List<HomeFeedResponse.Data>?,
    replyRowsMore: Int,
    replyNum: String,
    onShowTotalReply: (String?) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)?,
    onBlockUser: (String) -> Unit,
    onDelete: ((String, LikeType) -> Unit)?,
    onReply: (String, String, String?) -> Unit,
    replyUserAvatars: Map<String, String>,
    showReplyTargetAvatar: Boolean,
) {
    var menuIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    Card(modifier.fillMaxWidth().testTag("miuix-thread-preview"), cornerRadius = 14.dp,
        insideMargin = PaddingValues(4.dp),
        colors = CardColors(MiuixTheme.colorScheme.surface, MiuixTheme.colorScheme.onBackground)) {
        data.orEmpty().forEachIndexed { index, reply ->
            Box {
                Card(modifier = Modifier.fillMaxWidth().testTag("nested-reply-${reply.id}"),
                    cornerRadius = 10.dp, insideMargin = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    colors = CardColors(MiuixTheme.colorScheme.surface, MiuixTheme.colorScheme.onBackground),
                    onClick = rememberHapticClick {
                        if (CookieUtil.isLogin) onReply(reply.id.orEmpty(), reply.username.orEmpty(),
                            reply.userInfo?.userAvatar ?: reply.userAvatar)
                        else context.makeToast("请先登录")
                    },
                    onLongPress = { menuIndex = index }) {
                    if (showReplyTargetAvatar && reply.ruid != "0" && !reply.rusername.isNullOrEmpty()) {
                        ReplyIdentity("<a href=\"/u/${reply.uid}\">${reply.username}</a>",
                            "<a href=\"/u/${reply.ruid}\">${reply.rusername}</a>", replyUserAvatars[reply.ruid], onOpenLink)
                        ContentRichText(replyBody(reply.message), onOpenLink, Modifier.padding(top = 4.dp), size = 14f,
                            isReply = true, onShowReplies = { onShowTotalReply(null) }, images = reply.picArr)
                    } else {
                        ContentRichText(reply.message, onOpenLink, size = 14f, isReply = true,
                            onShowReplies = { onShowTotalReply(null) }, images = reply.picArr)
                    }
                }
                ContentMenu(menuIndex == index, { menuIndex = -1 }, buildList {
                    add(ContentAction("复制", MiuixIcons.Copy) { onCopyText(reply.message) })
                    add(ContentAction("拉黑", MiuixIcons.Blocklist) { onBlockUser(reply.uid.orEmpty()) })
                    add(ContentAction("显示回复", MiuixIcons.Reply) { onShowTotalReply(reply.id.orEmpty()) })
                    if (CookieUtil.isLogin) onReport?.let {
                        add(ContentAction("举报", MiuixIcons.Report) { it(reply.id.orEmpty(), ReportType.REPLY) })
                    }
                    if (!reply.uid.isNullOrEmpty() && reply.uid == CookieUtil.uid) onDelete?.let {
                        add(ContentAction("删除", MiuixIcons.Delete) { it(reply.id.orEmpty(), LikeType.REPLY) })
                    }
                })
            }
        }
        if (replyRowsMore != 0 || data.orEmpty().size > 1) {
            val label = if (replyRowsMore != 0) "查看更多回复($replyNum)"
                else "查看全部评论${replyNum.takeIf { it.isNotBlank() }?.let { "($it)" }.orEmpty()}"
            Surface(onClick = rememberHapticClick { onShowTotalReply(null) },
                modifier = Modifier.fillMaxWidth().testTag("view-all-replies"), color = MiuixTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.weight(1f), fontSize = 14.sp, color = MiuixTheme.colorScheme.primary)
                    Icon(MiuixIcons.Basic.ArrowRight, null, Modifier.size(16.dp), tint = MiuixTheme.colorScheme.primary)
                }
            }
        }
    }
}
