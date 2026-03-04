package com.huanchengfly.tieba.post.ui.widgets.theme

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.widget.CompoundButtonCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils

class TintCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.checkboxStyle
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    private var mBackgroundTintResId: Int
    private var mTextColorResId: Int
    private var mButtonTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mButtonTintResId = 0
            mTextColorResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mButtonTintResId = 0
            mTextColorResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintCheckBox, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintCheckBox_checkboxBackgroundTint, 0)
            mButtonTintResId = array.getResourceId(R.styleable.TintCheckBox_buttonTint, 0)
            mTextColorResId = array.getResourceId(R.styleable.TintCheckBox_textColor, 0)
            array.recycle()
            applyTintColor()
        }
    }

    private fun applyTintColor() {
        if (mBackgroundTintResId != 0) {
            backgroundTintList = ColorStateListUtils.createColorStateList(context, mBackgroundTintResId)
        }
        if (mButtonTintResId != 0) {
            CompoundButtonCompat.setButtonTintList(this, ColorStateListUtils.createColorStateList(context, mButtonTintResId))
        }
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(context, mTextColorResId))
        }
    }
}
