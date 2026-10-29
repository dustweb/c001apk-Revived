package com.example.c001apk.compose.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.c001apk.compose.ui.component.design.FilledTonalButton
import com.example.c001apk.compose.ui.component.design.HorizontalDivider
import com.example.c001apk.compose.ui.component.design.Icon
import com.example.c001apk.compose.ui.component.design.IconButton
import com.example.c001apk.compose.ui.component.design.Scaffold
import com.example.c001apk.compose.ui.component.design.Text
import com.example.c001apk.compose.ui.component.design.TextButton
import com.example.c001apk.compose.ui.component.design.TextField
import com.example.c001apk.compose.ui.component.design.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.util.createRandomNumber
import com.example.c001apk.compose.util.makeToast

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onWebLogin: () -> Unit,
) {

    val viewModel = hiltViewModel<LoginViewModel>()

    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val prefs = LocalUserPreferences.current
    var account by rememberSaveable { mutableStateOf(EMPTY_STRING) }
    var password by rememberSaveable { mutableStateOf(EMPTY_STRING) }
    var captcha by rememberSaveable { mutableStateOf(EMPTY_STRING) }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    fun onLogin() {
        if (account.isNotEmpty() && password.isNotEmpty()) {
            context.makeToast("正在登录...")
            viewModel.loginData["submit"] = "1"
            viewModel.loginData["randomNumber"] = createRandomNumber()
            viewModel.loginData["requestHash"] = viewModel.requestHash
            viewModel.loginData["login"] = account
            viewModel.loginData["password"] = password
            viewModel.loginData["captcha"] = captcha
            viewModel.loginData["code"] = EMPTY_STRING
            viewModel.onLogin()
        } else {
            context.makeToast("用户名或密码为空")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = "登录",
                actions = {
                    TextButton(
                        onClick = onWebLogin,
                        modifier = Modifier.padding(end = 20.dp)
                    ) {
                        Text(text = "网页登录")
                    }
                }
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            if (!com.example.c001apk.compose.ui.theme.isMiuix()) HorizontalDivider()

            TextField(
                value = account,
                onValueChange = { account = it },
                singleLine = true,
                labelText = "账号",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .padding(horizontal = 20.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    AnimatedVisibility(
                        visible = account.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = {
                            account = EMPTY_STRING
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Cancel,
                                contentDescription = "清空输入"
                            )
                        }
                    }
                },
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                labelText = "密码",
                visualTransformation =
                if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onLogin()
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val visibilityIcon =
                            if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = visibilityIcon, contentDescription = if (passwordHidden) "显示密码" else "隐藏密码")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 5.dp)
            )
            AnimatedVisibility(
                visible = viewModel.captchaImg != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(IntrinsicSize.Min)
                        .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(viewModel.captchaImg)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.onGetCaptcha()
                            }
                    )
                    TextField(
                        value = captcha,
                        onValueChange = { captcha = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = captcha.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = {
                                    captcha = EMPTY_STRING
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Cancel,
                                        contentDescription = "清空输入"
                                    )
                                }
                            }
                        },
                    )
                }
            }

            FilledTonalButton(
                enabled = viewModel.requestHash.isNotEmpty(),
                onClick = {
                    onLogin()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 5.dp)
            ) {
                Text(text = "登录")
            }

        }
    }

    if (prefs.isLogin) {
        onBackClick()
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.reset()
    }

}
