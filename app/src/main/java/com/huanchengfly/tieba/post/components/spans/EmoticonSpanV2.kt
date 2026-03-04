package com.huanchengfly.tieba.post.components.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class EmoticonSpanV2(drawable: Drawable, private val size: Int) : ImageSpan(drawable, ALIGN_BASELINE) {

    override fun getDrawable(): Drawable {
        val d = super.getDrawable()
        d.setBounds(0, 0, size, size)
        return d
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            val fontFm = paint.fontMetricsInt
            fm.ascent = fontFm.top
            fm.descent = fontFm.bottom
            fm.top = fm.ascent
            fm.bottom = fm.descent
        }
        return size
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val fm = paint.fontMetricsInt
        val d = getDrawable()
        val transY = y - d.bounds.bottom + fm.descent
        canvas.save()
        canvas.translate(x, transY.toFloat())
        d.draw(canvas)
        canvas.restore()
    }

    companion object {
        @JvmField
        val TAG: String = EmoticonSpanV2::class.java.simpleName
    }
}
