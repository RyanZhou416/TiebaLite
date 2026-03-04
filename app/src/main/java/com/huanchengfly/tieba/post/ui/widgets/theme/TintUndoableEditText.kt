package com.huanchengfly.tieba.post.ui.widgets.theme

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.widgets.edittext.widget.UndoableEditText

class TintUndoableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : UndoableEditText(context, attrs, defStyleAttr), Tintable {

    private var mTextColorResId: Int
    private var mHintTextColorResId: Int
    private var mCursorColorResId: Int

    init {
        if (isInEditMode) {
            mTextColorResId = 0
            mHintTextColorResId = 0
            mCursorColorResId = 0
        } else if (attrs == null) {
            mTextColorResId = 0
            mHintTextColorResId = 0
            mCursorColorResId = 0
            tint()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintUndoableEditText, defStyleAttr, 0)
            mTextColorResId = array.getResourceId(R.styleable.TintUndoableEditText_textColor, 0)
            mHintTextColorResId = array.getResourceId(R.styleable.TintUndoableEditText_hintTextColor, 0)
            mCursorColorResId = array.getResourceId(R.styleable.TintUndoableEditText_cursorColor, 0)
            array.recycle()
            tint()
        }
    }

    override fun tint() {
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(context, mTextColorResId))
        }
        if (mHintTextColorResId != 0) {
            setHintTextColor(ColorStateListUtils.createColorStateList(context, mHintTextColorResId))
        }
        if (mCursorColorResId != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val drawable = textCursorDrawable ?: return
                textCursorDrawable = ThemeUtils.tintDrawable(
                    drawable,
                    ColorStateListUtils.createColorStateList(context, mCursorColorResId)
                        ?: return
                )
            }
        }
    }

    fun setTextColorResId(textColorResId: Int) {
        mTextColorResId = textColorResId
        tint()
    }

    fun setHintTextColorResId(hintTextColorResId: Int) {
        mHintTextColorResId = hintTextColorResId
        tint()
    }
}
