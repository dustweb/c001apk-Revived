package com.example.c001apk.compose.ui.component.design

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.c001apk.compose.ui.theme.isMiuix

@Composable
fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val miuix = isMiuix()
    // Keep the pending action in one queue across live style changes.
    androidx.compose.material3.SnackbarHost(hostState, modifier) { data ->
        if (miuix) {
            val nativeData = remember(data) {
                object : top.yukonga.miuix.kmp.basic.SnackbarData {
                    override val visuals = top.yukonga.miuix.kmp.basic.SnackbarVisuals(
                        data.visuals.message, data.visuals.actionLabel, data.visuals.withDismissAction,
                        when (data.visuals.duration) {
                            androidx.compose.material3.SnackbarDuration.Short -> top.yukonga.miuix.kmp.basic.SnackbarDuration.Short
                            androidx.compose.material3.SnackbarDuration.Long -> top.yukonga.miuix.kmp.basic.SnackbarDuration.Long
                            androidx.compose.material3.SnackbarDuration.Indefinite -> top.yukonga.miuix.kmp.basic.SnackbarDuration.Indefinite
                        },
                    )
                    override suspend fun dismiss() = data.dismiss()
                    override suspend fun performAction() = data.performAction()
                }
            }
            top.yukonga.miuix.kmp.basic.Snackbar(nativeData)
        } else androidx.compose.material3.Snackbar(data)
    }
}
