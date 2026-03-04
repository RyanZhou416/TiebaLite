package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.MenuPopupWindow
import androidx.appcompat.widget.PopupMenu
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("RestrictedApi")
object PopupUtil {
    @JvmStatic
    fun replaceBackground(listPopupWindow: ListPopupWindow) {
        try {
            val contextField = ListPopupWindow::class.java.getDeclaredField("mContext")
            contextField.isAccessible = true
            val context = contextField.get(listPopupWindow) as? Context ?: return
            val currentTheme = ThemeUtil.getCurrentTheme()
            when {
                currentTheme == ThemeUtil.THEME_TRANSLUCENT_LIGHT -> {
                    listPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            AppCompatResources.getDrawable(context, R.drawable.bg_popup),
                            context.resources.getColor(R.color.theme_color_background_light)
                        )
                    )
                }
                currentTheme == ThemeUtil.THEME_TRANSLUCENT_DARK -> {
                    listPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            AppCompatResources.getDrawable(context, R.drawable.bg_popup),
                            context.resources.getColor(R.color.theme_color_background_dark)
                        )
                    )
                }
                else -> {
                    listPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            AppCompatResources.getDrawable(context, R.drawable.bg_popup),
                            ThemeUtils.getColorByAttr(context, R.attr.colorCard)
                        )
                    )
                }
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    private fun replaceBackground(popupMenu: PopupMenu) {
        try {
            val contextField = PopupMenu::class.java.getDeclaredField("mContext")
            contextField.isAccessible = true
            val context = contextField.get(popupMenu) as? Context ?: return
            val field = PopupMenu::class.java.getDeclaredField("mPopup")
            field.isAccessible = true
            val menuPopupHelper = field.get(popupMenu) as? MenuPopupHelper ?: return
            val obj = menuPopupHelper.popup
            val popupField = obj.javaClass.getDeclaredField("mPopup")
            popupField.isAccessible = true
            val menuPopupWindow = popupField.get(obj) as? MenuPopupWindow ?: return
            Log.i("Theme", ThemeUtil.getCurrentTheme())
            val currentTheme = ThemeUtil.getCurrentTheme()
            when {
                currentTheme == ThemeUtil.THEME_TRANSLUCENT_LIGHT -> {
                    menuPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            context.getDrawable(R.drawable.bg_popup),
                            context.resources.getColor(R.color.theme_color_background_light)
                        )
                    )
                }
                currentTheme == ThemeUtil.THEME_TRANSLUCENT_DARK -> {
                    menuPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            context.getDrawable(R.drawable.bg_popup),
                            context.resources.getColor(R.color.theme_color_background_dark)
                        )
                    )
                }
                else -> {
                    menuPopupWindow.setBackgroundDrawable(
                        ThemeUtils.tintDrawable(
                            context.getDrawable(R.drawable.bg_popup),
                            ThemeUtils.getColorByAttr(context, R.attr.colorCard)
                        )
                    )
                }
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun create(anchor: View): PopupMenu {
        val popupMenu = PopupMenu(anchor.context, anchor)
        replaceBackground(popupMenu)
        return popupMenu
    }
}
