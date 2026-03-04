package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatDrawableManager
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import java.util.Calendar

object Util {
    const val TAG = "Util"

    @JvmStatic
    fun createSnackbar(view: View, text: CharSequence, duration: Int): Snackbar {
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.setActionTextColor(ThemeUtils.getColorByAttr(view.context, R.attr.colorAccent))
        val mView = snackbar.view
        val mButton = mView.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
        val mTextView = mView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        mButton.setTextAppearance(view.context, R.style.TextAppearance_Bold)
        if (ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getRawTheme()) {
            mView.backgroundTintList = ColorStateList.valueOf(view.resources.getColor(R.color.white))
            mTextView.setTextColor(view.resources.getColor(R.color.color_text))
        } else {
            mView.backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorByAttr(view.context, R.attr.colorCard))
            mTextView.setTextColor(ThemeUtils.getColorByAttr(view.context, R.attr.colorText))
        }
        mTextView.setTextAppearance(view.context, R.style.TextAppearance_Bold)
        return snackbar
    }

    @JvmStatic
    fun createSnackbar(view: View, @StringRes resId: Int, duration: Int): Snackbar =
        createSnackbar(view, view.resources.getText(resId), duration)

    @JvmStatic
    fun tintBitmap(inBitmap: Bitmap?, tintColor: Int): Bitmap? {
        if (inBitmap == null) return null
        val outBitmap = Bitmap.createBitmap(inBitmap.width, inBitmap.height, inBitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(inBitmap, 0f, 0f, paint)
        return outBitmap
    }

    @JvmStatic
    @SuppressLint("RestrictedApi")
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @JvmStatic
    fun changeAlpha(color: Int, fraction: Float): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val alpha = (Color.alpha(color) * fraction).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    @JvmStatic
    fun findMax(lastVisiblePositions: IntArray): Int {
        var max = lastVisiblePositions[0]
        for (value in lastVisiblePositions) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    @JvmStatic
    fun inflate(context: Context, layoutId: Int): View? {
        if (layoutId <= 0) return null
        return LayoutInflater.from(context).inflate(layoutId, null)
    }

    @JvmStatic
    @ColorInt
    fun getIconColorByLevel(levelStr: String?): Int {
        @ColorInt var color = 0xFFB7BCB6.toInt()
        if (levelStr == null) return color
        when (levelStr) {
            "1", "2", "3" -> color = 0xFF2FBEAB.toInt()
            "4", "5", "6", "7", "8", "9" -> color = 0xFF3AA7E9.toInt()
            "10", "11", "12", "13", "14", "15" -> color = 0xFFFFA126.toInt()
            "16", "17", "18" -> color = 0xFFFF9C19.toInt()
        }
        return ColorUtils.greifyColor(color, 0.2f)
    }

    @JvmStatic
    @ColorInt
    fun getColorByAttr(context: Context, @AttrRes attr: Int, @ColorRes defaultColor: Int): Int {
        val attrs = intArrayOf(attr)
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs)
        val color = typedArray.getColor(0, context.resources.getColor(defaultColor))
        typedArray.recycle()
        return color
    }

    @JvmStatic
    @Dimension
    fun getDimenByAttr(context: Context, @AttrRes attr: Int, @Px defaultDimen: Int): Int {
        val attrs = intArrayOf(attr)
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs)
        val dimensionPixelSize = typedArray.getDimensionPixelSize(0, defaultDimen)
        typedArray.recycle()
        return dimensionPixelSize
    }

    @JvmStatic
    fun getDrawableByAttr(context: Context, @AttrRes attr: Int): Drawable? {
        val attrs = intArrayOf(attr)
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs)
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()
        return drawable
    }

    @JvmStatic
    fun canLoadGlide(context: Context?): Boolean {
        if (context is Activity) {
            return !context.isDestroyed
        }
        return context != null
    }

    @JvmStatic
    fun getTimeInMillis(timeStr: String): Long = time2Calendar(timeStr).timeInMillis

    @JvmStatic
    fun time2Calendar(timeStr: String): Calendar {
        val time = timeStr.split(":")
        var hour = 0
        var minute = 0
        var second = 0
        if (time.size >= 2) {
            hour = time[0].toInt()
            minute = time[1].toInt()
            if (time.size >= 3) {
                second = time[2].toInt()
            }
        }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        return calendar
    }

    @JvmStatic
    @SuppressLint("PrivateApi")
    fun setStatusBarTransparent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
                val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
                field.isAccessible = true
                field.setInt(activity.window.decorView, Color.TRANSPARENT)
            } catch (ignored: Exception) {
            }
        }
    }

    @JvmStatic
    fun alphaColor(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    @JvmStatic
    fun fixTimestampStr(timestampStr: String?): String? {
        if (timestampStr == null) return null
        val builder = StringBuilder(timestampStr)
        while (builder.length < 13) {
            builder.append("0")
        }
        return builder.toString()
    }
}
