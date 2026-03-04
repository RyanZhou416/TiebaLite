# TiebaLite 协议层升级优化方案

> 本文档中的优化需要参考 [aiotieba](https://github.com/lumina37/aiotieba) 项目的最新协议数据。
> aiotieba 是目前最活跃的贴吧第三方接口库（Python，1654 commits，v4.6.1），持续跟进官方客户端的协议变更。

---

## 关键发现：aiotieba 与 TiebaLite 的协议差异

通过对比 aiotieba 的源码，发现以下重要差异：

| 对比项 | TiebaLite 当前 | aiotieba 最新 |
|--------|---------------|--------------|
| **客户端版本号** | `11.10.8.6` / `12.52.1.0` | `12.64.1.1` |
| **JSON API 基地址** | `http://c.tieba.baidu.com/` (HTTP) | 不使用，已废弃 |
| **Protobuf API 基地址** | `https://tiebac.baidu.com/` | `tiebac.baidu.com` (统一使用) |
| **Accept-Encoding** | 未设置 | `gzip` (显式设置) |
| **Multipart Boundary** | `--------7da3d81520810*` | `-*_r1999` |
| **连接超时** | 60 秒 | 3 秒 |
| **读取超时** | 60 秒 | 12 秒 |
| **DNS 缓存** | 无 | 600 秒 TTL |
| **代理支持** | 无 | 原生支持 |
| **WebSocket** | 无 | 支持 |
| **User-Agent** | `bdtb for Android 12.25.1.0` 等 | `aiotieba/{version}` |

> 来源：[aiotieba/const.py](https://github.com/lumina37/aiotieba/blob/master/aiotieba/const.py)
> — `MAIN_VERSION = "12.64.1.1"`, `APP_BASE_HOST = "tiebac.baidu.com"`

---

## 1. 统一迁移到 `tiebac.baidu.com` (HTTPS)

**影响**: 高 | **难度**: 中

### 现状问题

TiebaLite 使用了多个 API 域名，其中 3 个走 HTTP 明文：

```
http://c.tieba.baidu.com/   ← NEW_TIEBA_API, MINI_TIEBA_API, OFFICIAL_TIEBA_API
https://tiebac.baidu.com/   ← OFFICIAL_PROTOBUF_TIEBA_API (已用HTTPS)
https://tieba.baidu.com/    ← WEB_TIEBA_API, HYBRID_TIEBA_API
```

HTTP/1.1 无法使用 HTTP/2 多路复用，海外用户每个请求都要建独立 TCP 连接。

### aiotieba 的做法

aiotieba 已经将所有 APP 接口统一到 `tiebac.baidu.com`：

```python
# aiotieba/const.py
APP_BASE_HOST = "tiebac.baidu.com"
WEB_BASE_HOST = "tieba.baidu.com"
```

所有移动端 API（不论 JSON 还是 Protobuf）都走 `tiebac.baidu.com`。

### 优化方案

将 `RetrofitTiebaApi.kt` 中使用 `http://c.tieba.baidu.com/` 的接口迁移到 `https://tiebac.baidu.com/`：

```kotlin
// 修改前
val NEW_TIEBA_API: NewTiebaApi by lazy {
    createJsonApi<NewTiebaApi>(
        "http://c.tieba.baidu.com/",  // HTTP
        // ...
    )
}

// 修改后
val NEW_TIEBA_API: NewTiebaApi by lazy {
    createJsonApi<NewTiebaApi>(
        "https://tiebac.baidu.com/",  // HTTPS → HTTP/2
        // ...
    )
}
```

需要逐个验证以下 API 实例是否在新域名下正常工作：

- [ ] `NEW_TIEBA_API` — `http://c.tieba.baidu.com/` → `https://tiebac.baidu.com/`
- [ ] `MINI_TIEBA_API` — `http://c.tieba.baidu.com/` → `https://tiebac.baidu.com/`
- [ ] `OFFICIAL_TIEBA_API` — `http://c.tieba.baidu.com/` → `https://tiebac.baidu.com/`

**收益**：统一后所有请求共享 HTTP/2 连接，一个 TCP 连接可以并行处理多个请求。

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 2. 升级客户端版本号

**影响**: 中 | **难度**: 中

### 现状问题

TiebaLite 模拟的客户端版本较旧：

```kotlin
enum class ClientVersion(val version: String) {
    TIEBA_V11("11.10.8.6"),    // 2020年10月
    TIEBA_V12("12.52.1.0"),    // 2024年
    TIEBA_V12_POST("12.35.1.0"); // 2023年1月
}
```

服务端可能根据客户端版本进行差异化处理（限制功能、返回不同数据格式等）。

### aiotieba 的版本

```python
# aiotieba/const.py
MAIN_VERSION = "12.64.1.1"
```

比 TiebaLite 的 V12 (`12.52.1.0`) 新了约 12 个小版本。

### 优化方案

参考 aiotieba 升级版本号，并同步更新相关参数：

```kotlin
enum class ClientVersion(val version: String) {
    TIEBA_V11("11.10.8.6"),       // 保留兼容
    TIEBA_V12("12.64.1.1"),       // 升级：12.52.1.0 → 12.64.1.1
    TIEBA_V12_POST("12.64.1.1");  // 升级：12.35.1.0 → 12.64.1.1
}
```

**注意事项**：

- 版本号升级后，需检查所有 Protobuf 消息格式是否兼容
- 可以参考 [n0099/tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) 对比新旧版本的 proto 差异
- 建议先在非关键接口上测试，再逐步推广
- 同时更新 `User-Agent` 中的版本号

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/Enums.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`（UA 更新）
- `app/src/main/java/com/huanchengfly/tieba/post/api/ProtobufRequest.kt`（CommonRequest 参数）

---

## 3. 更新 Multipart Boundary

**影响**: 低 | **难度**: 低

### 现状问题

```kotlin
// TiebaLite
const val BOUNDARY = "--------7da3d81520810*"
```

### aiotieba 的值

```python
# aiotieba/core/http.py → pack_proto_request
boundary="-*_r1999"
```

### 优化方案

更新 boundary 以匹配更新版本的官方协议：

```kotlin
const val BOUNDARY = "-*_r1999"
```

这虽然功能上没有区别，但可以让请求更贴近官方客户端的指纹，减少被服务端识别为异常请求的风险。

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/ProtobufRequest.kt`

---

## 4. 更新 Protobuf 定义

**影响**: 中 | **难度**: 高

### 现状问题

TiebaLite 使用的 Protobuf 消息定义来自较早版本的官方客户端。新版本可能新增了字段，这些字段可能包含优化数据加载的信息。

### 参考资源

1. **aiotieba 的 protobuf 目录**：
   [aiotieba/api/_protobuf/](https://github.com/lumina37/aiotieba/tree/master/aiotieba/api/_protobuf)
   包含了最新的 protobuf 序列化逻辑

2. **tbclient.protobuf 仓库**：
   [n0099/tbclient.protobuf](https://github.com/n0099/tbclient.protobuf)
   从 244 个版本中提取的完整 proto 定义（最新到 12.51.7.1）

### 优化方案

1. 从 aiotieba 的 `_protobuf` 目录中提取最新的消息结构
2. 对比 TiebaLite 当前使用的 Wire proto 定义
3. 添加新增的有用字段（比如可能新增的缓存控制字段、数据压缩标记等）
4. 使用 Wire 编译器重新生成 Kotlin 类

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/models/protos/` 下的所有 proto 定义

---

## 5. 将 JSON 接口迁移到 Protobuf

**影响**: 中 | **难度**: 中高

### 现状问题

部分 API 仍然使用 JSON 格式（通过 `OFFICIAL_TIEBA_API`、`NEW_TIEBA_API` 等调用），这些接口：
- 数据体积大于 Protobuf 2-5 倍
- 解析速度慢于 Protobuf 5-10 倍
- 使用 HTTP/1.1（`http://c.tieba.baidu.com`）

### aiotieba 的做法

aiotieba 对核心接口全部使用 Protobuf 序列化。即使是表单请求，也在参数中使用了优化的编码。

### 优化方案

逐步将仍使用 JSON 的接口迁移到 Protobuf 版本（如果官方服务端支持）：

1. 检查 `OfficialProtobufTiebaApi.kt` 中是否已有对应的 Protobuf 端点
2. 在 `MixedTiebaApiImpl.kt` 中将调用从 JSON API 切换到 Protobuf API
3. 更新对应的数据解析逻辑

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt`
- 对应的 Retrofit 接口定义文件

---

## 6. 添加 WebSocket 支持（长期）

**影响**: 中 | **难度**: 高

### 现状

TiebaLite 没有 WebSocket 支持。所有数据获取都依赖 HTTP 轮询。

### aiotieba 的做法

aiotieba 实现了完整的 WebSocket 接口：

```python
# aiotieba/core/__init__.py
from .websocket import TypeWebsocketCallback, WsCore, WsResponse

# aiotieba/config.py
ws_send: float = 3.0
ws_read: float = 8.0
ws_keepalive: float = 300.0
ws_heartbeat: float | None = None
```

### 潜在收益

- 实时消息推送（回复通知等）
- 减少轮询请求数量
- 持久连接复用

### 建议

这是一个长期优化项目。可以参考 aiotieba 的 `WsCore` 实现来了解协议细节，但实现复杂度较高。

---

## 7. 对齐 aiotieba 的请求头配置

**影响**: 低中 | **难度**: 低

### 现状差异

aiotieba 在请求头中做了以下优化：

```python
# aiotieba/core/http.py
app_headers = {
    "User-Agent": f"aiotieba/{__version__}",
    "Accept-Encoding": "gzip",          # 显式启用 gzip
    "Connection": "keep-alive",          # 显式保持长连接
    "Host": APP_BASE_HOST,               # 显式设置 Host
}

app_proto_headers = {
    "User-Agent": f"aiotieba/{__version__}",
    "x_bd_data_type": "protobuf",        # Protobuf 标识
    "Accept-Encoding": "gzip",
    "Connection": "keep-alive",
    "Host": APP_BASE_HOST,
}
```

注意 aiotieba 使用了自定义的 `User-Agent: aiotieba/xxx`，而不是伪装官方客户端。这意味着服务端对 User-Agent 并不做严格校验。

### 优化方案

精简 TiebaLite 的请求头，确保以下关键头字段存在：

```kotlin
CommonHeaderInterceptor(
    Header.ACCEPT_ENCODING to { "gzip" },
    Header.CONNECTION to { "keep-alive" },
    Header.HOST to { "tiebac.baidu.com" },
    // ...
)
```

### 需要修改的文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

---

## 实施优先级

| 顺序 | 优化项 | 预期收益 | 风险 | 依赖 |
|------|--------|---------|------|------|
| 1 | 统一迁移到 `tiebac.baidu.com` | HTTP/2 多路复用 | 中（需逐个测试） | 无 |
| 2 | 升级客户端版本号到 `12.64.1.1` | 更好的服务端兼容性 | 中（可能有参数变化） | 无 |
| 3 | 更新 Multipart Boundary | 请求指纹更新 | 低 | 无 |
| 4 | 对齐请求头（gzip 等） | 传输优化 | 低 | 无 |
| 5 | 将 JSON 接口迁移到 Protobuf | 数据量和解析速度优化 | 中 | 可能需要 #2 |
| 6 | 更新 Protobuf 定义 | 访问新字段/功能 | 高 | 需要 #2 |
| 7 | WebSocket 支持 | 实时推送 | 高 | 需要深入研究 |

---

## 参考资源

- [aiotieba GitHub](https://github.com/lumina37/aiotieba) — 最新的贴吧协议实现
- [aiotieba 文档](https://aiotieba.cc/) — API 参考
- [tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) — 历史版本 Protobuf 定义
- [TiebaLite 友情链接](https://github.com/lumina37/aiotieba#友情链接) — TiebaLite 被列为 aiotieba 的友情项目
