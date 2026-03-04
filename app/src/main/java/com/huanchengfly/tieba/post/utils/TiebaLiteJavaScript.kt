package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class TiebaLiteJavaScript(val webView: WebView) {
    val context: Context = webView.context

    @JavascriptInterface
    fun toast(text: String) {
        handler.post {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun getTimeFromNow(time: String): String =
        DateTimeUtils.getRelativeTimeString(context, time)

    @JavascriptInterface
    fun getTheme(): String = ThemeUtil.getRawTheme()

    @JavascriptInterface
    fun copyText(content: String) {
        TiebaUtil.copyText(context, content)
    }

    @JavascriptInterface
    fun putString(key: String, value: String) {
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
            .edit()
            .putString(key, value)
            .apply()
        Log.i(TAG, "putString: $key: $value")
    }

    @JavascriptInterface
    fun getString(key: String): String? =
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
            .getString(key, "")

    @JavascriptInterface
    fun getInt(key: String, defValue: Int): Int =
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
            .getInt(key, defValue)

    @JavascriptInterface
    fun putInt(key: String, value: Int) {
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
            .edit()
            .putInt(key, value)
            .apply()
        Log.i(TAG, "putInt: $key: $value")
    }

    companion object {
        const val TAG = "JsBridge"
        private val handler = Handler(Looper.getMainLooper())
    }
}
