package com.example.c001apk.compose.ui.component

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.method.LinkMovementMethodCompat
import com.example.c001apk.compose.logic.providable.LocalDragHandoffToParent
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.ImageShowUtil
import com.example.c001apk.compose.view.LinkTextView
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Created by bggRGjQaUbCoE on 2024/6/2
 */
@Composable
fun LinkText(
    modifier: Modifier = Modifier,
    text: String?,
    textSize: Float = 15f,
    isBold: Boolean = false,
    lineSpacingMultiplier: Float = 1.0f,
    maxLines: Int? = null,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    color: Int? = null,
    onOpenLink: ((String, String?) -> Unit)? = null,
    isReply: Boolean = false,
    onShowTotalReply: (() -> Unit)? = null,
    imgList: List<String>? = null,
    linkColor: Int? = null,
) {
    val contentColor = LocalContentColor.current
    val userPreference = LocalUserPreferences.current
    val dragHandoffToParent = LocalDragHandoffToParent.current

    val primary = linkColor ?: (if (isMiuix()) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary).toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LinkTextView(context).apply {
                setParams(
                    isReply = isReply,
                    size = textSize,
                    fontScale = userPreference.fontScale * userPreference.contentScale,
                )
                if (isBold)
                    setTypeface(typeface, Typeface.BOLD)
                setLineSpacing(0.0f, lineSpacingMultiplier)
                maxLines?.let {
                    this.maxLines = it
                    this.ellipsize = ellipsize
                }
                setTextColor(contentColor.toArgb())
                color?.let {
                    setTextColor(it)
                }
            }
        },
        update = { textView ->
            textView.setParams(isReply = isReply, size = textSize, fontScale = userPreference.fontScale * userPreference.contentScale)
            textView.setTextColor(color ?: contentColor.toArgb())
            textView.setTypeface(textView.typeface, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textView.setLineSpacing(0f, lineSpacingMultiplier)
            textView.maxLines = maxLines ?: Int.MAX_VALUE
            textView.ellipsize = if (maxLines != null) ellipsize else null
            textView.dragHandoffToParent = dragHandoffToParent
            textView.setSpText(
                text = text.orEmpty(),
                color = primary,
                onOpenLink = { url, title ->
                    onOpenLink?.let { it(url, title) }
                },
                onShowTotalReply = onShowTotalReply,
                onShowImages = { url ->
                    ImageShowUtil.startBigImgViewSimple(
                        textView.context,
                        imgList ?: listOf(url),
                        userAgent = userPreference.userAgent,
                    )
                }
            )
        }
    )
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val contentColor = LocalContentColor.current.toArgb()
    val primary = (if (isMiuix()) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary).toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethodCompat.getInstance()
                setTextColor(contentColor)
                setLinkTextColor(primary)
                text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
        },
        update = {
            it.setTextColor(contentColor)
            it.setLinkTextColor(primary)
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        },
    )
}
