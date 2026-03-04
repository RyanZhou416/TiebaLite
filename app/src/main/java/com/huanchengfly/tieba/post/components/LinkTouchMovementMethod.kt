package com.huanchengfly.tieba.post.components

import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

class LinkTouchMovementMethod private constructor() : LinkMovementMethod() {

    private var pressedSpan: ClickableSpan? = null

    override fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpan = getPressedSpan(textView, spannable, event)
                pressedSpan?.let {
                    Selection.setSelection(spannable, spannable.getSpanStart(it), spannable.getSpanEnd(it))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                if (pressedSpan != null && touchedSpan !== pressedSpan) {
                    pressedSpan = null
                    Selection.removeSelection(spannable)
                }
            }
            else -> {
                if (pressedSpan != null) {
                    super.onTouchEvent(textView, spannable, event)
                }
                pressedSpan = null
                Selection.removeSelection(spannable)
            }
        }
        return pressedSpan != null
    }

    /**
     * Copy from:
     * http://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
     * By:
     * Steven Meliopoulos
     */
    private fun getPressedSpan(textView: TextView, spannable: Spannable, event: MotionEvent): ClickableSpan? {
        var x = event.x.toInt()
        var y = event.y.toInt()

        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop

        x += textView.scrollX
        y += textView.scrollY

        val layout: Layout = textView.layout ?: return null
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())

        val link = spannable.getSpans(off, off, ClickableSpan::class.java)
        return if (link.isNotEmpty()) link[0] else null
    }

    fun isPressedSpan(): Boolean = pressedSpan != null

    companion object {
        @JvmStatic
        val instance: LinkTouchMovementMethod by lazy { LinkTouchMovementMethod() }
    }
}
