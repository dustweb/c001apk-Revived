package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.ImageView
import com.example.c001apk.compose.ui.component.LinkText
import com.example.c001apk.compose.ui.component.design.contentPress
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.ImageShowUtil
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixConversationCard(modifier: Modifier, data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit, onViewUser: (String) -> Unit,
    onHandleMessage: ((String, Int) -> Unit)?, onViewChat: ((String, String, String) -> Unit)?) {
    Card(modifier.fillMaxWidth().contentPress().testTag("miuix-conversation-${data.ukey}"),
        insideMargin = PaddingValues(16.dp), pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.None,
        onClick = { onViewChat?.invoke(data.ukey.orEmpty(), data.messageUid.orEmpty(), data.messageUsername.orEmpty()) },
        onLongPress = { onHandleMessage?.invoke(data.ukey.orEmpty(), data.isTop ?: 0) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ContentAvatar(data.messageUserAvatar, 48.dp, "conversation-avatar") { onViewUser(data.messageUid.orEmpty()) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.messageUsername.orEmpty(), Modifier.weight(1f), style = MiuixTheme.textStyles.headline1,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(fromToday(data.dateline ?: 0), style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentRichText(data.message, onOpenLink, Modifier.weight(1f), size = 14f, maxLines = 1, secondary = true)
                    if (data.isTop == 1) Text("置顶", style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.primary)
                    if ((data.unreadNum ?: 0) > 0) Badge { Text(data.unreadNum.toString()) }
                }
            }
        }
    }
}

@Composable
fun MiuixChatBubble(modifier: Modifier, data: HomeFeedResponse.Data, outgoing: Boolean,
    onGetImageUrl: (String) -> Unit, onLongClick: (String, String, String) -> Unit,
    onViewUser: (String) -> Unit, onOpenLink: (String, String?) -> Unit, onClearFocus: () -> Unit) {
    val pic = data.messagePic.orEmpty()
    LaunchedEffect(data.id, pic) { if (pic.isNotEmpty() && !pic.startsWith("http")) onGetImageUrl(data.id.orEmpty()) }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val bubbleWidth = ((maxWidth - 78.dp) * .9f).coerceAtLeast(64.dp)
        val background = if (outgoing) MiuixTheme.colorScheme.primaryContainer else MiuixTheme.colorScheme.surfaceContainer
        val foreground = if (outgoing) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurfaceContainer
        val avatar: @Composable () -> Unit = {
            ContentAvatar(data.fromUserAvatar, 36.dp, "chat-avatar-${data.id}") {
                onViewUser(if (outgoing) data.fromuid.orEmpty() else data.messageUid.orEmpty())
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top) {
            if (!outgoing) { avatar(); Spacer(Modifier.width(10.dp)) }
            Card(Modifier.widthIn(max = bubbleWidth).testTag("miuix-chat-${data.id}"),
                cornerRadius = 16.dp, insideMargin = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                colors = CardColors(background, foreground),
                onLongPress = { onLongClick(data.id.orEmpty(), data.message.orEmpty(), pic) }) {
                if (!data.message.isNullOrEmpty()) LinkText(text = data.message, textSize = 16f,
                    color = foreground.toArgb(), linkColor = MiuixTheme.colorScheme.primary.toArgb(), onOpenLink = onOpenLink)
                if (pic.isNotEmpty()) {
                    val ratio = runCatching { ImageShowUtil.getImageLp(pic.substringBefore('?')) }
                        .getOrNull()?.let { if (it.first > 0 && it.second > 0) it.first.toFloat() / it.second else 1f } ?: 1f
                    if (!data.message.isNullOrEmpty()) Spacer(Modifier.height(8.dp))
                    ImageView(url = pic, isChat = true, onClearFocus = onClearFocus,
                        modifier = Modifier.width(bubbleWidth * .75f).aspectRatio(ratio.coerceIn(.3f, 3f)))
                }
            }
            if (outgoing) { Spacer(Modifier.width(10.dp)); avatar() }
        }
    }
}
