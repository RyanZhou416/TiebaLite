package com.huanchengfly.tieba.post.api

import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class CachingDns(private val ttlMs: Long = 10 * 60 * 1000L) : Dns {
    private data class DnsEntry(
        val addresses: List<InetAddress>,
        val expireTime: Long
    )

    private val cache = ConcurrentHashMap<String, DnsEntry>()

    override fun lookup(hostname: String): List<InetAddress> {
        val entry = cache[hostname]
        if (entry != null && System.currentTimeMillis() < entry.expireTime) {
            return entry.addresses
        }
        val addresses = Dns.SYSTEM.lookup(hostname)
        cache[hostname] = DnsEntry(addresses, System.currentTimeMillis() + ttlMs)
        return addresses
    }
}
