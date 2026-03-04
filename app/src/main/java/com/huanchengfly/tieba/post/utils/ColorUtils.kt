package com.huanchengfly.tieba.post.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

object ColorUtils {
    @JvmStatic
    @JvmOverloads
    fun getDarkerColor(@ColorInt color: Int, i: Float = 0.1f): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] + i
        hsv[2] = hsv[2] - i
        return Color.HSVToColor(hsv)
    }

    @JvmStatic
    fun setLuminance(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) luminance: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = luminance
        return Color.HSVToColor(hsv)
    }

    @JvmStatic
    @ColorInt
    fun alpha(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    @JvmStatic
    @JvmOverloads
    fun getLighterColor(@ColorInt color: Int, i: Float = 0.1f): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] - i
        hsv[2] = hsv[2] + i
        return Color.HSVToColor(hsv)
    }

    @JvmStatic
    fun greifyColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) sat: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] - sat
        hsv[2] = hsv[2] - (sat / 3)
        return Color.HSVToColor(hsv)
    }
}
