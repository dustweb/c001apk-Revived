package com.example.c001apk.compose.ui.login

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.design.Button
import com.example.c001apk.compose.ui.component.design.Icon
import com.example.c001apk.compose.ui.component.design.IconButton
import com.example.c001apk.compose.ui.component.design.TextButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixLoginForm(
    account: String, onAccount: (String) -> Unit,
    password: String, onPassword: (String) -> Unit,
    passwordHidden: Boolean, onTogglePassword: () -> Unit,
    captcha: String, onCaptcha: (String) -> Unit, captchaImage: Bitmap?, onRefreshCaptcha: () -> Unit,
    ready: Boolean, loggingIn: Boolean, onLogin: () -> Unit, onWebLogin: () -> Unit, onBack: () -> Unit,
) {
    val behavior = MiuixScrollBehavior()
    val focus = LocalFocusManager.current
    val canLogin = ready && !loggingIn && account.isNotBlank() && password.isNotEmpty()
    fun submit() { if (canLogin) { focus.clearFocus(); onLogin() } }
    Scaffold(topBar = { TopAppBar(title = "登录", navigationIcon = { BackButton(onBack) }, scrollBehavior = behavior) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            Column(Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxWidth()
                .nestedScroll(behavior.nestedScrollConnection).verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("登录酷安账号", style = MiuixTheme.textStyles.title4)
                Text("使用账号密码登录，或选择网页登录。", style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.height(4.dp))
                TextField(value = account, onValueChange = onAccount, label = "手机号 / 邮箱 / 用户名",
                    useLabelAsPlaceholder = true, singleLine = true, enabled = !loggingIn,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    trailingIcon = {
                        if (account.isNotEmpty()) IconButton(onClick = { onAccount("") }, enabled = !loggingIn) {
                            Icon(Icons.Rounded.Cancel, "清空账号")
                        }
                    })
                TextField(value = password, onValueChange = onPassword, label = "密码", useLabelAsPlaceholder = true,
                    singleLine = true, enabled = !loggingIn,
                    visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = { IconButton(onClick = onTogglePassword, enabled = !loggingIn) {
                        Icon(if (passwordHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            if (passwordHidden) "显示密码" else "隐藏密码")
                    } })
                if (captchaImage != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextField(value = captcha, onValueChange = onCaptcha, label = "图形验证码", useLabelAsPlaceholder = true,
                            singleLine = true, modifier = Modifier.weight(1f), enabled = !loggingIn)
                        AsyncImage(model = captchaImage, contentDescription = "刷新验证码",
                            modifier = Modifier.size(width = 112.dp, height = 52.dp).clickable(enabled = !loggingIn, onClick = onRefreshCaptcha))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { submit() }, enabled = canLogin, modifier = Modifier.fillMaxWidth()) {
                    com.example.c001apk.compose.ui.component.design.Text(if (loggingIn) "正在登录…" else "登录")
                }
                TextButton(onClick = onWebLogin, enabled = !loggingIn, modifier = Modifier.fillMaxWidth()) {
                    com.example.c001apk.compose.ui.component.design.Text("网页登录")
                }
            }
        }
    }
}
