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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.constraintlayout.widget.ConstraintSet
import com.example.c001apk.compose.R
import com.example.c001apk.compose.databinding.ActivityReplyBinding
import com.example.c001apk.compose.view.VectorGlyphDrawable
import com.google.android.material.textfield.TextInputLayout
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok

/** Keeps the rich-text/IME engine while styling and laying out its native views. */
internal class ReplyViewStyle(private val binding: ActivityReplyBinding) {
    private val density = binding.root.resources.displayMetrics.density
    private fun dp(value: Int) = (value * density).toInt()
    private val headerId = R.id.reply_header
    private val headerViews = listOf(binding.title, binding.replyAvatar, binding.replyTargetName, binding.publish)
    private val originalHeaderParams = headerViews.associateWith {
        ConstraintLayout.LayoutParams(it.layoutParams as ConstraintLayout.LayoutParams)
    }
    private val header = LinearLayout(binding.root.context).apply {
        id = headerId
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(8), dp(16), 0)
    }
    private val heading = LinearLayout(binding.root.context).apply { orientation = LinearLayout.VERTICAL }
    private val target = LinearLayout(binding.root.context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    private val originalConstraints = ConstraintSet().apply {
        clone(binding.inputLayout)
        for (index in 0 until binding.inputLayout.childCount) {
            setVisibilityMode(binding.inputLayout.getChildAt(index).id, ConstraintSet.VISIBILITY_MODE_IGNORE)
        }
    }
    private val originalMode = binding.textInputLayout.boxBackgroundMode
    private val originalLayoutHint = binding.textInputLayout.hint
    private val originalHintEnabled = binding.textInputLayout.isHintEnabled
    private val originalBackground = binding.editText.background
    private val originalHint = binding.editText.hint
    private val originalDrawables = binding.checkBox.compoundDrawablesRelative
    private val originalDrawablePadding = binding.checkBox.compoundDrawablePadding
    private val originalPadding = listOf(binding.title, binding.publish, binding.editText).associateWith {
        intArrayOf(it.paddingLeft, it.paddingTop, it.paddingRight, it.paddingBottom)
    }
    private val originalPublishForeground = binding.publish.foreground
    private val originalMinLines = binding.editText.minLines
    private var nativeApplied = false

    fun apply(native: Boolean, palette: ColorScheme?) {
        if (!native) {
            if (!nativeApplied) return
            headerViews.forEach { (it.parent as? ViewGroup)?.removeView(it) }
            binding.inputLayout.removeView(header)
            headerViews.forEachIndexed { index, view ->
                binding.inputLayout.addView(view, index, originalHeaderParams.getValue(view))
            }
            originalConstraints.applyTo(binding.inputLayout)
            originalPadding.forEach { (view, p) -> view.setPadding(p[0], p[1], p[2], p[3]) }
            binding.title.minHeight = 0
            binding.publish.minWidth = 0
            binding.publish.minHeight = 0
            binding.publish.foreground = originalPublishForeground
            binding.publish.isEnabled = true
            binding.textInputLayout.boxBackgroundMode = originalMode
            binding.textInputLayout.isHintEnabled = originalHintEnabled
            binding.textInputLayout.hint = originalLayoutHint
            binding.editText.background = originalBackground
            binding.editText.hint = originalHint
            binding.editText.minLines = originalMinLines
            binding.checkBox.setCompoundDrawablesRelativeWithIntrinsicBounds(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3])
            binding.checkBox.compoundDrawablePadding = originalDrawablePadding
            binding.inputLayout.findViewById<View>(R.id.line)?.visibility = View.VISIBLE
            nativeApplied = false
            return
        }
        if (palette == null) return
        if (!nativeApplied) {
            headerViews.forEach { (it.parent as? ViewGroup)?.removeView(it) }
            header.removeAllViews()
            heading.removeAllViews()
            target.removeAllViews()
            heading.addView(binding.title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            target.addView(binding.replyAvatar, LinearLayout.LayoutParams(dp(28), dp(28)).apply { marginEnd = dp(8) })
            target.addView(binding.replyTargetName, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            heading.addView(target, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            header.addView(heading, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(12) })
            header.addView(binding.publish, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            binding.inputLayout.addView(header, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
            })
        }
        nativeApplied = true
        val hasTarget = binding.replyTargetName.visibility == View.VISIBLE || binding.replyAvatar.visibility == View.VISIBLE
        target.visibility = if (hasTarget) View.VISIBLE else View.GONE
        binding.title.visibility = View.VISIBLE
        binding.publish.visibility = View.VISIBLE
        binding.textInputLayout.layoutParams = ConstraintLayout.LayoutParams(binding.textInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
            width = 0
            startToStart = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
            topToTop = ConstraintSet.UNSET
            topToBottom = headerId
            marginStart = dp(16)
            marginEnd = dp(16)
            topMargin = dp(8)
        }
        binding.title.apply { setPadding(0, 0, 0, 0); minHeight = if (hasTarget) 0 else dp(48); gravity = Gravity.START or Gravity.CENTER_VERTICAL; maxLines = 1 }
        binding.publish.apply { setPadding(dp(20), dp(10), dp(20), dp(10)); minWidth = dp(80); minHeight = dp(48); gravity = Gravity.CENTER; setSingleLine(true); foreground = null }
        binding.textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        binding.textInputLayout.isHintEnabled = false
        binding.textInputLayout.setCounterTextColor(ColorStateList.valueOf(palette.onSurfaceVariant.toArgb()))
        binding.editText.apply {
            background = rounded(palette.secondaryContainer.toArgb(), 16)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            hint = if (binding.title.text.contains("发布")) "分享此刻的想法…" else "写回复…"
            minLines = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 4
        }
        val checkedCircle = rounded(palette.primary.toArgb(), 24).apply { shape = GradientDrawable.OVAL }
        val check = VectorGlyphDrawable(MiuixIcons.Medium.Ok, density).apply { setTintList(ColorStateList.valueOf(palette.onPrimary.toArgb())) }
        val checked = LayerDrawable(arrayOf(checkedCircle, check)).apply { setLayerInset(1, dp(5), dp(5), dp(5), dp(5)) }
        val indicator = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), checked)
            addState(intArrayOf(), rounded(palette.onSurface.copy(alpha = .25f).toArgb(), 24).apply { shape = GradientDrawable.OVAL })
            setEnterFadeDuration(120)
            setExitFadeDuration(120)
            setBounds(0, 0, dp(24), dp(24))
        }
        binding.checkBox.setCompoundDrawablesRelative(indicator, null, null, null)
        binding.checkBox.compoundDrawablePadding = dp(8)
        binding.inputLayout.findViewById<View>(R.id.line)?.visibility = View.INVISIBLE
        updatePublish(binding.editText.text?.isNotBlank() == true, palette)
        binding.inputLayout.requestLayout()
    }

    fun updatePublish(enabled: Boolean, palette: ColorScheme) {
        binding.publish.isEnabled = enabled
        binding.publish.isClickable = enabled
        binding.publish.background = rounded((if (enabled) palette.primary else palette.surfaceContainerHigh).toArgb(), 16)
        binding.publish.setTextColor((if (enabled) palette.onPrimary else palette.onSurfaceVariant).toArgb())
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
}
