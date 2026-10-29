package com.example.c001apk.compose.ui.feed.reply

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.constraintlayout.widget.Barrier
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
    private val barrierId = View.generateViewId()
    private val headerId = View.generateViewId()
    private val originalTitleParams = ConstraintLayout.LayoutParams(binding.title.layoutParams as ConstraintLayout.LayoutParams)
    private val originalPublishParams = ConstraintLayout.LayoutParams(binding.publish.layoutParams as ConstraintLayout.LayoutParams)
    private val header = LinearLayout(binding.root.context).apply {
        id = headerId
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), 0, dp(16), 0)
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
            header.removeAllViews()
            binding.inputLayout.removeView(header)
            binding.inputLayout.findViewById<View>(barrierId)?.let(binding.inputLayout::removeView)
            binding.inputLayout.addView(binding.title, 0, originalTitleParams)
            binding.inputLayout.addView(binding.publish, 3, originalPublishParams)
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
            binding.inputLayout.removeView(binding.title)
            binding.inputLayout.removeView(binding.publish)
            header.addView(binding.title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(12) })
            header.addView(binding.publish, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            binding.inputLayout.addView(header, ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT))
        }
        nativeApplied = true
        val constraints = ConstraintSet().apply {
            clone(binding.inputLayout)
            constrainWidth(headerId, ConstraintLayout.LayoutParams.MATCH_PARENT)
            constrainHeight(headerId, ConstraintSet.WRAP_CONTENT)
            connect(headerId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(headerId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(headerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dp(12))
            connect(R.id.replyAvatar, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp(16))
            connect(R.id.replyAvatar, ConstraintSet.TOP, headerId, ConstraintSet.BOTTOM, dp(8))
            connect(R.id.replyTargetName, ConstraintSet.START, R.id.replyAvatar, ConstraintSet.END, dp(8))
            connect(R.id.replyTargetName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(16))
            connect(R.id.replyTargetName, ConstraintSet.TOP, R.id.replyAvatar, ConstraintSet.TOP)
            connect(R.id.replyTargetName, ConstraintSet.BOTTOM, R.id.replyAvatar, ConstraintSet.BOTTOM)
            createBarrier(barrierId, Barrier.BOTTOM, 0, headerId, R.id.replyAvatar, R.id.replyTargetName)
            constrainWidth(R.id.textInputLayout, 0)
            connect(R.id.textInputLayout, ConstraintSet.TOP, barrierId, ConstraintSet.BOTTOM, dp(12))
            connect(R.id.textInputLayout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp(16))
            connect(R.id.textInputLayout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(16))
        }
        constraints.applyTo(binding.inputLayout)
        binding.inputLayout.findViewById<Barrier>(barrierId).setAllowsGoneWidget(false)
        binding.title.apply { setPadding(0, 0, 0, 0); minHeight = dp(48); gravity = Gravity.START or Gravity.CENTER_VERTICAL; maxLines = 1 }
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
