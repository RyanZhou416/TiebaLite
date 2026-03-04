package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AlertDialogLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil

@SuppressLint("RestrictedApi")
class TintAlertDialogLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AlertDialogLayout(context, attrs), Tintable {

    private var mBackgroundTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = R.color.default_color_background
        } else if (attrs == null) {
            mBackgroundTintResId = R.color.default_color_background
            applyTintColor()
        } else {
            @SuppressLint("CustomViewStyleable")
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, 0, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.default_color_background)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (background == null) {
            setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
        } else {
            background = ThemeUtils.tintDrawable(background, ThemeUtils.getColorById(context, mBackgroundTintResId))
        }
        ThemeUtil.setTranslucentDialogBackground(this)
    }
}
