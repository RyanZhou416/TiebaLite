# TiebaLite 协议层升级优化方案（状态化）

> 更新时间：2026-03-05  
> 本文档基于当前代码实现核对，不再只描述“提案”，而是区分 **已落地 / 部分落地 / 待落地**。  
> 协议参考仍以 [aiotieba](https://github.com/lumina37/aiotieba) 与 [tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) 为主。

---

## 0. 当前状态总览

| 项目 | 当前状态 | 说明 |
|------|----------|------|
| APP 域名统一到 `https://tiebac.baidu.com/` | 已落地 | `NEW/MINI/OFFICIAL/PROTO` 已迁移 |
| `Accept-Encoding: gzip` | 已落地 | 已在通用 Header 拦截器注入 |
| DNS 缓存 | 已落地 | `CachingDns(ttl=10min)` 已接入 |
| 连接预热 | 已落地 | `App.onCreate()` 已调用预热 |
| 代理支持 | 已落地 | `SettingsProxySelector` + 设置页已可配置 |
| 超时参数优化 | 已落地 | `connect=15s/read=30s/write=20s` |
| 客户端版本升级到 `12.64.1.1` | 已落地 | `V11`/`V12`/`V12_POST` 均已统一为 `12.64.1.1`（含发帖/回帖/上传） |
| Multipart Boundary 升级 | 已落地 | 已统一到 `BOUNDARY` 常量，值升级为 `-*_r1999` |
| JSON -> Protobuf 迁移 | 部分落地 | 21 个端点已走 Protobuf（含 ReplyMe 新迁移）；cmd 全量审计完成 |
| Protobuf 定义跟新版本对齐 | 部分落地 | Phase 1+2：高频消息 + 共享类型已对齐，PbPage 74-79 新类型已补齐 |
| WebSocket 支持 | 未落地（长期） | 目前仍以 HTTP 拉取为主 |

---

## 1. 已落地项（建议仅保留回归验证）

### 1.1 域名与传输通道

**状态：已落地**

- `NEW_TIEBA_API`、`MINI_TIEBA_API`、`OFFICIAL_TIEBA_API`、`OFFICIAL_PROTOBUF_*` 均已使用 `https://tiebac.baidu.com/`
- Web/Hybrid 仍保留 `https://tieba.baidu.com/`（这是不同业务域，不属于回退）

**代码位置**

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

### 1.2 请求头基础优化（gzip）

**状态：已落地**

- 已在通用请求头中显式设置 `Accept-Encoding: gzip`

**代码位置**

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`

### 1.3 超时、DNS 缓存、连接池与预热

**状态：已落地**

- 超时：`connect=15s`、`read=30s`、`write=20s`
- DNS：`CachingDns`（TTL 10 分钟）
- 连接池保活：`ConnectionPool(32, 10min)`
- 启动预热：`App.onCreate()` 中调用 `RetrofitTiebaApi.prewarmConnections(appScope)`

**代码位置**

- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/CachingDns.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/App.kt`

### 1.4 代理支持

**状态：已落地**

- 运行时代理：`SettingsProxySelector`（支持 HTTP/SOCKS）
- 设置项：`proxy_enabled`、`proxy_type`、`proxy_host`、`proxy_port`
- UI 入口：设置页“更多设置”

**代码位置**

- `app/src/main/java/com/huanchengfly/tieba/post/api/SettingsProxySelector.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/MoreSettingsPage.kt`

---

## 2. 部分落地项（继续推进）

## 2.1 JSON 接口迁移到 Protobuf

**状态：部分落地**

当前核心流量接口已大量使用 Protobuf（如 `frsPage`、`threadList`、`pbPage`、`pbFloor`、`profile`、`personalized` 等），但仍有不少 JSON 端点保留（例如部分 `NEW_TIEBA_API` / `OFFICIAL_TIEBA_API` / `WEB_TIEBA_API` 场景）。

**建议下一步**

1. 做一份“JSON 端点台账”（按调用量和页面入口排序）
2. 逐项确认是否存在可替代的 Protobuf 接口
3. 从高频接口开始迁移，并保留灰度开关

### 已完成（Phase 2）

审计了 8 个同时存在 JSON 和 Protobuf 实现的端点，全部核心路径已走 Protobuf：

- `personalized`、`frsPage`、`pbPage`、`pbFloor`、`profile`、`userPost`、`addPost` — 均已通过 Protobuf 路径调用
- `forumRecommend` — OKSigner 已从 JSON `forumRecommendFlow()` 迁移到 Protobuf `forumRecommendNewFlow()`
- 遗留 JSON 方法已在 `ITiebaApi.kt` 中标记 `@Deprecated`
- `QuickPreviewUtil` 中已删除无调用者的旧回调方法

### JSON 端点台账（Phase 3 审计）

全量审计结果：已迁移 **21** 个 | JSON-only 有 Proto 定义可迁移 17 个 | JSON-only 无 Proto 定义 26 个 | 总计 67 个

### 已完成（Phase 4 — JSON → Protobuf 迁移）

- `/c/u/feed/replyme`（cmd=303007）→ `replyMeProtoFlow()` — 完整端到端迁移
  - 新建 5 个 proto 文件（`ReplyMe/` 目录）
  - 在 `OfficialProtobufTiebaApi.kt` 添加 protobuf Retrofit 方法
  - 在 `ITiebaApi.kt` 添加 `replyMeProtoFlow()` 接口
  - `MixedTiebaApiImpl.kt` 实现 protobuf 请求构建
  - `NotificationsListViewModel.kt` 已切换到 protobuf 路径（含 `ReplyList` → `MessageInfoBean` 数据映射）
  - 旧 JSON 方法 `replyMe()`/`replyMeAsync()`/`replyMeFlow()` 标记 `@Deprecated`

### 已完成（Phase 5 — GetDislikeList 迁移并与 tbclient 对齐）

- `/c/u/user/getDislikeList`（cmd=309692）→ `getDislikeListFlow(page, rn)` — 端到端迁移
  - 以本地克隆的 [tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) 与 [aiotieba](https://github.com/lumina37/aiotieba) 为参考
  - 新增 [ForumList.proto](app/src/main/protos/ForumList.proto)，与 tbclient `ForumList` 一致（forum_id、forum_name、avatar、member_count、slogan、content、post_num、thread_num）
  - Request 与 tbclient DataReq 对齐：`common`、`pn`（int32）、`rn`（int32，默认 20）
  - Response 与 tbclient DataRes 对齐：`forum_list`（repeated ForumList）、`has_more`、`cur_page`
  - [OfficialProtobufTiebaApi.kt](app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/OfficialProtobufTiebaApi.kt)、[ITiebaApi](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt)、[MixedTiebaApiImpl.kt](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt) 已接好；暂无 UI 调用，接口层可复用

### 已完成（Phase 6 — GetForumSquare 迁移并与 tbclient 对齐）

- `/c/f/forum/getForumSquare`（cmd=309653）→ `getForumSquareFlow(className, page, rn)` — 端到端迁移
  - 以 tbclient.protobuf GetForumSquare DataReq/DataRes 与 aiotieba get_square_forums 为参考
  - 新增 [GetForumSquare/](app/src/main/protos/GetForumSquare/) 下 Request/Response proto：common、class_name、pn、rn（及可选 user_id、second_class_name）；Response 含 page_structure、forum_info（RecommendForumInfo）、page、class_name、second_class_list
  - 复用现有 [RecommendForumInfo.proto](app/src/main/protos/RecommendForumInfo.proto)、[Page.proto](app/src/main/protos/Page.proto)
  - [OfficialProtobufTiebaApi.kt](app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/OfficialProtobufTiebaApi.kt)、[ITiebaApi](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt)、[MixedTiebaApiImpl.kt](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt) 已接好；暂无 UI，接口层可复用（如「吧广场」发现页）

### 已完成（Phase 7 — SearchPostForum 迁移并与 tbclient 对齐）

- `/c/f/forum/searchPostForum`（cmd=309466）→ `searchPostForumFlow(word)` — 端到端迁移（用于 get_tab_map）
  - 以 tbclient.protobuf SearchPostForum DataReq/DataRes 与 aiotieba get_tab_map 为参考
  - 新增 [SearchPostForum/](app/src/main/protos/SearchPostForum/) 下 [SearchForum.proto](app/src/main/protos/SearchPostForum/SearchForum.proto)、Request/Response：Request 含 common、word（吧名/关键词）；Response 含 exact_match（SearchForum）、fuzzy_match（repeated SearchForum）；SearchForum 含 forum_id、forum_name、avatar、post_num、concern_num、slogan、intro、has_concerned、tab_info（FrsTabInfo）
  - 复用现有 [FrsTabInfo.proto](app/src/main/protos/FrsTabInfo.proto)
  - [OfficialProtobufTiebaApi.kt](app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/OfficialProtobufTiebaApi.kt)、[ITiebaApi](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt)、[MixedTiebaApiImpl.kt](app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt) 已接好；可替代 get_tab_map 的 JSON 调用

### 迁移可行性验证

深入分析发现，上游 tbclient.protobuf 中只有 **6 个端点** 有完整 IDL（ReqIdl + ResIdl），其余仅有数据模型定义：

| 端点 | 有完整 IDL | cmd 已知 | 可迁移 |
|------|:---------:|:-------:|:------:|
| ReplyMe | ✓ | ✓ (303007) | ✓ 已完成 |
| AgreeMe | ✓ | ✗ | ✗ 待确认 cmd |
| Search | ✓ | ✗ | ✗ 待确认 cmd |
| GetDislikeList | ✓ | ✓ (309692) | ✓ 已完成 |
| Hottopic | ✓ | ✗ | ✗ 待确认 cmd |
| SearchPostForum | ✓ | ✓ (309466) | ✓ 已完成（get_tab_map） |
| ReSign / MFollow / SearchFriend 等 | ✗ | — | ✗ 无 IDL |

### aiotieba cmd 全量审计（Phase 5）

对 aiotieba 全量 protobuf 端点扫描，确认 TiebaLite 已覆盖其中 13/15 个 HTTP protobuf 端点。

**aiotieba 中有但 TiebaLite 尚未实现的 protobuf 端点**：

| cmd | 端点路径 | 功能 | 备注 |
|-----|---------|------|------|
| 303028 | `/c/u/user/userMuteQuery` | 黑名单查询 | 低优先级 |
| 309466 | `/c/f/forum/searchPostForum` | 搜索标签映射 | 已实现 searchPostForumFlow（get_tab_map） |
| 309653 | `/c/f/forum/getForumSquare` | 吧广场 | 已实现 getForumSquareFlow |
| 309692 | `/c/u/user/getDislikeList` | 不喜欢的吧列表 | 已实现 getDislikeListFlow |
| 309697 | `/c/c/user/setUserBlack` | 设置黑名单 | 操作类 |
| 309702 | `/c/u/user/getUserByTiebaUid` | UID转用户信息 | 工具类 |

**确认为 JSON-only 的端点**（aiotieba 也使用 JSON）：

`atMe`、`follow/unfollow`、`sign`、`followList`、`fans` — 服务端不支持 protobuf

**高优先级迁移候选**：

| 端点路径 | 函数名 | cmd | 分类 |
|---------|--------|-----|------|
| `/c/u/user/getDislikeList` | getDislikeListFlow（已完成） | 309692 | 推荐不感兴趣 |
| `/c/f/forum/getForumSquare` | getForumSquareFlow（已完成） | 309653 | 吧广场发现 |
| `/c/f/forum/searchPostForum` | searchPostForumFlow（已完成） | 309466 | get_tab_map |
| `/c/u/feed/agreeme` | agreeMe | 未知 | 消息-点赞 |

**暂不迁移**（无 Proto 定义或纯操作类）：

点赞(`opAgree`)、消息提醒(`msg`)、@我(`atMe`)、同步(`sync`)、登录(`login`)、昵称初始化(`initNickName`)、资料修改(`profileModify`)、头像上传(`imgPortrait`)、关注吧列表(`getForumList`)、批量签到(`mSign`)、关注/取关吧(`likeForum`/`unlikeForum`)、收藏管理(`addStore`/`removeStore`)、管理操作(`delThread`/`delPost`)、举报(`checkReport`)、图片上传(`uploadPicture`)、所有 Web 端点(`/mo/q/`、`/mg/o/`、`/f`)

**涉及文件**

- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/`

---

## 3. 待落地高优先级项

## 3.1 客户端版本统一升级

**状态：已落地**  
**影响：高（协议兼容与风控指纹）**  
**难度：中**

### 当前情况

```kotlin
enum class ClientVersion(val version: String) {
    TIEBA_V11("12.64.1.1"),       // Phase 3 升级（原 11.10.8.6）
    TIEBA_V12("12.64.1.1"),       // Phase 1 已升级（原 12.52.1.0）
    TIEBA_V12_POST("12.64.1.1");  // Phase 5 已统一（原 12.35.1.0，发帖/回帖/上传链路）
}
```

### 已完成

- `TIEBA_V12` 从 `12.52.1.0` 升级到 `12.64.1.1`（与 aiotieba `MAIN_VERSION` 对齐）
- `UserPage.kt` 中硬编码版本号改为引用 `ClientVersion.TIEBA_V12.version`
- 所有通过 `ClientVersion.TIEBA_V12` 引用的 `_client_version` 和 `User-Agent` 自动跟随更新

### 已完成（Phase 2）

- `OfficialTiebaApi.kt` 13 处硬编码 `"11.10.8.6"` 已全部改为引用 `ClientVersion.TIEBA_V11.version`
- `MixedTiebaApiImpl.kt` imgPortrait 中硬编码版本已改为 `ClientVersion.TIEBA_V11.version`
- `RetrofitTiebaApi.kt` WEB_TIEBA_API User-Agent 已改为引用 `ClientVersion.TIEBA_V11.version`
- 现在升级 V11 版本号只需修改 `Enums.kt` 一处枚举值

### 已完成（Phase 3）

- `TIEBA_V11` 从 `11.10.8.6` 升级到 `12.64.1.1`，现在 V11 和 V12 版本号统一
- 注意：此时 `TIEBA_V11` 和 `TIEBA_V12` 版本号相同，但 `CommonRequest` 构建逻辑仍有差异（V11 使用简化结构）

### 已完成（Phase 4 — V11/V12 CommonRequest 统一）

- `buildCommonRequest` 中 V11 分支已与 V12 合并，使用完整的 V12 结构（含 `active_timestamp`、`android_id`、`scr_*`、`z_id` 等字段）
- 4 个残留的 V11 protobuf 端点（`userLikeFlow`、`hotThreadListFlow`、`topicListFlow`、`forumRecommendNewFlow`）已迁移到 `OFFICIAL_PROTOBUF_TIEBA_V12_API` 客户端
- `OFFICIAL_PROTOBUF_TIEBA_API` 已标记 `@Deprecated`，实际委托到 V12 客户端
- `buildProtobufRequestBody` 移除了 V11 特殊的 `_client_version` 表单字段逻辑，默认版本改为 V12
- `ProtobufRequest.kt` 清理了 `OAID`、`toJson` 等不再使用的 import
- V12_POST 分支保持独立（`android_id` 不做 base64、含 `applist`/`device_score`/`tbs`）

### 已完成（Phase 5 — V12_POST 版本号统一到 12.64.1.1）

- `TIEBA_V12_POST` 从 `12.35.1.0` 升级到 `12.64.1.1`，发帖/回帖/楼中楼/上传均使用统一版本号
- [Enums.kt](app/src/main/java/com/huanchengfly/tieba/post/api/Enums.kt) 中枚举值已修改；[RetrofitTiebaApi.kt](app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt)（OFFICIAL_PROTOBUF_TIEBA_POST_API、HYBRID_TIEBA_API）、[SofireUtils.kt](app/src/main/java/com/huanchengfly/tieba/post/utils/SofireUtils.kt) 中版本号均引用 `ClientVersion.TIEBA_V12_POST.version`，无硬编码残留
- 建议按文档第 7 节验收清单对发帖/回帖/上传图片链路做一次回归

### 说明

- `TIEBA_V11` 枚举值仍保留，作为 JSON 端点默认版本的语义别名
- 保证以下字段链路一致：`ClientVersion`、`CommonRequest`、`User-Agent`、`_client_version`

### 涉及文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/Enums.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/ProtobufRequest.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/user/UserPage.kt`

## 3.2 Multipart Boundary 升级并去重

**状态：已落地**  
**影响：中（协议指纹一致性）**  
**难度：低**

### 已完成

- `BOUNDARY` 常量值从 `--------7da3d81520810*` 升级为 `-*_r1999`（与 aiotieba 对齐）
- `MixedTiebaApiImpl.imgPortrait()` 中硬编码的 boundary 已改为引用 `BOUNDARY` 常量
- 所有 multipart 请求现在统一使用 `ProtobufRequest.BOUNDARY` 单一来源

### 涉及文件

- `app/src/main/java/com/huanchengfly/tieba/post/api/ProtobufRequest.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/components/ImageUploader.kt`（已正确引用常量）

## 3.3 Protobuf 定义对齐

**状态：部分落地（Phase 2）**  
**影响：中高（新字段可用性、未来兼容）**  
**难度：高**

### 建议策略

1. 先选高频消息（`pbPage`、`frsPage`、`personalized`）做字段 diff
2. 优先补"向后兼容字段"，避免大范围改动
3. 每批字段升级都附带回归清单（加载、翻页、发帖、楼中楼）

### 已完成（Phase 1 — 向后兼容字段对齐）

以 [tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) 为基准，对三个高频消息做了字段 diff，补齐了仅使用已有类型或标量的新字段：

**PbPage Response**（`PbPageResponseData.proto`）
- 补 `Post top_answer = 73` — 置顶最佳回答（复用现有 Post 类型）

**FrsPage Response**（`FrsPage.proto` → `FrsPageResponseData`）
- 补 `int32 fortune_bag = 10`
- 补 `ThreadInfo store_card = 30` — 收藏卡片
- 补 `string bawu_enter_url = 32` — 吧务入口链接
- 补 `int32 frs_tab_default = 38` — 默认 Tab
- 补 `int32 sort_type = 39` — 当前排序类型
- 补 `int32 show_adsense = 108`

**Personalized Response**（`Personalized.proto` → `PersonalizedResponseData`）
- 补 `uint32 sug_seconds = 6` — 推荐刷新间隔
- 补 `repeated SimpleForum like_forums = 14` — 关注吧列表
- 补 `Anti anti = 16` — 反作弊信息

**PbPage / FrsPage / Personalized Request — 无需修改**（TiebaLite 已有的字段与上游一致）

### 已完成（Phase 2 — 共享类型对齐 + PbPage 新类型）

**共享类型字段对齐**（以 tbclient.protobuf 为基准补齐 scalar 与已有类型字段）：

- **SimpleForum** — 补 15 个字段（`is_exists`、`is_liked`、`is_signed`、`first_class`、`second_class`、`ext`、`level_id`、`is_brand_forum`、`tab_info`、`forum_toutu`、`is_frs_mask`、`theme_color`、`recommend_tip`、`pendants`、`scheme`）
- **Post** — 补 8 个 scalar 字段（`dynamic_url`、`rumor_source_img`、`shield_icon`、`icon_url`、`toutiao_card_tag`、`toutiao_card_tag_color`、`is_bot_reply`、`bot_reply_content`）
- **User** — 补 19 个字段（`no_post_high`、`ala_info`、`seal_prefix`、`has_bottle_enter`、`each_other_friend`、`ala_live_info`、`baijiahao_info`、`uk`、`business_account_info`、`appeal_thread_popover`、`work_num`、`show_pb_private_flag`、`follow_from`、`manager_forum`、`display_auth_type`、`ios_b_url`、`pa`、`enable_new_homepage`、`target_scheme`）
- **ThreadInfo** — 补 55 个字段，包括：
  - scalar：`is_vote`、`is_voice_thread`、`thread_type`、`is_livepost`、`is_bub`、`is_global_top`、`is_pic`、`repost_num`、`has_commented`、`from`、`time`、`valid_post_num`、`is_ad`、`timeline`、`hot_weight`、`storecount`、`post_num`、`category_name`、`is_novel`、`pic_num`、`is_deal`、`recom_source`、`recom_weight`、`last_read_pid`、`recom_reason`、`is_multiforum_thread`、`is_partial_visible`、`freq_num`、`is_god`、`trans_num`、`is_bjh`、`is_headlinepost`、`thread_share_link`、`if_comment`、`collect_num`、`is_pictxt`、`is_highlight`、`is_excellent_thread`、`head_type`、`disable_share`、`share_url`、`top_thread_set_time`
  - 已有类型：`voice_info`、`zan`、`anchor_info`、`location`、`hotTWInfo`、`poll_info`、`ext_tails`、`high_together`、`deal_info`、`skin_info`、`video_ad_info`、`multiple_forum_list`、`top_agree_post`、`pic_info`、`baijiahao`、`pb_link_info`、`item`、`item_star`、`tiebaplus_ad`、`hot_post_list`

**PbPage Response 新类型**（fields 74-79）：

- 新建 15 个 proto 文件：`JumpLinkInfo`、`SimilarContent`、`AiChatCard`、`AichatBotCard`、`ChatContent`、`RobotEntrance`、`RobotSkill`、`RobotSkillInfo`、`CallRobotEntrance`、`AbilityConf`、`StyleConf`、`StyleConfExtra`、`StyleContentInfo`、`BotReplyContent`、`BotReplyUserInfo`
- `PbPageResponseData` 补齐：`jump_link_info=74`、`similar_content=75`、`robot_entrance=76`、`aichat_card=77`、`aichat_bot_card=79`

### 剩余工作

- FrsPage Response 尚有约 100 个上游字段未补齐（多为运营/活动/广告相关，优先级低）
- Personalized Response 尚有约 25 个上游字段未补齐（标签、卡片、广告等，需新建 proto 类型）
- ThreadInfo 尚有约 36 个需新建 proto 类型的字段未补齐（多为冷门运营/活动/小说等功能）
- Post 尚有约 14 个需新建 proto 类型的字段未补齐
- User 尚有约 33 个需新建 proto 类型的字段未补齐
- SimpleForum 尚有 4 个需新建 proto 类型的字段未补齐

---

## 4. 长期项

## 4.1 WebSocket 支持（消息推送）

**状态：未落地（长期）**  
**影响：中高（实时性）**  
**难度：高**

建议先做技术预研，不直接进入主线开发。先明确：

- 是否有稳定可用的贴吧 WS 协议入口
- 移动端后台保活与心跳成本
- 降级策略（WS 失败自动回落 HTTP 轮询）

---

## 5. 与客户端侧优化文档的关系

`docs/optimization-client-side.md` 中以下内容已在代码落地，协议文档不再重复作为“待做项”：

- DNS 缓存
- 连接预热和连接池保活
- 代理支持
- 超时优化
- 部分预取增强与分页参数优化

本文件聚焦协议层剩余问题：**JSON-only 端点迁移、proto 定义升级**（客户端版本已统一到 12.64.1.1）。

---

## 6. 最新实施优先级（仅未完成项）

| 顺序 | 优化项 | 状态 | 预期收益 | 风险 |
|------|--------|------|---------|------|
| ~~1~~ | ~~客户端版本统一到 `12.64.1.1`~~ | ✅ V11+V12 已完成 | 兼容性、风控一致性 | — |
| ~~2~~ | ~~Boundary 升级并去重~~ | ✅ 已落地 | 请求指纹一致性 | — |
| ~~3~~ | ~~V11/V12 CommonRequest 合并~~ | ✅ 已落地 | 代码简化、协议一致 | — |
| 4 | JSON 接口分批迁移 Protobuf | ReplyMe 已迁移；cmd 审计完成 | 性能、一致性 | 中 |
| 5 | Protobuf 定义对齐升级 | Phase 1+2 已完成 | 剩余为低优先级字段 | 低 |
| ~~6~~ | ~~V12_POST 版本号升级~~ | ✅ 已落地（12.64.1.1） | 发帖指纹一致性 | — |
| 7 | WebSocket 预研 | 未落地 | 实时推送能力 | 高 |

---

## 7. 建议验收清单

每次协议改动建议按以下最小集合回归：

- 登录态接口（我的关注、个人页、消息列表）
- 帖子详情链路（首开、翻页、楼中楼）
- 发帖/回帖/上传图片链路
- 搜索与吧页列表链路
- 弱网与代理开启场景

---

## 参考资源

- [aiotieba GitHub](https://github.com/lumina37/aiotieba)
- [aiotieba 文档](https://aiotieba.cc/)
- [tbclient.protobuf](https://github.com/n0099/tbclient.protobuf)
