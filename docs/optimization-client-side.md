# TiebaLite 客户端侧性能优化方案

> 本文档中的所有优化均为**纯客户端侧改动**，不需要任何新的抓包数据或协议变更。
> 使用现有的 API 接口即可实现，适用于所有用户（尤其是海外高延迟用户）。

---

## 1. Repository 层 Stale-While-Revalidate 缓存

**影响**: 高 | **难度**: 中

### 现状问题

- `PbPageRepository` 完全没有缓存，每次都发起网络请求
- `FrsPageRepository` 只有单条内存缓存（`lastHash`/`lastResponse`），切换吧就失效
- `PersonalizedRepository` 完全没有缓存

### 优化方案

为所有核心 Repository 实现 "先展示缓存、后台刷新" 策略：

```kotlin
// 示例：FrsPageRepository 改造
object FrsPageRepository {
    private val cache = object : LinkedHashMap<String, FrsPageResponse>(
        MAX_SIZE + 1, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FrsPageResponse>?) =
            size > MAX_SIZE
    }

    fun frsPage(...): Flow<FrsPageResponse> = flow {
        val hash = "${forumName}_${page}_${sortType}_${goodClassifyId}"

        // 1. 立刻 emit 缓存数据（如果有），UI 秒开
        synchronized(cache) { cache[hash] }?.let { emit(it) }

        // 2. 始终发起网络请求获取最新数据
        val fresh = TiebaApi.getInstance().frsPage(...)
            .map { /* 数据转换 */ }
            .firstOrNull()

        if (fresh != null) {
            synchronized(cache) { cache[hash] = fresh }
            emit(fresh)  // 更新 UI
        }
    }
}
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/repository/FrsPageRepository.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/repository/PbPageRepository.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/repository/PersonalizedRepository.kt`
- 对应的 ViewModel 需要处理 Flow 可能 emit 两次的情况（缓存 + 新数据）

---

## 2. 预取策略增强

**影响**: 高 | **难度**: 低

### 现状问题

- `ThreadDetailPrefetchManager` 最大并发只有 2
- `get()` 会立即删除缓存（`cache.remove`），用户返回再进同一帖子无缓存
- 只预取后方 3 个帖子

### 优化方案

```kotlin
object ThreadDetailPrefetchManager {
    private const val MAX_CACHE_SIZE = 20      // 15 → 20
    private const val MAX_CONCURRENCY = 3      // 2 → 3
    private const val CACHE_TTL_MS = 5 * 60 * 1000L  // 缓存 5 分钟过期

    // 改为带时间戳的缓存
    private data class CacheEntry(
        val response: PbPageResponse,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired() = System.currentTimeMillis() - timestamp > CACHE_TTL_MS
    }

    // get() 改为不删除，读取时检查过期
    fun get(threadId: Long): PbPageResponse? {
        synchronized(cache) {
            val entry = cache[threadId] ?: return null
            if (entry.isExpired()) {
                cache.remove(threadId)
                return null
            }
            return entry.response  // 不再 remove
        }
    }
}
```

在列表页增大预取范围：

```kotlin
// ForumThreadListPage.kt / PersonalizedPage.kt
// 当前：预取后 3 个 (lastVisibleIndex + 1..+4)
// 改为：预取后 5 个 (lastVisibleIndex + 1..+6)
val prefetchRange = (lastVisibleIndex + 1).coerceAtMost(threadList.size)
    .until((lastVisibleIndex + 6).coerceAtMost(threadList.size))
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/repository/ThreadDetailPrefetchManager.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/personalized/PersonalizedPage.kt`

---

## 3. OkHttp 磁盘缓存

**影响**: 中 | **难度**: 低

### 现状问题

所有 OkHttpClient 均未配置 `Cache`，无法利用 HTTP 层的缓存机制。

### 优化方案

在 `RetrofitTiebaApi.kt` 中添加 OkHttp Cache：

```kotlin
object RetrofitTiebaApi {
    private val httpCache by lazy {
        Cache(
            File(App.INSTANCE.cacheDir, "http_cache"),
            50L * 1024 * 1024  // 50MB
        )
    }

    private inline fun <reified T : Any> createJsonApi(...) =
        Retrofit.Builder()
            // ...
            .client(OkHttpClient.Builder().apply {
                cache(httpCache)  // 添加缓存
                // ... 其他配置
            }.build())
            .build()
            .create(T::class.java)
}
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 4. DNS 缓存

**影响**: 中高（海外用户尤为显著）| **难度**: 低

### 现状问题

每次 DNS 查询都走系统 DNS，海外用户解析百度域名可能需要 100-200ms。

### 优化方案

实现 OkHttp 自定义 DNS 并缓存结果：

```kotlin
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

// 使用
OkHttpClient.Builder().apply {
    dns(CachingDns())
    // ...
}
```

> 参考：aiotieba 使用了 `dns_ttl=600`（10 分钟的 DNS 缓存），效果良好。

### 需要修改的文件

- 新建 `app/src/main/java/com/huanchengfly/tieba/post/api/CachingDns.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 5. 连接预热 + 保活优化

**影响**: 中（海外用户影响高）| **难度**: 低

### 现状问题

冷启动时首次请求需要完成 DNS + TCP + TLS 握手，海外用户可达 800-1500ms。

### 优化方案

在 Application 启动阶段预热连接：

