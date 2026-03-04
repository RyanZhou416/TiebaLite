package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringDef
import com.huanchengfly.tieba.post.App

object SharedPreferencesUtil {
    const val SP_APP_DATA = "appData"
    const val SP_DRAFT = "draft"
    const val SP_SETTINGS = "settings"
    const val SP_PERMISSION = "permission"
    const val SP_IGNORE_VERSIONS = "ignore_version"
    const val SP_WEBVIEW_INFO = "webview_info"
    const val SP_PLUGINS = "plugins"

    @JvmStatic
    fun get(@Preferences name: String): SharedPreferences = get(App.INSTANCE, name)

    @JvmStatic
    fun get(context: Context, @Preferences name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    @JvmStatic
    fun put(sharedPreferences: SharedPreferences, key: String, value: String): Boolean =
        sharedPreferences.edit().putString(key, value).commit()

    @JvmStatic
    fun put(sharedPreferences: SharedPreferences, key: String, value: Boolean): Boolean =
        sharedPreferences.edit().putBoolean(key, value).commit()

    @JvmStatic
    fun put(sharedPreferences: SharedPreferences, key: String, value: Int): Boolean =
        sharedPreferences.edit().putInt(key, value).commit()

    @JvmStatic
    fun put(context: Context, @Preferences preference: String, key: String, value: String): Boolean =
        put(get(context, preference), key, value)

    @JvmStatic
    fun put(context: Context, @Preferences preference: String, key: String, value: Boolean): Boolean =
        put(get(context, preference), key, value)

    @JvmStatic
    fun put(context: Context, @Preferences preference: String, key: String, value: Int): Boolean =
        put(get(context, preference), key, value)

    @StringDef(SP_APP_DATA, SP_IGNORE_VERSIONS, SP_PERMISSION, SP_WEBVIEW_INFO, SP_DRAFT, SP_PLUGINS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Preferences
}
