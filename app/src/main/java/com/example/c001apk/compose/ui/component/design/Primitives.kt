package com.example.c001apk.compose.ui.component.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation as materialSurfaceAtElevation
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Text(
    text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null, fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified, textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip, softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {}, style: TextStyle = LocalTextStyle.current,
) {
    val foreground = if (color != Color.Unspecified) color else if (style.color != Color.Unspecified) style.color else LocalContentColor.current
    if (isMiuix()) top.yukonga.miuix.kmp.basic.Text(
        text, modifier, color = foreground, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style,
    ) else androidx.compose.material3.Text(
        text, modifier, color = color, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style,
    )
}

@Composable
fun Text(
    text: AnnotatedString, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null, fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified, textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip, softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: (TextLayoutResult) -> Unit = {}, style: TextStyle = LocalTextStyle.current,
) {
    val foreground = if (color != Color.Unspecified) color else if (style.color != Color.Unspecified) style.color else LocalContentColor.current
    if (isMiuix()) top.yukonga.miuix.kmp.basic.Text(
        text, modifier, color = foreground, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style, inlineContent = inlineContent,
    ) else androidx.compose.material3.Text(
        text, modifier, color = color, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style, inlineContent = inlineContent,
    )
}

@Composable
fun Icon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.Icon(miuixIcon(imageVector), contentDescription, modifier, tint)
    else androidx.compose.material3.Icon(imageVector, contentDescription, modifier, tint)
}

@Composable
fun Icon(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.Icon(painter, contentDescription, modifier, tint)
    else androidx.compose.material3.Icon(painter, contentDescription, modifier, tint)
}

@Composable
fun Icon(bitmap: ImageBitmap, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.Icon(bitmap, contentDescription, modifier, tint)
    else androidx.compose.material3.Icon(bitmap, contentDescription, modifier, tint)
}

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = Color.Unspecified) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.HorizontalDivider(modifier, thickness,
        if (color == Color.Unspecified) MiuixTheme.colorScheme.dividerLine else color)
    else androidx.compose.material3.HorizontalDivider(modifier, thickness,
        if (color == Color.Unspecified) MaterialTheme.colorScheme.outlineVariant else color)
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = Color.Unspecified) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.VerticalDivider(modifier, thickness,
        if (color == Color.Unspecified) MiuixTheme.colorScheme.dividerLine else color)
    else androidx.compose.material3.VerticalDivider(modifier, thickness,
        if (color == Color.Unspecified) MaterialTheme.colorScheme.outlineVariant else color)
}

@Composable
fun Surface(
    modifier: Modifier = Modifier, shape: Shape = androidx.compose.ui.graphics.RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface, contentColor: Color = androidx.compose.material3.contentColorFor(color),
    tonalElevation: Dp = 0.dp, shadowElevation: Dp = 0.dp, border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    val background = if (!isMiuix() && color == MaterialTheme.colorScheme.surface)
        MaterialTheme.colorScheme.materialSurfaceAtElevation(tonalElevation) else color
    top.yukonga.miuix.kmp.basic.Surface(modifier = modifier, shape = shape,
        color = background, contentColor = contentColor, border = border, shadowElevation = shadowElevation) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Composable
fun Surface(
    onClick: () -> Unit, enabled: Boolean = true,
    modifier: Modifier = Modifier, shape: Shape = androidx.compose.ui.graphics.RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface, contentColor: Color = androidx.compose.material3.contentColorFor(color),
    tonalElevation: Dp = 0.dp, shadowElevation: Dp = 0.dp, border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    val background = if (!isMiuix() && color == MaterialTheme.colorScheme.surface)
        MaterialTheme.colorScheme.materialSurfaceAtElevation(tonalElevation) else color
    top.yukonga.miuix.kmp.basic.Surface(onClick = onClick, enabled = enabled, modifier = modifier, shape = shape,
        color = background, contentColor = contentColor, border = border, shadowElevation = shadowElevation) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Composable
fun Badge(modifier: Modifier = Modifier, containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified, content: @Composable (RowScope.() -> Unit)? = null) {
    if (isMiuix()) {
        val background = if (containerColor == Color.Unspecified) top.yukonga.miuix.kmp.basic.BadgeDefaults.containerColor else containerColor
        val foreground = if (contentColor == Color.Unspecified) top.yukonga.miuix.kmp.basic.BadgeDefaults.contentColor else contentColor
        if (content == null) top.yukonga.miuix.kmp.basic.Badge(modifier, background, foreground)
        else top.yukonga.miuix.kmp.basic.Badge(modifier, background, foreground) {
            val row = this
            CompositionLocalProvider(LocalContentColor provides foreground) { content(row) }
        }
    } else androidx.compose.material3.Badge(modifier,
        containerColor = if (containerColor == Color.Unspecified) androidx.compose.material3.BadgeDefaults.containerColor else containerColor,
        contentColor = if (contentColor == Color.Unspecified) MaterialTheme.colorScheme.onError else contentColor,
        content = content)
}
