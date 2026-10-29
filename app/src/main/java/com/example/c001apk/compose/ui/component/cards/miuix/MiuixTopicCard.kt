package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.design.contentPress
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.squircle.squircleClip

@Composable
internal fun MiuixTopicCard(modifier: Modifier, data: HomeFeedResponse.Data, onOpenLink: (String, String?) -> Unit) {
    Card(modifier.fillMaxWidth().padding(horizontal = 16.dp).contentPress(),
        insideMargin = PaddingValues(16.dp), pressFeedbackType = PressFeedbackType.None,
        onClick = { onOpenLink(data.url.orEmpty(), data.title) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CoilLoader(url = data.logo, modifier = Modifier.size(44.dp).squircleClip(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(data.title.orEmpty(), style = MiuixTheme.textStyles.headline1)
                Text("${data.hotNumTxt.orEmpty()} 热度 · ${data.commentnumTxt.orEmpty()} 讨论",
                    style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }
    }
}
