package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable")
class TintConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Tintable, BackgroundTintable {

    private var mBackgroundTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (background == null) {
                setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
            } else {
                backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mBackgroundTintResId))
            }
        }
    }

    override fun getBackgroundTintResId(): Int = mBackgroundTintResId

    override fun setBackgroundTintResId(resId: Int) {
        mBackgroundTintResId = resId
        tint()
    }
}
