package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.ui.component.NineImageView
import com.example.c001apk.compose.util.longClick
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixArticleContent(item: FeedArticleContentBean, onOpenLink: (String, String?) -> Unit, onCopyText: (String) -> Unit) {
    when (item.type) {
        "text" -> ContentRichText(item.message, onOpenLink, Modifier.fillMaxWidth()
            .longClick { onCopyText(item.message.orEmpty()) }.padding(horizontal = 20.dp, vertical = 8.dp))
        "image" -> Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            NineImageView(modifier = Modifier.squircleClip(16.dp),
                pic = null, picArr = listOf(item.url.orEmpty()), feedType = null, isSingle = true)
            if (!item.description.isNullOrBlank()) Text(item.description,
                Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp), fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        "shareUrl" -> Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            cornerRadius = 16.dp, insideMargin = PaddingValues(16.dp),
            onClick = { onOpenLink(item.url.orEmpty(), item.title) }) {
            Text(item.title.orEmpty(), fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.primary)
        }
    }
}
