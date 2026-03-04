package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.utils.appPreferences
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class SettingsProxySelector : ProxySelector() {
    override fun select(uri: URI): List<Proxy> {
        if (!App.isInitialized) return listOf(Proxy.NO_PROXY)
        val prefs = App.INSTANCE.appPreferences
        val host = prefs.proxyHost
        val port = prefs.proxyPort?.toIntOrNull() ?: 0
        if (!prefs.proxyEnabled || host.isNullOrBlank() || port <= 0) {
            return listOf(Proxy.NO_PROXY)
        }
        val type = when (prefs.proxyType) {
            "socks" -> Proxy.Type.SOCKS
            else -> Proxy.Type.HTTP
        }
        return listOf(Proxy(type, InetSocketAddress(host, port)))
    }

    override fun connectFailed(uri: URI, sa: SocketAddress, ioe: IOException) {}
}