```kotlin
// App.kt 中添加
private fun prewarmConnections() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val hosts = listOf("tiebac.baidu.com", "c.tieba.baidu.com")
    hosts.forEach { host ->
        scope.launch {
            runCatching {
                val request = Request.Builder()
                    .url("https://$host/")
                    .head()
                    .build()
                sharedOkHttpClient.newCall(request).execute().close()
            }
        }
    }
}
```

同时延长连接池保活时间：

```kotlin
// 当前：ConnectionPool(32, 5, TimeUnit.MINUTES)
// 建议：
private val connectionPool = ConnectionPool(32, 10, TimeUnit.MINUTES)
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/App.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 6. 缩短超时时间

**影响**: 中 | **难度**: 低

### 现状问题

连接/读取/写入超时均为 60 秒，导致弱网下错误反馈极慢。

### 优化方案

```kotlin
// 当前
private const val READ_TIMEOUT = 60L
private const val CONNECT_TIMEOUT = 60L
private const val WRITE_TIMEOUT = 60L

// 建议（参考 aiotieba 使用 connect=3s, read=12s）
// 考虑到海外移动网络场景适当放宽
private const val CONNECT_TIMEOUT = 15L
private const val READ_TIMEOUT = 30L
private const val WRITE_TIMEOUT = 20L
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 7. 启用 gzip 压缩

**影响**: 中 | **难度**: 低

### 现状问题

当前未显式设置 `Accept-Encoding` 请求头。OkHttp 默认会添加，但自定义 Header 可能覆盖了此行为。

### 优化方案

参考 aiotieba 的做法，显式设置 `Accept-Encoding: gzip`：

```kotlin
// 在 CommonHeaderInterceptor 中添加
Header.ACCEPT_ENCODING to { "gzip" }
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 8. 从列表页传递预览数据到详情页

**影响**: 中 | **难度**: 中

### 现状问题

打开帖子详情时，`ThreadUiIntent.Init` 只传递了 `ThreadInfo`（标题、作者），用户看到的是完全空白的加载中状态。

### 优化方案

在列表页就提前传递更多数据（首楼摘要、回复数、图片缩略图），让详情页可以立刻展示骨架内容：

1. 列表项 → 详情页导航时，把列表页已有的 ThreadInfo 完整传递
2. 在 `ThreadPartialChange.Init.Success` 的 `reduce` 中利用这些数据构建初始 UI
3. 等网络数据到达后无缝替换

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadViewModel.kt`

---

## 9. 数据转换优化

**影响**: 低中 | **难度**: 低

### 现状问题

`PbPageRepository.pbPage()` 中 `userList.first { user -> user.id == it.author_id }` 是 O(n) 线性查找，在大量回复时产生不必要的开销。

### 优化方案

```kotlin
// 当前：O(n) 线性查找
val author = userList.first { user -> user.id == it.author_id }

// 优化：O(1) HashMap 查找
val userMap = response.data_.user_list.associateBy { it.id }
val author = userMap[it.author_id] ?: it.author
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/repository/PbPageRepository.kt`

---

## 10. 增大每页请求数据量

**影响**: 中（高延迟网络下效果明显）| **难度**: 低

### 现状问题

帖子详情每页只请求 15 条回复（`rn=15`），每条回复只加载 4 条楼中楼（`floor_rn=4`）。高延迟网络下频繁翻页体验差。

### 优化方案

```kotlin
// MixedTiebaApiImpl.kt 中 pbPageFlow 的参数
// 当前
rn = 15,
floor_rn = 4,

// 建议
rn = 30,
floor_rn = 6,
```

可以考虑做成用户可配置选项，让海外用户可以选择更大的分页。

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt`

---

## 11. 代理支持

**影响**: 高（海外用户）| **难度**: 中

### 现状问题

无法配置 HTTP 代理或 SOCKS 代理。海外用户如果有低延迟到中国的通道无法利用。

### 优化方案

在设置页面添加代理配置选项，并在 OkHttpClient 中应用：

```kotlin
OkHttpClient.Builder().apply {
    if (proxyEnabled) {
        proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort)))
    }
}
```

> 参考：aiotieba 原生支持代理配置（`ProxyConfig`），通过 `proxy=True` 自动读取环境变量。

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/` 相关设置页面

---

## 实施优先级

| 顺序 | 优化项 | 预期收益 | 工作量 |
|------|--------|---------|--------|
| 1 | Repository Stale-While-Revalidate 缓存 | 列表页/首页秒开 | 中 |
| 2 | 预取策略增强 | 帖子详情秒开率大幅提升 | 低 |
| 3 | DNS 缓存 | 海外用户每次请求减少 100-200ms | 低 |
| 4 | 连接预热 + 保活 | 冷启动首屏加速 500-1000ms | 低 |
| 5 | 缩短超时时间 | 弱网下更快的错误反馈 | 低 |
| 6 | 数据转换优化 | 减少 CPU/GC 开销 | 低 |
| 7 | 增大分页 rn | 减少翻页网络请求次数 | 低 |
| 8 | gzip 压缩 | 减少传输数据量 | 低 |
| 9 | 从列表传预览数据 | 感知秒开 | 中 |
| 10 | OkHttp 磁盘缓存 | 重复请求加速 | 低 |
| 11 | 代理支持 | 海外用户 RTT 可降至 100ms 以内 | 中 |
