package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.example.c001apk.compose.ui.component.design.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.c001apk.compose.ui.component.design.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.logic.state.State

/**
 * Created by bggRGjQaUbCoE on 2024/6/5
 */

enum class Type {
    TEXT,
    INDICATOR,
    NONE
}

@Composable
fun LoadingCard(
    modifier: Modifier = Modifier,
    state: State,
    onClick: (() -> Unit)? = null,
    isFeed: Boolean = false
) {

    if (com.example.c001apk.compose.ui.theme.isMiuix() && (state == FooterState.End || state == LoadingState.Empty)) {
        top.yukonga.miuix.kmp.basic.Text(
            if (state == FooterState.End) "已显示全部内容" else "暂无内容",
            modifier.fillMaxWidth().padding(vertical = 20.dp), textAlign = TextAlign.Center,
            style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1,
            color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary)
        return
    }
    val type: Pair<Type, String?> = when (state) {
        FooterState.End -> Pair(Type.TEXT, stringResource(id = R.string.loading_end))
        is FooterState.Error -> Pair(Type.TEXT, state.errMsg)
        FooterState.Loading -> Pair(Type.INDICATOR, null)
        FooterState.Success -> Pair(Type.NONE, null)
        LoadingState.Empty -> Pair(Type.TEXT, stringResource(id = R.string.loading_empty))
        is LoadingState.Error -> Pair(Type.TEXT, state.errMsg)
        LoadingState.Loading -> Pair(Type.INDICATOR, null)
        is LoadingState.Success<*> -> Pair(Type.NONE, null)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (type.first == Type.NONE) 0.dp else 80.dp)
            .clip(
                if (isFeed) RectangleShape
                else MaterialTheme.shapes.medium
            )
            .clickable(
                enabled = onClick != null,
                onClick = onClick ?: {}
            )
    ) {
        when (type.first) {
            Type.TEXT -> Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                text = type.second ?: stringResource(id = R.string.loading_empty),
                textAlign = TextAlign.Center,
                color = com.example.c001apk.compose.ui.component.design.secondaryTextColor(),
            )

            Type.INDICATOR -> androidx.compose.animation.AnimatedVisibility(
                visible = type.first == Type.INDICATOR,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(400)),
                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round
                )
            }

            Type.NONE -> {}
        }
    }

}
