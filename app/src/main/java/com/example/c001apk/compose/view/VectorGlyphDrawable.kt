package com.example.c001apk.compose.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath

/** Uses the same monochrome icon geometry in native editor toolbars and Compose controls. */
class VectorGlyphDrawable(private val vector: ImageVector) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paths = mutableMapOf<VectorPath, android.graphics.Path>()
    private var drawableAlpha = 255
    private var tint: ColorStateList? = null

    override fun draw(canvas: Canvas) {
        val checkpoint = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / vector.viewportWidth, bounds.height() / vector.viewportHeight)
        drawGroup(canvas, vector.root)
        canvas.restoreToCount(checkpoint)
    }

    private fun drawGroup(canvas: Canvas, group: VectorGroup) {
        val checkpoint = canvas.save()
        canvas.translate(group.translationX + group.pivotX, group.translationY + group.pivotY)
        canvas.rotate(group.rotation)
        canvas.scale(group.scaleX, group.scaleY)
        canvas.translate(-group.pivotX, -group.pivotY)
        if (group.clipPathData.isNotEmpty()) {
            canvas.clipPath(PathParser().addPathNodes(group.clipPathData).toPath().asAndroidPath())
        }
        for (node in group) when (node) {
            is VectorGroup -> drawGroup(canvas, node)
            is VectorPath -> {
                val path = paths.getOrPut(node) {
                    PathParser().addPathNodes(node.pathData).toPath().apply {
                        fillType = node.pathFillType
                    }.asAndroidPath()
                }
                if (node.fill != null) {
                    paint.style = Paint.Style.FILL
                    paint.alpha = (drawableAlpha * node.fillAlpha).toInt()
                    canvas.drawPath(path, paint)
                }
                if (node.stroke != null) {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = node.strokeLineWidth
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.strokeJoin = Paint.Join.ROUND
                    paint.alpha = (drawableAlpha * node.strokeAlpha).toInt()
                    canvas.drawPath(path, paint)
                }
            }
        }
        canvas.restoreToCount(checkpoint)
    }

    override fun setAlpha(alpha: Int) { drawableAlpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    override fun setTintList(tint: ColorStateList?) {
        this.tint = tint
        onStateChange(state)
    }
    override fun isStateful() = tint?.isStateful == true
    override fun onStateChange(state: IntArray): Boolean {
        paint.colorFilter = tint?.let {
            PorterDuffColorFilter(it.getColorForState(state, it.defaultColor), PorterDuff.Mode.SRC_IN)
        }
        invalidateSelf()
        return true
    }
    @Deprecated("Deprecated in Android")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun getIntrinsicWidth() = vector.defaultWidth.value.toInt()
    override fun getIntrinsicHeight() = vector.defaultHeight.value.toInt()
}
