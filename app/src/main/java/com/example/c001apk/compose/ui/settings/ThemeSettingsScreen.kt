package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.PreferenceTopBar
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.StateDropdownListItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.component.design.TextButton
import com.example.c001apk.compose.ui.theme.isMiuix
import com.materialkolor.PaletteStyle
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Slider
import kotlin.math.roundToInt

@Composable
fun ThemeSettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs = LocalUserPreferences.current
    val miuix = isMiuix()
    val behavior = MiuixScrollBehavior()
    val scroll = rememberScrollState()
    var customColor by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("theme-settings-page")
            .then(if (miuix) Modifier.nestedScroll(behavior.nestedScrollConnection) else Modifier),
        topBar = { PreferenceTopBar("主题设置", behavior, onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll)) {
            PreferenceGroup("配色") {
                DropdownListItem(value = prefs.themeMode, headlineText = "深色主题",
                    selections = listOf(
                        SelectionItem("跟随系统", ThemeMode.FOLLOW_SYSTEM),
                        SelectionItem("总是开启", ThemeMode.ALWAYS_ON),
                        SelectionItem("总是关闭", ThemeMode.ALWAYS_OFF),
                    ), onValueChanged = { _, value -> viewModel.setDarkTheme(value) })
                SwitchListItem(prefs.materialYou, Icons.Outlined.Palette, "系统主题色",
                    "使用系统壁纸配色", viewModel::setMaterialYou)
                StateDropdownListItem(isEnable = !prefs.materialYou, value = prefs.themeType, headlineText = "主题颜色",
                    selections = ThemeType.entries.filter { it != ThemeType.UNRECOGNIZED }
                        .map { SelectionItem(stringResource(themeTypeLabelRes(it)), it) },
                    onValueChanged = { _, value ->
                        if (value == ThemeType.Custom) customColor = true
                        viewModel.setThemeType(value)
                    })
                StateDropdownListItem(isEnable = !prefs.materialYou, value = prefs.paletteStyle, headlineText = "调色板风格",
                    selections = PaletteStyle.entries.mapIndexed { index, value ->
                        SelectionItem(stringResource(paletteStyleLabelRes(value)), index)
                    }, onValueChanged = { index, _ -> viewModel.setPaletteStyle(index) })
                SwitchListItem(prefs.pureBlack, Icons.Outlined.InvertColors, "纯黑主题", onValueChanged = viewModel::setPureBlack)
            }
            PreferenceGroup("显示") {
                ScalePreference("字体大小", prefs.fontScale, viewModel::setFontScale)
                ScalePreference("界面缩放", prefs.contentScale, viewModel::setContentScale)
            }
            Spacer(Modifier.height(24.dp))
        }
        if (customColor) EditTextDialog(title = "自定义主题颜色", hint = "6650A4", maxLength = 6,
            data = prefs.seedColor, onDismiss = { customColor = false }, setData = viewModel::setSeedColor)
    }
}

@Composable
private fun ScalePreference(title: String, value: Float, onCommit: (Float) -> Unit) {
    var preview by remember(value) { mutableFloatStateOf(value) }
    BasicListItem(headlineText = title, supportingText = "${(preview * 100).roundToInt()}%",
        trailingContent = {
            TextButton(onClick = { preview = 1f; onCommit(1f) }) {
                androidx.compose.material3.Text("重置")
            }
        })
    if (isMiuix()) {
        Slider(value = preview, onValueChange = { preview = it }, valueRange = 0.8f..1.3f,
            onValueChangeFinished = { onCommit(preview) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)
                .testTag("scale-$title"),
            hapticEffect = top.yukonga.miuix.kmp.basic.SliderDefaults.SliderHapticEffect.None)
    } else {
        androidx.compose.material3.Slider(value = preview, onValueChange = { preview = it },
            valueRange = 0.8f..1.3f, onValueChangeFinished = { onCommit(preview) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("scale-$title"))
    }
}
