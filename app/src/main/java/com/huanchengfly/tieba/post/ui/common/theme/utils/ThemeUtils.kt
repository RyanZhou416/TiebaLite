package com.huanchengfly.tieba.post.ui.common.theme.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.WrapperListAdapter
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.ExtraRefreshable
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.ThemeSwitcher
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import java.lang.reflect.Field
import java.lang.reflect.Method

object ThemeUtils {

    private var themeSwitcher: ThemeSwitcher? = null
    private var sRecycler: Field? = null
    private var sRecycleViewClearMethod: Method? = null
    private var sRecyclerBin: Field? = null
    private var sListViewClearMethod: Method? = null

    @JvmStatic
    fun tintDrawable(drawable: Drawable?, colorStateList: ColorStateList): Drawable? {
        if (drawable == null) return null
        val wrapper = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTintList(wrapper, colorStateList)
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
        return drawable
    }

    @JvmStatic
    fun tintDrawable(drawable: Drawable?, @ColorInt color: Int): Drawable? {
        if (drawable == null) return null
        val wrapper = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTint(wrapper, color)
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
        return drawable
    }

    @JvmStatic
    fun init(themeSwitcher: ThemeSwitcher) {
        this.themeSwitcher = themeSwitcher
    }

    @JvmStatic
    @ColorInt
    fun getColorByAttr(context: Context, @AttrRes attrId: Int): Int {
        val switcher = themeSwitcher
            ?: throw IllegalStateException("ThemeSwitcher is uninitialized.")
        return switcher.getColorByAttr(context, attrId)
    }

    @JvmStatic
    @ColorInt
    fun getColorById(context: Context, @ColorRes colorId: Int): Int {
        val switcher = themeSwitcher
            ?: throw IllegalStateException("ThemeSwitcher is uninitialized.")
        return switcher.getColorById(context, colorId)
    }

    @JvmStatic
    fun refreshUI(context: Context) {
        refreshUI(context, null)
    }

    @JvmStatic
    fun getWrapperActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getWrapperActivity(context.baseContext)
            else -> null
        }
    }

    @JvmStatic
    fun refreshUI(context: Context, extraRefreshable: ExtraRefreshable?) {
        val activity = getWrapperActivity(context) ?: return
        extraRefreshable?.refreshGlobal(activity)
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        refreshView(rootView, extraRefreshable)
    }

    @SuppressLint("PrivateApi")
    private fun refreshView(view: View?, extraRefreshable: ExtraRefreshable?) {
        if (view == null) return
        view.destroyDrawingCache()
        if (view is Tintable) {
            view.tint()
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    refreshView(view.getChildAt(i), extraRefreshable)
                }
            }
        } else {
            extraRefreshable?.refreshSpecificView(view)
            if (view is AbsListView) {
                try {
                    if (sRecyclerBin == null) {
                        sRecyclerBin = AbsListView::class.java.getDeclaredField("mRecycler").apply {
                            isAccessible = true
                        }
                    }
                    if (sListViewClearMethod == null) {
                        sListViewClearMethod = Class.forName("android.widget.AbsListView\$RecycleBin")
                            .getDeclaredMethod("clear").apply {
                                isAccessible = true
                            }
                    }
                    sListViewClearMethod?.invoke(sRecyclerBin?.get(view))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                var adapter = view.adapter
                while (adapter is WrapperListAdapter) {
                    adapter = adapter.wrappedAdapter
                }
                if (adapter is BaseAdapter) {
                    adapter.notifyDataSetChanged()
                }
            }
            if (view is RecyclerView) {
                try {
                    sRecycler = RecyclerView::class.java.getDeclaredField("mRecycler").apply {
                        isAccessible = true
                    }
                    sRecycleViewClearMethod =
                        Class.forName("androidx.recyclerview.widget.RecyclerView\$Recycler")
                            .getDeclaredMethod("clear").apply {
                                isAccessible = true
                            }
                    sRecycleViewClearMethod?.invoke(sRecycler?.get(view))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.recycledViewPool.clear()
                for (i in 0 until view.itemDecorationCount) {
                    val itemDecoration = view.getItemDecorationAt(i)
                    if (itemDecoration is Tintable) {
                        itemDecoration.tint()
                    }
                }
                view.invalidateItemDecorations()
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    refreshView(view.getChildAt(i), extraRefreshable)
                }
            }
        }
    }
}
