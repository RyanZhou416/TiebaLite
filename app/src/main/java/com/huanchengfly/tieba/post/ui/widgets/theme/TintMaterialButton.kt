package com.huanchengfly.tieba.post.ui.widgets.theme

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils

class TintMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int
    private var mTextColorResId: Int
    private var mStrokeColorResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mTextColorResId = 0
            mStrokeColorResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mTextColorResId = 0
            mStrokeColorResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintMaterialButton, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintMaterialButton_buttonBackgroundTint, 0)
            mTextColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonTextColor, 0)
            mStrokeColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonStrokeColor, 0)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(context, mTextColorResId))
        }
        if (mBackgroundTintResId != 0) {
            backgroundTintList = ColorStateListUtils.createColorStateList(context, mBackgroundTintResId)
        }
        if (mStrokeColorResId != 0) {
            strokeColor = ColorStateListUtils.createColorStateList(context, mStrokeColorResId)
        }
    }

    fun setTextColorResId(textColorResId: Int) {
        mTextColorResId = textColorResId
        applyTintColor()
    }

    fun setBackgroundTintResId(backgroundTintResId: Int) {
        mBackgroundTintResId = backgroundTintResId
        applyTintColor()
    }
}
