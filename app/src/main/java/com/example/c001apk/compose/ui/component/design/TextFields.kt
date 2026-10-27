package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colors: TextFieldColors? = null,
) {
    if (isMiuix()) {
        FieldDecoration(modifier, value.isEmpty(), label, placeholder, leadingIcon != null) {
            top.yukonga.miuix.kmp.basic.TextField(
                value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle.copy(color = MiuixTheme.colorScheme.onSurface),
                backgroundColor = colors?.unfocusedContainerColor ?: MiuixTheme.colorScheme.secondaryContainer,
                leadingIcon = leadingIcon, trailingIcon = trailingIcon, singleLine = singleLine,
                maxLines = maxLines, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
            )
        }
    } else {
        androidx.compose.material3.TextField(value, onValueChange, modifier, textStyle = textStyle,
            label = label, placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            singleLine = singleLine, maxLines = maxLines, keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions, visualTransformation = visualTransformation,
            colors = colors ?: androidx.compose.material3.TextFieldDefaults.colors())
    }
}

@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colors: TextFieldColors? = null,
) {
    if (isMiuix()) {
        FieldDecoration(modifier, value.text.isEmpty(), label, placeholder, leadingIcon != null) {
            top.yukonga.miuix.kmp.basic.TextField(
                value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle.copy(color = MiuixTheme.colorScheme.onSurface),
                backgroundColor = colors?.unfocusedContainerColor ?: MiuixTheme.colorScheme.secondaryContainer,
                leadingIcon = leadingIcon, trailingIcon = trailingIcon, singleLine = singleLine,
                maxLines = maxLines, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
            )
        }
    } else {
        androidx.compose.material3.TextField(value, onValueChange, modifier, textStyle = textStyle,
            label = label, placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            singleLine = singleLine, maxLines = maxLines, keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions, visualTransformation = visualTransformation,
            colors = colors ?: androidx.compose.material3.TextFieldDefaults.colors())
    }
}

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
) {
    if (isMiuix()) {
        TextField(value, onValueChange, modifier, placeholder = placeholder,
            leadingIcon = leadingIcon, singleLine = singleLine)
    } else {
        androidx.compose.material3.OutlinedTextField(value, onValueChange, modifier,
            placeholder = placeholder, leadingIcon = leadingIcon, singleLine = singleLine)
    }
}

@Composable
fun OutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
) {
    if (isMiuix()) {
        TextField(value, onValueChange, modifier, placeholder = placeholder,
            leadingIcon = leadingIcon, singleLine = singleLine)
    } else {
        androidx.compose.material3.OutlinedTextField(value, onValueChange, modifier,
            placeholder = placeholder, leadingIcon = leadingIcon, singleLine = singleLine)
    }
}

@Composable
private fun FieldDecoration(
    modifier: Modifier,
    empty: Boolean,
    label: (@Composable () -> Unit)?,
    placeholder: (@Composable () -> Unit)?,
    hasLeadingIcon: Boolean,
    field: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        label?.invoke()
        Box {
            field()
            if (empty && placeholder != null) {
                Box(Modifier.align(Alignment.CenterStart)
                    .padding(start = if (hasLeadingIcon) 52.dp else 16.dp, end = 16.dp)) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        placeholder()
                    }
                }
            }
        }
    }
}
