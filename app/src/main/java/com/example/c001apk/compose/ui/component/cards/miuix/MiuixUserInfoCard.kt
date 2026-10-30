package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.ImageView
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.makeToast
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixUserInfoCard(data: HomeFeedResponse.Data, onFollow: (String, Int) -> Unit,
    onPMUser: (String, String) -> Unit, onViewFFFList: (String, String) -> Unit) {
    val context = LocalContext.current
    var coverRatio by rememberSaveable(data.cover) { mutableFloatStateOf(0f) }
    Column(Modifier.fillMaxWidth().testTag("miuix-user-profile")) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val coverHeight = if (coverRatio > 0f) maxWidth / coverRatio else 150.dp
            Box(Modifier.fillMaxWidth().height(coverHeight + 40.dp)) {
                ImageView(
                    url = data.cover.orEmpty(), isCover = true,
                    modifier = Modifier.fillMaxWidth().height(coverHeight),
                    imageScaleType = android.widget.ImageView.ScaleType.FIT_CENTER,
                    onImageSize = { width, height ->
                        if (coverRatio == 0f) coverRatio = width.toFloat() / height
                    },
                )
                ImageView(url = data.userAvatar.orEmpty(), isRound = true,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp).size(80.dp))
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(data.username.orEmpty(), Modifier.weight(1f).clickable { context.copyText(data.username.orEmpty()) },
                    style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Lv.${data.level}", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.primary)
            }
            Text("UID: ${data.uid}", Modifier.clickable { context.copyText(data.uid.orEmpty()) },
                style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            if (!data.bio.isNullOrEmpty()) Text(data.bio, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = "私信", modifier = Modifier.weight(1f), onClick = {
                    if (CookieUtil.isLogin) onPMUser(data.uid.orEmpty(), data.username.orEmpty())
                    else context.makeToast("请先登录")
                })
                TextButton(text = if (data.isFollow == 1) "取消关注" else "关注", modifier = Modifier.weight(1f),
                    colors = if (data.isFollow == 1) ButtonDefaults.textButtonColors() else ButtonDefaults.textButtonColorsPrimary(),
                    onClick = { onFollow(data.uid.orEmpty(), data.isFollow ?: 0) })
            }
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()) {
                    val values = listOf((data.feed?.id ?: 0).toString(), (data.beLikeNum ?: 0).toString(), (data.follow ?: 0).toString(), (data.fans ?: 0).toString())
                    listOf("动态", "获赞", "关注", "粉丝").forEachIndexed { index, label ->
                        Column(Modifier.weight(1f).clickable(enabled = index >= 2) {
                            onViewFFFList(data.uid.orEmpty(), if (index == 2) FFFListType.USER_FOLLOW.name else FFFListType.FAN.name)
                        }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(values[index], style = MiuixTheme.textStyles.headline1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    }
                }
            }
            Text("${fromToday(data.logintime ?: 0)}活跃", style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}
