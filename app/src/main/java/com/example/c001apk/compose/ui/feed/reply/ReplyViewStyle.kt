package com.example.c001apk.compose.ui.feed.reply

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.example.c001apk.compose.R
import com.example.c001apk.compose.databinding.ActivityReplyBinding
import com.example.c001apk.compose.view.VectorGlyphDrawable
import com.google.android.material.textfield.TextInputLayout
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok

/** The sheet header stays in the XML hierarchy across theme and IME changes. */
internal class ReplyViewStyle(private val binding: ActivityReplyBinding) {
    private val density = binding.root.resources.displayMetrics.density
    private fun dp(value: Int) = (value * density).toInt()
    private val originalMode = binding.textInputLayout.boxBackgroundMode
    private val originalLayoutHint = binding.textInputLayout.hint
    private val originalHintEnabled = binding.textInputLayout.isHintEnabled
    private val originalBackground = binding.editText.background
    private var originalHint = binding.editText.hint
    private var originalCounterColor = binding.textInputLayout.counterTextColor
    private val originalDrawables = binding.checkBox.compoundDrawablesRelative
    private val originalDrawablePadding = binding.checkBox.compoundDrawablePadding
    private val originalEditorPadding = intArrayOf(binding.editText.paddingLeft, binding.editText.paddingTop, binding.editText.paddingRight, binding.editText.paddingBottom)
    private val originalMinLines = binding.editText.minLines
    private val originalTitleGravity = binding.title.gravity
    private val closeIcon = VectorGlyphDrawable(MiuixIcons.Medium.Close, density)
    private val sendIcon = VectorGlyphDrawable(MiuixIcons.Medium.Ok, density)
    private var nativeApplied = false

    fun apply(native: Boolean, palette: ColorScheme?) {
        if (native && palette == null) return
        if (native && !nativeApplied) {
            originalHint = binding.editText.hint
            originalCounterColor = binding.textInputLayout.counterTextColor
        }
        val hasTarget = binding.replyTargetName.visibility == View.VISIBLE
        binding.replyDragArea.visibility = if (native) View.VISIBLE else View.GONE
        binding.replyCancel.visibility = if (native) View.VISIBLE else View.GONE
        binding.replySend.visibility = if (native) View.VISIBLE else View.GONE
        binding.publish.visibility = if (native) View.GONE else View.VISIBLE
        binding.title.visibility = if (native && hasTarget) View.GONE else View.VISIBLE
        binding.title.gravity = if (native) Gravity.CENTER else originalTitleGravity
        binding.title.setPadding(if (native) 0 else dp(16), if (native) 0 else dp(16), if (native) 0 else dp(16), if (native) 0 else dp(16))
        binding.title.layoutParams = (binding.title.layoutParams as LinearLayout.LayoutParams).apply {
            width = if (!native && hasTarget) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            weight = if (!native && hasTarget) 0f else 1f
        }
        binding.replyHeaderRow.minimumHeight = dp(if (native) 48 else 52)
        binding.replyHeaderRow.setPadding(if (native) dp(8) else 0, 0, if (native) dp(8) else 0, 0)
        binding.replyAvatar.layoutParams = (binding.replyAvatar.layoutParams as LinearLayout.LayoutParams).apply {
            width = dp(if (native) 32 else 28); height = width
        }
        if (!native) {
            if (!nativeApplied) return
            binding.textInputLayout.boxBackgroundMode = originalMode
            binding.textInputLayout.isHintEnabled = originalHintEnabled
            binding.textInputLayout.hint = originalLayoutHint
            binding.editText.background = originalBackground
            binding.editText.hint = originalHint
            binding.editText.minLines = originalMinLines
            binding.textInputLayout.setCounterTextColor(originalCounterColor)
            originalEditorPadding.let { binding.editText.setPadding(it[0], it[1], it[2], it[3]) }
            binding.checkBox.setCompoundDrawablesRelativeWithIntrinsicBounds(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3])
            binding.checkBox.compoundDrawablePadding = originalDrawablePadding
            binding.inputLayout.findViewById<View>(R.id.line)?.visibility = View.VISIBLE
            nativeApplied = false
            return
        }
        val colors = checkNotNull(palette)
        nativeApplied = true
        binding.replyCancel.setImageDrawable(closeIcon)
        binding.replyCancel.imageTintList = ColorStateList.valueOf(colors.onSurface.toArgb())
        binding.replySend.setImageDrawable(sendIcon)
        binding.replyDragHandle.background = rounded(colors.onSurfaceVariant.copy(alpha = .35f).toArgb(), 2)
        binding.textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        binding.textInputLayout.isHintEnabled = false
        binding.textInputLayout.setCounterTextColor(ColorStateList.valueOf(colors.onSurfaceVariant.toArgb()))
        binding.editText.apply {
            background = rounded(colors.secondaryContainer.toArgb(), 16)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            hint = if (binding.title.text.contains("发布")) "分享此刻的想法…" else "写回复…"
            minLines = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 4
        }
        val circle = rounded(colors.primary.toArgb(), 24).apply { shape = GradientDrawable.OVAL }
        val check = VectorGlyphDrawable(MiuixIcons.Medium.Ok, density).apply { setTintList(ColorStateList.valueOf(colors.onPrimary.toArgb())) }
        val checked = LayerDrawable(arrayOf(circle, check)).apply { setLayerInset(1, dp(5), dp(5), dp(5), dp(5)) }
        val indicator = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), checked)
            addState(intArrayOf(), rounded(colors.onSurface.copy(alpha = .25f).toArgb(), 24).apply { shape = GradientDrawable.OVAL })
            setEnterFadeDuration(120); setExitFadeDuration(120)
            setBounds(0, 0, dp(24), dp(24))
        }
        binding.checkBox.setCompoundDrawablesRelative(indicator, null, null, null)
        binding.checkBox.compoundDrawablePadding = dp(8)
        binding.inputLayout.findViewById<View>(R.id.line)?.visibility = View.INVISIBLE
        updatePublish(binding.editText.text?.isNotBlank() == true, colors)
    }

    fun updatePublish(enabled: Boolean, palette: ColorScheme) {
        binding.publish.isClickable = enabled
        binding.replySend.isEnabled = enabled
        binding.replySend.imageTintList = ColorStateList.valueOf((if (enabled) palette.primary else palette.onSurfaceVariant.copy(alpha = .38f)).toArgb())
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
}
