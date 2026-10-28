package com.example.c001apk.compose.ui.component.design

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.ui.theme.isMiuix
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DialogPreferencesEntryPoint {
    fun preferences(): UserPreferencesRepository
}

data class DialogAction(val label: String, val primary: Boolean = false, val onClick: () -> Unit = {})

/** Hosts themed Compose content for the WebView, gallery and native rich-text editor. */
fun showAppDialog(
    context: Context,
    title: String,
    message: String? = null,
    actions: List<DialogAction> = listOf(DialogAction("确定", primary = true)),
    choices: List<DialogAction> = emptyList(),
    cancelable: Boolean = true,
    content: (@Composable () -> Unit)? = null,
): DialogInterface {
    var current = context
    while (current is ContextWrapper && current !is Activity) current = current.baseContext
    val activity = current as? Activity ?: error("A dialog requires an Activity context")
    val root = activity.window.decorView as ViewGroup
    val host = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        tag = "themed-platform-dialog"
    }
    val repository = EntryPointAccessors.fromApplication(context.applicationContext,
        DialogPreferencesEntryPoint::class.java).preferences()
    val visible = mutableStateOf(true)
    var nextAction: (() -> Unit)? = null
    var finished = false
    val handler = Handler(Looper.getMainLooper())
    val finish = Runnable {
        if (!finished) {
            finished = true
            root.removeView(host)
            host.disposeComposition()
            nextAction?.invoke()
        }
    }
    val handle = object : DialogInterface {
        override fun cancel() = dismiss()
        override fun dismiss() {
            visible.value = false
            handler.postDelayed(finish, 750)
        }
    }
    host.setContent {
        val prefs by repository.data.collectAsStateWithLifecycle(initialValue = null)
        prefs?.let { preferences ->
            CompositionLocalProvider(LocalUserPreferences provides preferences) {
                C001apkComposeTheme(darkTheme = preferences.isDarkMode(), themeType = preferences.themeType,
                    seedColor = preferences.seedColor, materialYou = preferences.materialYou,
                    pureBlack = preferences.pureBlack, paletteStyle = preferences.paletteStyle,
                    fontScale = preferences.fontScale, contentScale = preferences.contentScale,
                    uiStyle = preferences.uiStyle, applySystemBars = false) {
                    val close = { handle.dismiss() }
                    val body: @Composable () -> Unit = {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                                message?.let { Text(it) }
                                content?.invoke()
                                choices.forEach { choice ->
                                    val select = { nextAction = choice.onClick; handle.dismiss() }
                                    if (isMiuix()) top.yukonga.miuix.kmp.basic.BasicComponent(
                                        title = choice.label, onClick = select)
                                    else TextButton(onClick = select, modifier = Modifier.fillMaxWidth()) {
                                        Text(choice.label)
                                    }
                                }
                            }
                            val buttons = actions.map { action ->
                                val button: @Composable () -> Unit = {
                                    StyleButton(primary = action.primary, onClick = {
                                        nextAction = action.onClick
                                        handle.dismiss()
                                    }) { Text(action.label) }
                                }
                                button
                            }
                            if (isMiuix()) DialogActions(buttons)
                            else FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                buttons.forEach { it() }
                            }
                        }
                    }
                    if (isMiuix()) {
                        top.yukonga.miuix.kmp.window.WindowDialog(show = visible.value, title = title,
                            onDismissRequest = if (cancelable) close else null,
                            onDismissFinished = { handler.removeCallbacks(finish); finish.run() }, content = body)
                    } else if (visible.value) {
                        androidx.compose.material3.AlertDialog(onDismissRequest = { if (cancelable) close() },
                            title = { Text(title) }, text = body, confirmButton = {},
                            properties = androidx.compose.ui.window.DialogProperties(
                                dismissOnBackPress = cancelable, dismissOnClickOutside = cancelable))
                    } else LaunchedEffect(Unit) { handler.removeCallbacks(finish); finish.run() }
                }
            }
        }
    }
    root.addView(host, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    return handle
}
