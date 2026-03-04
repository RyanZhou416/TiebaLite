# TiebaLite 架构升级方案

> 本文档覆盖从构建工具链到依赖库的全面现代化升级。
> 升级跨度大（约 2 年的版本差距），因此按依赖关系分阶段执行，每个阶段完成后应保证项目可编译通过。

---

## 升级进度

| 阶段 | 状态 | 备注 |
|------|------|------|
| 1. SweetDependency → TOML | ✅ 已完成 | 86 个库 + 9 个插件迁移完成；附带发现 JDK 25 与 Kotlin 1.9 不兼容，已在 gradle.properties 中固定 JDK 17 |
| 2. JDK 11 → 17 | ✅ 已完成 | compileOptions + jvmTarget 更新；CI 本就使用 JDK 17 无需修改 |
| 3~6. Gradle+AGP+Kotlin+Hilt+ButterKnife | ✅ 已完成 | **合并执行**（因循环依赖）：AGP 9→需Hilt 2.59+→需KSP→需去kapt→需去ButterKnife→需Kotlin 2.x。详见下方 |
| 7. Compose BOM + Destinations 2.x | ✅ 已完成 | Compose BOM 2026.01.01 + Destinations 2.2.0；minSdk 21→23 |
| 8. Accompanist 替换 | ✅ 已完成 | systemuicontroller → enableEdgeToEdge + WindowInsetsControllerCompat；placeholder → 项目内 fork；insets-ui Scaffold → 官方 Scaffold；Accompanist 0.34.0→0.37.3（仅保留 drawablepainter） |
| 9. Sketch 3 → 4 | ✅ 已完成 | sketch3 3.3.0 → sketch4 4.3.1；zoomimage 1.0.1 → 1.4.0；~20 文件 API 迁移 |
| 10. LitePal → Room | ✅ 已完成 | LitePal 3.0.0 → Room 2.8.4；7 Entity + 7 DAO + AppDatabase + Hilt DI + Migration 37→38；11 文件 API 迁移 |
| 11. Retrofit 升级 | ✅ 已完成 | Retrofit 2.9.0 → 2.12.0；自定义 Gson Converter 兼容，无代码变更 |
| 12. Java → Kotlin 转换 | ✅ 已完成 | 106 个 Java 文件全部转为 Kotlin；项目现为 100% Kotlin |
| 13. 最终清理 | ✅ 已完成 | 移除 Jetifier；启用 `nonTransitiveRClass` + `nonFinalResIds`；清理 TOML 残留条目 |
| 14. 底层架构收尾 | ✅ 已完成 | AndroidX 核心库版本升级；废弃 API 替换；协程统一；DataStore 优化。详见下方 |

### 阶段 3~6 合并执行详情

由于以下依赖链，原计划的阶段 3、4、5、6 必须合并为一步执行：

```
AGP 9.0 内置 Kotlin → 不兼容 kotlin-android 插件
                     → 不兼容 kapt 插件
                       → 必须移除 ButterKnife（唯一的 kapt 消费者之外还有 Hilt）
                       → Hilt 必须从 kapt 切到 KSP
                       → Hilt 2.59+ 才支持 AGP 9
Kotlin 2.3.10 K2 编译器 → ButterKnife @BindView(R.id.xxx) 注解不兼容
```

**实际变更清单：**

| 组件 | 旧版本 | 新版本 |
|------|--------|--------|
| Gradle | 8.5 | 9.3.1 |
| AGP | 8.2.2 | 9.0.1 |
| Kotlin | 1.9.22 | 2.3.10 (AGP 9 内置) |
| KSP | 1.9.22-1.0.17 | 2.3.6 |
| Hilt | 2.46.1 | 2.59.2 |
| Wire | 4.9.3 | 6.0.0-alpha02 (AGP 9 兼容) |
| kotlinx-coroutines | 1.7.3 | 1.10.1 |
| kotlinx-serialization | 1.6.2 | 1.8.0 |
| kotlinx-collections-immutable | 0.3.7 | 0.3.8 |
| dependency-analysis | 1.29.0 | 2.17.0 |
| AndroidX Hilt | 1.1.0 | 1.2.0 |
| compileSdk / targetSdk | 34 | 36 |

**架构变更：**
- ButterKnife 完全移除（5 个文件，替换为 `findViewById`）
- kapt 插件完全移除
- `kotlin-android` 插件移除（AGP 9 内置 Kotlin 支持）
- Hilt 注解处理从 kapt 迁移到 KSP
- `composeOptions {}` 移除，改用 `composeCompiler {}` DSL
- `kotlinOptions {}` 移除，改用 `kotlin { compilerOptions {} }` DSL
- `applicationVariants` / `BaseVariantOutputImpl` 代码移除（AGP 9 新 DSL 不支持）
- `android.defaults.buildfeatures.buildconfig=true` 移入 `buildFeatures { buildConfig = true }`
- `android.nonFinalResIds=false` 移除，Java switch-case R.id 改为 if-else
- Compose Compiler 参数从 `freeCompilerArgs` 迁移到 `composeCompiler {}` 块

---

## 目标版本一览

| 组件 | 当前版本 | 目标版本 | 跨度 |
|------|---------|---------|------|
| **JDK** | 11 | 17 | 主要 |
| **Gradle** | 8.5 | 9.3.1 | 主要 |
| **AGP** | 8.2.2 | 9.0.1 | 主要 |
| **Kotlin** | 1.9.22 | 2.3.10 | 主要 |
| **KSP** | 1.9.22-1.0.17 | 2.3.6 | 主要 |
| **Compose BOM** | 2024.01.00 | 2026.01.01 | 主要 |
| **Compose Compiler** | 1.5.8 (独立版本) | Kotlin 内置 (插件方式) | 架构变更 |
| **compileSdk / targetSdk** | 34 | 36 | 次要 |
| **minSdk** | 21 | 23 (Compose BOM 2026 要求) | 次要 |
| **Hilt** | 2.46.1 | 2.59.2 | 主要 |
| **Retrofit** | 2.9.0 | 2.12.0 | 次要 |
| **OkHttp** | 4.12.0 | 保持 (Retrofit 2.12 兼容) | — |
| **Wire** | 4.9.3 | 6.0.0-alpha02 (AGP 9 必需) | 主要 |
| **Compose Destinations** | 1.10.0 | 2.2.0 | 主要 |
| **Sketch** | 3.3.0 | 4.1.0 | 主要 |
| **Accompanist** | 0.34.0 | 移除/替换 | 移除 |
| **数据库** | LitePal 3.0.0 | Room 2.8.4 | 重写 |
| **依赖管理** | SweetDependency 1.0.4 | Gradle Version Catalog (TOML) | 架构变更 |
| **注解处理** | kapt | KSP (已完成) | 架构变更 |
| **ButterKnife** | 10.2.3 | 移除 (已完成) | 移除 |
| **Jetifier** | 启用 | 移除 | 移除 |

---

## 1. 迁移依赖管理：SweetDependency → Gradle Version Catalog

**影响**: 高 | **难度**: 中 | **前置依赖**: 无

### 现状问题

SweetDependency（1.0.4）是一个仅有 52 stars 的小众 Gradle 插件，最后一次发布是 2023 年 11 月。存在以下风险：

- **未验证对 Gradle 9.x / Kotlin 2.x 的兼容性**，后续升级可能直接卡住
- 使用 YAML 配置而非 Gradle 标准的 TOML，IDE 支持差（无自动补全、无跳转）
- SweetProperty 插件也存在同样的维护风险
- `autowire()` 是非标准 DSL，新开发者学习成本高

### 迁移方案

将 `gradle/sweet-dependency/sweet-dependency-config.yaml` 转换为标准的 `gradle/libs.versions.toml`。

#### 1.1 创建 `gradle/libs.versions.toml`

```toml
[versions]
agp = "9.0.1"
kotlin = "2.3.10"
ksp = "2.3.6"
hilt = "2.59.2"
compose-bom = "2026.01.01"
compose-destinations = "2.1.0"
accompanist = "0.36.0"
sketch = "4.1.0"
wire = "4.9.3"
retrofit = "2.12.0"
okhttp = "4.12.0"
room = "2.8.4"
# ... 其他版本

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-parcelize = { id = "org.jetbrains.kotlin.plugin.parcelize", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
wire = { id = "com.squareup.wire", version.ref = "wire" }
room = { id = "androidx.room", version.ref = "room" }

[libraries]
# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-animation = { module = "androidx.compose.animation:animation" }
compose-animation-graphics = { module = "androidx.compose.animation:animation-graphics" }
compose-material = { module = "androidx.compose.material:material" }
compose-material-icons-core = { module = "androidx.compose.material:material-icons-core" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
compose-ui-util = { module = "androidx.compose.ui:ui-util" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
compose-runtime-tracing = { module = "androidx.compose.runtime:runtime-tracing", version = "1.0.0-beta01" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version = "1.3.0" }
hilt-compiler-androidx = { module = "androidx.hilt:hilt-compiler", version = "1.3.0" }

# Network
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-converter-wire = { module = "com.squareup.retrofit2:converter-wire", version.ref = "retrofit" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# Wire
wire-runtime = { module = "com.squareup.wire:wire-runtime", version.ref = "wire" }

# ... 其余依赖同理转换
```

#### 1.2 更新 `settings.gradle.kts`

```kotlin
// 移除
// id("com.highcapable.sweetdependency") version "1.0.4"
// id("com.highcapable.sweetproperty") version "1.0.5"
// 以及全部 sweetProperty { ... } 配置块

// SweetProperty 管理的 application.properties 和 keystore.properties
// 改为在 build.gradle.kts 中手动读取：
// val props = Properties().apply { load(rootProject.file("application.properties").reader()) }
```

#### 1.3 更新 `build.gradle.kts`（项目级）

```kotlin
// 修改前
plugins {
    autowire(libs.plugins.com.android.application) apply false
    autowire(libs.plugins.kotlin.android) apply false
    // ...
}

// 修改后
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.wire) apply false
}
```

#### 1.4 更新 `app/build.gradle.kts`

```kotlin
// 修改前
plugins {
    autowire(libs.plugins.com.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.kapt)           // 将在后续阶段移除
    autowire(libs.plugins.kotlin.serialization)
    autowire(libs.plugins.kotlin.parcelize)
    autowire(libs.plugins.hilt.android)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.com.squareup.wire)
}

// 修改后
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)           // 新增：Compose Compiler 插件
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.wire)
}
```

依赖声明从 SweetDependency 的 `implementation(sketch.core)` 风格改为标准的 `implementation(libs.sketch.core)`。

#### 1.5 处理 SweetProperty

SweetProperty 用于读取 `application.properties` 和 `keystore.properties`。迁移后手动读取：

```kotlin
// app/build.gradle.kts 顶部
import java.util.Properties

val appProps = Properties().apply {
    rootProject.file("application.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}
val keystoreProps = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}

val applicationVersionCode = appProps.getProperty("versionCode")?.toIntOrNull() ?: 1
val applicationVersionName = appProps.getProperty("versionName") ?: "1.0.0"
// ... 其他属性
```

### 潜在问题

- SweetDependency 的 `autowire()` 在 ~260 行依赖声明中使用，需逐行转换
- SweetProperty 的 `property.xxx` 访问器需要全部替换为手动读取
- `application.properties` 的字段名需要确认（`versionCode`、`versionName`、`isPreRelease`、`preReleaseName`、`preReleaseVer`）

### 需要修改的文件

- 删除 `gradle/sweet-dependency/` 目录
- 新建 `gradle/libs.versions.toml`
- `settings.gradle.kts` — 移除 SweetDependency 和 SweetProperty 插件
- `build.gradle.kts` — 更新 plugins 声明
- `app/build.gradle.kts` — 更新 plugins 和所有依赖声明

---

## 2. 升级 JDK：11 → 17

**影响**: 中 | **难度**: 低 | **前置依赖**: 无

### 为什么需要

- AGP 9.0 要求 JDK 17
- Gradle 9.x 要求 JDK 17
- JDK 17 是当前 Android 开发的标准基线

### 修改方案

#### 2.1 `app/build.gradle.kts`

```kotlin
// 修改前
compileOptions {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = "11"
}

// 修改后
compileOptions {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}
kotlinOptions {
    jvmTarget = "17"
}
```

#### 2.2 CI/CD 配置

检查 `.github/workflows/build.yml`，确保 CI 使用 JDK 17：

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: 'zulu'
    java-version: '17'
```

### 潜在问题

- 项目中 108 个 Java 文件均使用较旧的 Java 语法，JDK 17 向后兼容，不会有编译问题
- 确保本地开发环境和 CI 环境都更新到 JDK 17

### 需要修改的文件

- `app/build.gradle.kts` — `compileOptions` + `kotlinOptions`
- `.github/workflows/build.yml` — JDK 版本

---

## 3. 升级 Gradle + AGP

**影响**: 高 | **难度**: 高 | **前置依赖**: #1 (TOML 迁移), #2 (JDK 17)

### 版本跳跃

- Gradle: 8.5 → 9.3.1（跨主版本）
- AGP: 8.2.2 → 9.0.1（跨主版本）

### 3.1 升级 Gradle Wrapper

```properties
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.1-all.zip
```

### 3.2 升级 AGP

在 `gradle/libs.versions.toml` 中设置 `agp = "9.0.1"`。

### 3.3 修复 AGP 9.0 破坏性变更

AGP 9.0 移除了旧的 Variant API，项目中直接受影响的代码：

#### 移除 `BaseVariantOutputImpl`

```kotlin
// 修改前 (app/build.gradle.kts)
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

applicationVariants.configureEach {
    val variant = this
    outputs.configureEach {
        val fileName =
            "${variant.buildType.name}-${applicationVersionName}(${applicationVersionCode}).apk"
        (this as BaseVariantOutputImpl).outputFileName = fileName
    }
    kotlin.sourceSets {
        getByName(variant.name) {
            kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
        }
    }
}

// 修改后：使用新的 Artifacts API
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set(
                "${variant.buildType}-${applicationVersionName}(${applicationVersionCode}).apk"
            )
        }
    }
}
// KSP 生成目录通常由 KSP 插件自动配置，不再需要手动添加 srcDir
```

#### 更新 `buildToolsVersion`

```kotlin
// 修改前
buildToolsVersion = "34.0.0"
compileSdk = 34

// 修改后
// AGP 9.0 默认管理 buildToolsVersion，可以移除显式声明
// buildToolsVersion = "36.0.0"  // 可选：显式指定
compileSdk = 36
```

#### 更新 `targetSdk`

```kotlin
defaultConfig {
    // ...
    //noinspection OldTargetApi
    targetSdk = 36  // 34 → 36
}
```

#### 移除 `composeOptions`（Kotlin 2.0+ 不再需要）

```kotlin
// 移除整个块
// composeOptions {
//     kotlinCompilerExtensionVersion = "1.5.8"
// }
```

Compose Compiler 现在通过 `org.jetbrains.kotlin.plugin.compose` 插件管理，版本与 Kotlin 绑定。

#### 更新 `kotlinOptions` 中的 Compose Compiler 参数

```kotlin
// 修改前
kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=..."
    )
    freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=..."
    )
    freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=..."
    )
}

// 修改后：使用 Compose Compiler 插件 DSL
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_metrics")
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_stability_configuration.txt")
}
```

注意：`project.buildDir` 在 Gradle 9.x 中已废弃，改用 `layout.buildDirectory`。

### 3.4 更新 `gradle.properties`

```properties
# 修改前
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8 \
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED

# 修改后（移除 kapt 需要的 --add-exports，kapt 移除后不再需要）
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8

# 可选：启用 Configuration Cache（Gradle 9.x 改进了兼容性）
org.gradle.configuration-cache=true
```

### 潜在问题

- **`BaseVariantOutputImpl` 是内部 API**：AGP 9.0 移除后必须迁移到 `androidComponents` API，否则编译直接失败
- **Gradle 9.x 废弃的 API**：`project.buildDir` → `layout.buildDirectory`；`configurations.compile` 等旧配置名 → 新名称
- **第三方 Gradle 插件兼容性**：Wire 插件、dependency-analysis 插件需要验证是否兼容 Gradle 9.x
- **Configuration Cache**：如果启用，需要验证所有插件兼容
- **`android.defaults.buildfeatures.buildconfig=true`**：AGP 9.0 中 BuildConfig 默认关闭，需要在 `buildFeatures` 中显式开启

### 需要修改的文件

- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/libs.versions.toml` — AGP 版本
- `build.gradle.kts` — 插件声明
- `app/build.gradle.kts` — Variant API 迁移、composeOptions 移除、compileSdk/targetSdk
- `gradle.properties` — JVM 参数、废弃属性

---

## 4. 升级 Kotlin：1.9.22 → 2.3.10

**影响**: 高 | **难度**: 中高 | **前置依赖**: #3 (Gradle + AGP 升级)

### 主要变更

Kotlin 2.0 引入了 K2 编译器，2.3.10 是当前最新稳定版。关键影响：

1. **K2 编译器**：更严格的类型检查，可能暴露之前被忽略的类型错误
2. **Compose Compiler 内置**：不再需要独立版本号，通过 `kotlin-compose` 插件管理
3. **KSP 版本绑定**：KSP 版本必须匹配 Kotlin 版本（2.3.6 对应 2.3.10）
4. **kotlinx 库兼容性**：kotlinx-serialization、kotlinx-coroutines 需要升级到兼容版本

### 4.1 KSP 版本同步

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.3.10"
ksp = "2.3.6"
```

### 4.2 升级 kotlinx 库

```toml
[versions]
kotlinx-coroutines = "1.10.1"       # 1.7.3 → 1.10.1（支持 Kotlin 2.x）
kotlinx-serialization = "1.8.0"      # 1.6.2 → 1.8.0（支持 Kotlin 2.x）
kotlinx-collections-immutable = "0.3.8"  # 0.3.7 → 0.3.8
```

### 4.3 K2 编译器注意事项

K2 编译器可能对以下模式报错（之前被 K1 放过）：

```kotlin
// K2 更严格的 Smart Cast
// 如果一个 var 在 lambda 中被捕获，K2 不允许 smart cast
// 修复方式：用 val localVar = sharedVar 显式拷贝

// K2 更严格的可见性检查
// internal 成员在某些跨模块场景下可能报错

// K2 对 Nothing 类型的推导更精确
// 某些 when 分支可能需要显式处理
```

**建议**：升级 Kotlin 后先跑一次完整编译，逐个修复 K2 报出的新错误。这些错误数量通常不多，但需要逐一排查。

### 潜在问题

- **kotlinx-serialization 版本不兼容**：Kotlin 2.3.10 要求 serialization 插件版本一致。如果代码中使用了 `@Serializable` 且涉及复杂泛型，K2 可能有不同的序列化代码生成行为
- **Compose Compiler 行为变化**：新版 Compose Compiler 对 Stability 推导更准确，可能导致某些之前被认为 Stable 的类型变为 Unstable，引起不必要的重组。需要借助 Compose Metrics 报告验证
- **Wire 插件兼容性**：Wire 4.9.3 的 Gradle 插件需要验证是否兼容 Kotlin 2.3.10，可能需要升级 Wire

### 需要修改的文件

- `gradle/libs.versions.toml` — Kotlin、KSP、kotlinx 版本
- `app/build.gradle.kts` — 移除 `composeOptions`，添加 `composeCompiler` 块
- 所有使用 kotlinx-serialization 的文件 — 检查 API 兼容性
- 所有 Compose 文件 — 检查 Stability 变化

---

## 5. Hilt 升级 + kapt → KSP 迁移

**影响**: 高 | **难度**: 中 | **前置依赖**: #4 (Kotlin 升级)

### 现状

- Hilt 2.46.1，使用 kapt 处理注解
- 25 个 `@HiltViewModel`，2 个 `@AndroidEntryPoint` Activity，1 个 `@HiltAndroidApp`，1 个 Hilt Module
- kapt 同时被 ButterKnife 使用（将在 #6 中移除）

### 5.1 升级 Hilt 版本

```toml
# gradle/libs.versions.toml
[versions]
hilt = "2.59.2"
```

Hilt 2.59+ 要求 AGP 9.0+，这就是为什么本节依赖 #3 完成。

### 5.2 kapt → KSP

```kotlin
// app/build.gradle.kts

// 修改前
plugins {
    // ...
    alias(libs.plugins.kotlin.kapt)  // kapt 插件（待移除 ButterKnife 后删除）
    alias(libs.plugins.ksp)
}

dependencies {
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.compiler.androidx)
}

// 修改后
plugins {
    // kotlin.kapt 在 ButterKnife 移除后可以彻底删除
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.compiler.androidx)
}
```

### 5.3 Hilt 2.59 API 变更

Hilt 2.59 的主要变化：

- AGP 9 支持
- AndroidX Hilt 1.3.0 将 `hiltViewModel()` APIs for Compose 移到了独立 artifact

检查 `arch/Extensions.kt` 中自定义的 `hiltViewModel` 扩展函数是否与新版本冲突：

```kotlin
// 当前：com.huanchengfly.tieba.post.arch.Extensions.kt
// 包含 hiltViewModel、createHiltViewModelFactory 等自定义扩展
// 需要验证与 Hilt 2.59.2 + hilt-navigation-compose 1.3.0 的兼容性
```

### 潜在问题

- **Hilt KSP 仍为 Alpha**：Google 标记为 alpha，但实际上已被大量生产项目使用。主要风险是边缘场景的代码生成问题，核心功能稳定
- **增量编译**：Hilt KSP 的增量编译行为与 kapt 不同，首次全量编译可能较慢，后续增量更快
- **kapt 不能与 KSP 同时处理同一注解**：确保 ButterKnife（仍用 kapt）和 Hilt（改用 KSP）之间没有交叉依赖。由于它们处理不同的注解，不会冲突
- **多 ViewModel 文件**：`NotificationsListViewModel.kt`（2 个类）和 `ForumThreadListViewModel.kt`（2 个类）在同一文件中定义了多个 `@HiltViewModel`，需验证 KSP 正确处理

### 需要修改的文件

- `app/build.gradle.kts` — `kapt(hilt.compiler)` → `ksp(libs.hilt.compiler)`
- `gradle/libs.versions.toml` — Hilt 版本
- `arch/Extensions.kt` — 验证自定义 hiltViewModel 兼容性
- `gradle.properties` — 移除 `kapt.verbose=true`（在 kapt 完全移除后）

---

## 6. 移除 ButterKnife

**影响**: 中 | **难度**: 低 | **前置依赖**: #5 (Hilt KSP 迁移完成后，kapt 仅剩 ButterKnife)

### 现状

ButterKnife 于 2020 年停止维护。项目中的使用范围很小：

| 文件 | 使用方式 |
|------|---------|
| `TranslucentThemeActivity.kt` | 12 个 `@BindView` |
| `AppFontSizeActivity.kt` | 5 个 `@BindView` |
| `BaseActivity.kt` | `ButterKnife.bind(this)` |
| `BaseFragment.kt` | `ButterKnife.bind(this, inflate)` + `Unbinder` |
| `BaseBottomSheetDialogFragment.java` | `ButterKnife.bind(this, rootView)` + `Unbinder` |

### 迁移方案

由于只有 2 个 Activity 直接使用 `@BindView`，且都有对应的 XML 布局文件，最简单的方式是用 `findViewById` 替换：

#### 6.1 替换 `@BindView`

```kotlin
// 修改前 (TranslucentThemeActivity.kt)
@BindView(R.id.some_view)
lateinit var someView: SomeViewType

// 修改后
private val someView: SomeViewType by lazy { findViewById(R.id.some_view) }
```

对 `TranslucentThemeActivity.kt`（12 个）和 `AppFontSizeActivity.kt`（5 个）的所有 `@BindView` 做同样替换。

#### 6.2 清理基类

```kotlin
// BaseActivity.kt — 移除
// import butterknife.ButterKnife
// ButterKnife.bind(this)  // 删除此行

// BaseFragment.kt — 移除
// import butterknife.ButterKnife
// import butterknife.Unbinder
// private var unbinder: Unbinder? = null
// unbinder = ButterKnife.bind(this, inflate)  // 删除
// unbinder?.unbind()  // 删除

// BaseBottomSheetDialogFragment.java — 同上
```

#### 6.3 移除依赖和 kapt

```kotlin
// app/build.gradle.kts — 删除以下行
// implementation(libs.butterknife)
// kapt(libs.butterknife.compiler)

// 移除 kapt 插件（此时 kapt 已无任何使用者）
// alias(libs.plugins.kotlin.kapt)  // 删除
```

#### 6.4 清理 gradle.properties

```properties
# 移除（kapt 已不再使用）
# kapt.verbose=true

# 移除 kapt 需要的 JVM 参数
# --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
# --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
# --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

### 潜在问题

- **BaseActivity 子类**：确认没有其他继承 `BaseActivity` 的子类在使用 ButterKnife 注解。根据搜索结果，只有 `TranslucentThemeActivity` 和 `AppFontSizeActivity` 使用了 `@BindView`
- **BaseBottomSheetDialogFragment.java**：这是一个 Java 文件，修改时注意语法差异
- **长期建议**：`TranslucentThemeActivity` 和 `AppFontSizeActivity` 应该在后续迁移到 Compose，那时 `findViewById` 也可以移除

### 需要修改的文件

- `app/build.gradle.kts` — 移除 ButterKnife 依赖和 kapt 插件
- `activities/TranslucentThemeActivity.kt` — 替换 12 个 `@BindView`
- `activities/AppFontSizeActivity.kt` — 替换 5 个 `@BindView`
- `activities/BaseActivity.kt` — 移除 `ButterKnife.bind`
- `fragments/BaseFragment.kt` — 移除 `ButterKnife.bind` 和 `Unbinder`
- `fragments/BaseBottomSheetDialogFragment.java` — 移除 `ButterKnife.bind` 和 `Unbinder`
- `gradle.properties` — 移除 `kapt.verbose` 和 `--add-exports`
- `gradle/libs.versions.toml` — 移除 ButterKnife 相关条目

---

## 7. 升级 Compose BOM + Compose Destinations 2.x

**影响**: 高 | **难度**: 高 | **前置依赖**: #4 (Kotlin 升级)

### 执行状态：✅ 已完成

### 7.1 Compose BOM 升级

```toml
# 2024.01.00 → 2026.01.01
compose-bom = "2026.01.01"
```

#### 修复的 Compose BOM Breaking Changes

| 问题 | 文件 | 修复方式 |
|------|------|---------|
| `rememberRipple` DeprecationLevel.ERROR | `ForumSearchPostPage.kt`, `SearchPage.kt`, `Search.kt`, `VideoPlayer.kt` | `import androidx.compose.material.ripple` + `ripple()` 替代 |
| `animateItemPlacement()` 移除 | `SearchPage.kt` | 改用 `animateItem()` |
| `SnapFlingBehavior` 变为 internal | `LazyLoad.kt` | 改用 `TargetedFlingBehavior` |
| `beyondBoundsPageCount` 重命名 | `LazyLoad.kt` | 改用 `beyondViewportPageCount` |
| `minSdk` 21 不满足新依赖要求 | `app/build.gradle.kts` | 提升到 `minSdk = 23` |

### 7.2 Compose Destinations 1.10.0 → 2.2.0

**实际变更清单：**

| 组件 | 旧版本 | 新版本 |
|------|--------|--------|
| Compose Destinations | 1.10.0 | 2.2.0 |
| artifact | `animations-core` | `core` + `bottom-sheet` |

**注**：初次尝试时因 Compose Destinations 2.x 与 KSP 2.3.6 不兼容（NPE）而失败。后发现 2.2.0 版本（2025-05-03 发布）已修复 KSP2 兼容性，迁移成功。

#### 核心 API 变更对照

| 1.x API | 2.x API |
|---------|---------|
| `@RootNavGraph(start = true)` + `@Destination` | `@Destination<RootGraph>(start = true)` |
| `@Destination` | `@Destination<RootGraph>` (必须指定图) |
| `animations-core` artifact | `core` + `bottom-sheet` artifact |
| `rememberAnimatedNavHostEngine()` | 移除，使用 `defaultTransitions` 参数 |
| `RootNavGraphDefaultAnimations(...)` | `NavHostAnimatedDestinationStyle()` 子类 |
| `DestinationStyleBottomSheet` (spec 包) | `DestinationStyleBottomSheet` (bottomsheet.spec 包) |
| `DestinationStyle.Dialog` (interface) | `DestinationStyle.Dialog()` (abstract class) |
| `DestinationSpec<*>` | `DestinationSpec` (无泛型) |
| `NavController.navigate(Direction)` | `NavController.toDestinationsNavigator().navigate(Direction)` |
| `DeepLink` (annotation 包) | `DeepLink` (annotation.parameters 包) |
| `baseRoute` | `route` |

#### build.gradle.kts 变更

```kotlin
// 新增 bottom-sheet 依赖
implementation(libs.compose.destinations.bottom.sheet)

// 新增 KSP 参数，保持生成代码在原包路径
ksp {
    arg("compose-destinations.codeGenPackageName", "com.huanchengfly.tieba.post.ui.page")
}
```

#### 导航引擎迁移 (MainActivityV2.kt)

```kotlin
// 旧：使用 rememberAnimatedNavHostEngine + Accompanist BottomSheet
val engine = TiebaNavHostDefaults.rememberNavHostEngine()
val navigator = TiebaNavHostDefaults.rememberBottomSheetNavigator()
navController.navigatorProvider += navigator
DestinationsNavHost(engine = engine, ...)

// 新：使用 material-navigation BottomSheet + defaultTransitions
val sheetState = rememberModalBottomSheetState(...)
val navigator = remember(sheetState) { BottomSheetNavigator(sheetState) }
val navController = rememberNavController(navigator)
DestinationsNavHost(defaultTransitions = TiebaDefaultAnimations, ...)
```

动画从 `TiebaNavHostDefaults` 对象（包含 Composable 方法）重构为 `TiebaDefaultAnimations` 对象（继承 `NavHostAnimatedDestinationStyle()`，以 override 属性声明动画）。

### 需要修改的文件

**Compose BOM：**
- `gradle/libs.versions.toml` — Compose BOM 版本
- `app/build.gradle.kts` — `minSdk` 21 → 23
- `ForumSearchPostPage.kt`, `SearchPage.kt`, `Search.kt`, `VideoPlayer.kt` — `rememberRipple` → `ripple`
- `SearchPage.kt` — `animateItemPlacement` → `animateItem`
- `LazyLoad.kt` — `SnapFlingBehavior` → `TargetedFlingBehavior` + `beyondBoundsPageCount` → `beyondViewportPageCount`

**Compose Destinations v2：**
- `gradle/libs.versions.toml` — 版本 + artifact 变更
- `app/build.gradle.kts` — 新增 `bottom-sheet` 依赖 + KSP 参数
- 29 个 `@Destination` 注解文件 — `@Destination` → `@Destination<RootGraph>`
- 6 个文件 — `DeepLink` import 路径更新
- `MainActivityV2.kt` — 导航引擎全面迁移
- `ReplyPage.kt`, `SubPostsPage.kt` — `DestinationStyleBottomSheet` import 路径更新
- `CopyDialogPage.kt` — `DestinationStyle.Dialog` 接口 → 抽象类
- `BackHandler.kt` — `DestinationSpec<*>` → `DestinationSpec` + `baseRoute` → `route`

---

## 8. 替换 Accompanist 废弃 API ✅ 已完成

**影响**: 中 | **难度**: 中 | **前置依赖**: #7 (Compose BOM 升级)

### 完成概要

Accompanist v0.37.0 已移除 systemuicontroller、placeholder、insets-ui 等废弃模块。本阶段将所有废弃 API 替换为官方替代方案或项目内 fork，并将 Accompanist 从 `0.34.0` 升级到 `0.37.3`（仅保留 `drawablepainter`）。

### 8.1 systemuicontroller → enableEdgeToEdge + WindowInsetsControllerCompat

**修改了 5 个文件：**

| 文件 | 变更 |
|------|------|
| `BaseComposeActivity.kt` | `enableEdgeToEdge()` 替代 `WindowCompat.setDecorFitsSystemWindows`；`WindowInsetsControllerCompat` 替代 `rememberSystemUiController`；移除 `onCreateContent` 的 `SystemUiController` 参数 |
| `MainActivityV2.kt` | 适配 `onCreateContent()` 新签名（无参数） |
| `PhotoViewActivity.kt` | `WindowInsetsControllerCompat.hide(systemBars())` 替代 `systemUiController.isSystemBarsVisible = false` |
| `EditProfileActivity.kt` | 同 BaseComposeActivity 模式：`enableEdgeToEdge()` + `WindowInsetsControllerCompat` |
| `FeedCard.kt` | 通过 `LocalView` 获取 `WindowInsetsControllerCompat`，使用 `hide/show(systemBars())` 控制全屏视频时的系统栏 |

### 8.2 insets-ui Scaffold → 官方 Scaffold

**修改了 1 个文件：** `HotTopicListPage.kt`

```kotlin
// 修改前
import com.google.accompanist.insets.ui.Scaffold
// 修改后
import androidx.compose.material.Scaffold
```

### 8.3 placeholder-material → 项目内 fork

**新建 1 个文件 + 修改 9 个文件的 import：**

从 Accompanist 源码 fork 了 placeholder 核心实现到 `ui/widgets/compose/Placeholder.kt`（约 148 行），包含：
- `PlaceholderHighlight` 接口 + `fade()` 工厂函数
- `Modifier.placeholder()` 扩展（自动从 MaterialTheme 派生默认颜色和形状）
- `PlaceholderDefaults` 默认动画规格

API 完全兼容，所有 9 个文件仅需修改 import 路径：

| 文件 | 变更 |
|------|------|
| `Avatars.kt` / `FeedCard.kt` / `Headers.kt` | 同包，删除 accompanist import 即可 |
| `ForumPage.kt` | → `...ui.widgets.compose.{PlaceholderHighlight, fade, placeholder}` |
| `EditProfileActivity.kt` / `HotPage.kt` / `BlockListPage.kt` / `UserPage.kt` / `HomePage.kt` | → `...ui.widgets.compose.placeholder` |

### 8.4 drawablepainter — 保留并升级

`accompanist-drawablepainter` 未被废弃，升级到 `0.37.3`。涉及 7 个文件（`Avatars.kt`、`HomePage.kt`、`AboutPage.kt`、`EmoticonManager.kt`、`Toolbar.kt`、`CustomSettingsPage.kt`、`ReplyPage.kt`）保持不变。

Compose BOM 2026.01.01 包含 Compose UI 1.10.3，Accompanist 0.37.3 官方支持到 1.7/1.8，但 `drawablepainter` 作为简单的 Drawable→Painter 包装器，实测与 1.10.3 兼容，构建通过。

### 8.5 navigation-material (BottomSheet)

已在阶段 7 通过 Compose Destinations 2.x 的 `bottom-sheet` artifact 处理完毕。

### 依赖清理

```toml
# libs.versions.toml
accompanist = "0.37.3"  # 从 0.34.0 升级
# 移除了 accompanist-insets-ui / accompanist-systemuicontroller / accompanist-placeholder-material
# 仅保留 accompanist-drawablepainter
```

```kotlin
// app/build.gradle.kts — 移除 3 行，保留 1 行
implementation(libs.accompanist.drawablepainter)
```

---

## 9. Sketch 3 → 4 图片加载库迁移 ✅ 已完成

**影响**: 中高 | **难度**: 中高 | **前置依赖**: #4 (Kotlin 升级)

### 完成概要

从 `io.github.panpf.sketch3` (3.3.0) 迁移到 `io.github.panpf.sketch4` (4.3.1)。Sketch 4 是面向 Compose Multiplatform 的全面重构版本，API 不兼容 v3。同步升级 zoomimage 从 1.0.1 → 1.4.0。

### 依赖变更

```toml
# libs.versions.toml
sketch = "4.3.1"          # 从 3.3.0 升级
zoomimage = "1.4.0"       # 从 1.0.1 升级

# 移除
sketch-core                # 合并到 sketch-compose
sketch-gif                 # 拆分为 sketch-animated-gif/webp/heif

# 新增/替换
sketch-compose = { module = "io.github.panpf.sketch4:sketch-compose" }
sketch-view = { module = "io.github.panpf.sketch4:sketch-view" }
sketch-ext-compose = { module = "io.github.panpf.sketch4:sketch-extensions-compose" }
sketch-animated-gif = { module = "io.github.panpf.sketch4:sketch-animated-gif" }
sketch-animated-webp = { module = "io.github.panpf.sketch4:sketch-animated-webp" }
sketch-animated-heif = { module = "io.github.panpf.sketch4:sketch-animated-heif" }
sketch-http-okhttp = { module = "io.github.panpf.sketch4:sketch-http-okhttp" }
zoomimage-compose-sketch = { module = "io.github.panpf.zoomimage:zoomimage-compose-sketch4" }
```

### 主要 API 变更

| Sketch 3 | Sketch 4 |
|-----------|----------|
| `SketchFactory` | `SingletonSketch.Factory` |
| `createSketch()` | `createSketch(context: PlatformContext)` |
| `DisplayRequest` | `ImageRequest` |
| `LoadRequest` / `DownloadRequest` | `ImageRequest` + `sketch.executeDownload()` |
| `DisplayResult` / `LoadResult` / `DownloadResult` | `ImageResult` |
| `AsyncImage(request = DisplayRequest(...))` | `AsyncImage(request = ComposableImageRequest(...))` 或 `AsyncImage(uri = ...)` |
| `sketch.compose.AsyncImage` | `sketch.AsyncImage` |
| `stateimage.ThumbnailMemoryCacheStateImage` | `state.ThumbnailMemoryCacheStateImage` |
| `placeholder(Drawable)` | `placeholder(DrawableStateImage(RealDrawableFetcher(EquitableDrawable(...))))` |
| `result.bitmap` / `result.drawable` | `result.image.asBitmap()` / `result.image.asDrawable()` |
| `disposeDisplay()` | `disposeLoad()` |
| `target(imageView)` | `target(ImageViewTarget(imageView))` |
| `resizeScale(Scale.X)` | `scale(Scale.X)` |
| `addDrawableDecoder(...)` | 自动注册（通过模块依赖） |
| `httpStack(OkHttpStack(...))` | `addIgnoreFetcherProvider()` + `addFetcher(OkHttpHttpUriFetcher.Factory(...))` |

### 修改的文件（约 20 个）

**初始化**:
- `App.kt` — `SingletonSketch.Factory` + 自动注册解码器（移除手动 GIF/WebP/HEIF 注册）+ OkHttp 配置方式更新

**Compose 图片加载（10 个文件）**:
- `Images.kt`、`Avatars.kt`、`VideoPlayer.kt`、`PbContentRender.kt`、`ReplyPage.kt`、`EditProfileActivity.kt`、`AppThemePage.kt`、`MainActivityV2.kt`、`EmoticonManager.kt`、`PhotoViewActivity.kt`

**非 Compose（5 个文件）**:
- `ImageUtil.kt` — 最复杂：View 加载、下载、缓存清理全面迁移；`getPlaceHolder()` 返回 `EquitableDrawable`
- `ThemeUtil.kt`、`TranslucentThemeActivity.kt` — `ImageRequest` + `result.image.asDrawable()`
- `utils.kt` — `LoadRequest` → `ImageRequest`
- `ImageCacheUtil.kt` — 缓存 API 保持不变

**自定义组件**:
- `SketchBlurTransformation.kt` — 适配 Sketch 4 的 `Transformation` 接口（`Image` 代替 `Bitmap`）

---

## 10. LitePal → Room 数据库迁移

**影响**: 高 | **难度**: 高 | **状态**: ✅ 已完成

### 版本变更

| 组件 | 旧版本 | 新版本 |
|------|--------|--------|
| LitePal | 3.0.0 | **移除** |
| Room | — | 2.8.4 |

### 迁移概述

LitePal 3.0.0 是一个已不再维护的 SQLite ORM。本次迁移将其完全替换为 Google 官方推荐的 Room 数据库方案。Room 支持 KSP 编译时处理、编译时 SQL 验证、协程/Flow 原生集成、Hilt 依赖注入。

核心工作包括：
1. 将 7 个 `LitePalSupport` 子类转换为 Room `@Entity`
2. 创建 7 个 DAO 接口
3. 创建 `AppDatabase`、数据迁移脚本（v37→v38）和 Hilt DI 模块
4. 替换 11 个文件中的所有 LitePal API 调用
5. 移除 LitePal 依赖和配置文件

### 关键技术决策

**列名映射**：LitePal 默认将 camelCase 字段名映射为全小写列名。所有 Room Entity 的 `@ColumnInfo` 注解都必须匹配 LitePal 的实际列名：
- `sToken` → `@ColumnInfo(name = "stoken")`
- `nameShow` → `@ColumnInfo(name = "nameshow")`
- `forumId` → `@ColumnInfo(name = "forumid")`
- `forumName` → `@ColumnInfo(name = "forumname")`
- 等

**数据迁移 (v37→v38)**：LitePal 和 Room 都使用 SQLite，Room 直接接管 LitePal 创建的 `tblite.db` 数据库。迁移脚本的主要工作是：
- 清理可能存在的重复数据
- 创建 Room 所需的唯一索引（`draft.hash`、`topforum.forumid`、`searchhistory.content`、`searchposthistory.content`）

**Account 的 saveOrUpdate**：LitePal 的 `saveOrUpdate("uid = ?", uid)` 在更新时保留原始 `id`。由于 `id` 存储在 SharedPreferences 中用于标识当前账户，不能使用 Room 的 `REPLACE`（会改变 id）。改为 DAO 中的 `@Transaction` 方法：先查询 → 如果存在则 `update`（保留 id）→ 否则 `insert`。

**异步 API 迁移**：LitePal 的回调式异步 API（`.saveOrUpdateAsync().listen{}`、`.findAllAsync().listen{}`）全部转换为协程（`suspend` 函数 + `GlobalScope.launch(Dispatchers.IO)`）或 `Flow`。

**Java 实体转 Kotlin**：`Draft.java` 和 `TopForum.java` 两个 Java 实体类转换为 Kotlin data class。

**Hilt 集成**：通过 `DatabaseModule`（`@InstallIn(SingletonComponent::class)`）提供 `AppDatabase` 单例和各 DAO。`App.kt` 使用 `@Inject lateinit var database: AppDatabase` 注入数据库实例，在 `onCreate` 中初始化各工具类的 DAO 引用。

### 新建文件

| 文件 | 说明 |
|------|------|
| `models/database/AppDatabase.kt` | Room Database 定义，version=38 |
| `models/database/Migrations.kt` | v37→v38 迁移脚本，创建唯一索引 |
| `models/database/DatabaseModule.kt` | Hilt Module，提供 Database + 7 个 DAO |
| `models/database/dao/AccountDao.kt` | Account DAO，含 `@Transaction saveOrUpdate` |
| `models/database/dao/BlockDao.kt` | Block DAO |
| `models/database/dao/DraftDao.kt` | Draft DAO |
| `models/database/dao/HistoryDao.kt` | History DAO，含分页查询 |
| `models/database/dao/SearchHistoryDao.kt` | SearchHistory DAO |
| `models/database/dao/SearchPostHistoryDao.kt` | SearchPostHistory DAO |
| `models/database/dao/TopForumDao.kt` | TopForum DAO |

### 修改的文件

**Entity 文件（7 个）**：
- `Account.kt` — 移除 `LitePalSupport`，添加 `@Entity` + `@ColumnInfo` + `@PrimaryKey`
- `Block.kt` — 同上
- `History.kt` — 同上
- `SearchHistory.kt` — 添加 `@Index(unique=true)` on content
- `SearchPostHistory.kt` — 添加 `@Index(unique=true)` on content
- `Draft.java` → `Draft.kt` — 重写为 Kotlin data class + Room Entity
- `TopForum.java` → `TopForum.kt` — 重写为 Kotlin data class + Room Entity

**API 调用替换（11 个文件）**：
- `App.kt` — `LitePal.initialize(this)` → Hilt 注入 + DAO 初始化
- `AccountUtil.kt` — 所有 `findAll`/`saveOrUpdateAsync`/`where().findFirst`/`update`/`delete` → DAO 方法
- `HistoryUtil.kt` — 所有 `find`/`save`/`update`/`deleteAll` + `LitePalFlowExt` → DAO 方法
- `BlockManager.kt` — `save`/`saveAsync`/`delete`/`findAllAsync` → DAO 方法
- `SearchViewModel.kt` — 添加 `@HiltViewModel` + 注入 `AppDatabase`；LitePal 调用 → DAO
- `HomeViewModel.kt` — 添加 `@HiltViewModel` + 注入 `AppDatabase`；LitePal 调用 → DAO
- `ForumSearchPostViewModel.kt` — 注入 `AppDatabase`；LitePal 调用 → DAO
- `HistoryListViewModel.kt` — 注入 `AppDatabase`；LitePal 调用 → DAO
- `ReplyPage.kt` — Draft 操作通过 `viewModel.database.draftDao()` 访问
- `ReplyViewModel.kt` — 注入 `AppDatabase`
- `UserViewModel.kt` / `EditProfileViewModel.kt` — `updateAll("uid = ?", uid)` → `accountDao.saveOrUpdate(account)`

**删除的文件**：
- `app/src/main/assets/litepal.xml`
- `utils/extension/LitePalFlowExt.kt`

### 构建错误及修复

1. **`Conflicting import: 'BlockManager' is ambiguous`** — `App.kt` 中重复导入 `BlockManager`，移除多余的 import
2. **`Argument type mismatch` in `HomeViewModel.kt`** — `flow {}` 类型推断问题，`flow` 块只有 `Success` 类型的 emit 导致 `catch` 中的 `Failure` 类型不匹配。解决：显式指定 `flow<HomePartialChange>` 类型参数
3. **`Unresolved reference 'updateAll'`** — `UserViewModel.kt` 和 `EditProfileViewModel.kt` 中直接调用了 `LitePalSupport.updateAll()` 实例方法，未在初次扫描中发现。替换为 `AccountUtil.accountDao.saveOrUpdate(account)`

---

## 11. 升级 Retrofit

**影响**: 低 | **难度**: 低 | **状态**: ✅ 已完成

### 版本变更

```toml
retrofit = "2.12.0"  # 2.9.0 → 2.12.0
```

选择 2.12.0 而非 3.0.0：Retrofit 3.0.0 引入了对 OkHttp 4.12 的传递性 Kotlin 依赖，虽然项目已使用 Kotlin，但为了降低风险，先升级到 2.12.0。

### 兼容性验证

- `converter-wire` 版本与 `retrofit` 版本一致（同为 2.12.0），兼容
- 项目自定义的 `GsonConverterFactory.java`（fork 版）仅使用 Retrofit 公共 API（`Converter`、`Converter.Factory`），与 2.12.0 完全兼容
- 无需修改任何代码，仅更新版本号即可

### 修改的文件

- `gradle/libs.versions.toml` — `retrofit = "2.9.0"` → `"2.12.0"`

---

## 12. Java → Kotlin 转换

**影响**: 中 | **难度**: 中高 | **前置依赖**: #6 (ButterKnife 移除) | **状态**: ✅ 已完成

### 版本变更

- Java 文件：106 个 → 0 个（Phase 10 已转换 Draft.java 和 TopForum.java）
- 项目语言：Java + Kotlin 混合 → **100% Kotlin**

### 转换概览

全部 106 个 Java 文件已转换为惯用 Kotlin，分 7 批并行处理：

| 批次 | 目录 | 文件数 | 转换要点 |
|------|------|--------|---------|
| 1 | `interfaces/` | 8 | SAM 接口 → `fun interface` |
| 1 | `models/` | 4 | Gson POJO → `open class` / `data class` |
| 1 | `api/caster/` | 2 | 属性访问适配，空安全处理 |
| 2 | `api/models/` + `web/` | 10 | Gson 模型，保留 `@SerializedName`、继承关系 |
| 3 | `api/adapters/` | 11 | Gson TypeAdapter，`when` 表达式替代 `if-else` |
| 3 | `api/retrofit/converter/gson/` | 3 | `@JvmStatic` 工厂方法，`@Throws` 注解 |
| 4 | `fragments/` | 2 | 生命周期方法保留 |
| 4 | `services/` + `activities/` + `adapters/` | 3 | 简单直转 |
| 5 | `utils/` | 17 | 工具类 → `object` + `@JvmStatic` |
| 5 | `utils/helios/` | 8 | 位运算：`ushr`/`shl`/`xor`/`and`/`or`/`inv` |
| 6 | `components/` | 16 | Dialog、Glide、Span、Blur 算法 |
| 7 | `ui/common/theme/` | 6 | 主题接口 + 工具类 |
| 7 | `ui/widgets/` | 16 | Tint* View 子类 → `@JvmOverloads constructor` |

### 主要转换策略

- **零 `!!` 原则**：全部使用 `?.`、`?:`、`let`、`as?` 等安全调用
- **Java 互操作**：静态成员用 `@JvmStatic` / `@JvmField` / `const val`
- **`fun interface`**：8 个 SAM 接口全部标注
- **`object`**：纯工具类（`DisplayUtil`、`ColorUtils` 等）转为单例对象
- **`@JvmOverloads constructor`**：所有 View 子类（14 个 Tint* 组件）
- **位运算保真**：`utils/helios/` 的 XXHash 算法中所有 `>>>` → `ushr`，`&`/`|`/`^`/`~` → `and`/`or`/`xor`/`inv`

### 构建修复

转换后修复了以下编译问题：

| 问题 | 影响文件 | 修复方式 |
|------|---------|---------|
| 私有属性访问 | `ForumBeanCaster.kt` | 改用 setter 方法（Kotlin 中 `@SerializedName` 属性带 `private set`） |
| `data class` 不可继承 | `ErrorBean.kt` | 改为 `open class` |
| `setOnGrantedCallback` 不存在 | `WebViewPage.kt` | 改为属性赋值 `onGrantedCallback = ...` |
| `HORIZONTAL` 静态常量 | `TranslucentThemeActivity.kt` | `MyLinearLayoutManager.HORIZONTAL` → `LinearLayoutManager.HORIZONTAL` |
| `MediaType.get()` 废弃 | `GsonRequestBodyConverter.kt` | `"...".toMediaType()` |
| `XXHash` 方法可见性 | `XXHash.kt` + `XXHashEncoder.kt` | `protected` → `internal` |
| `Bitmap.Config?` 空安全 | `FastBlur.kt`(2), `Util.kt` | `?: Bitmap.Config.ARGB_8888` |
| 多处 `Context?`/`String?` 空安全 | 6 个文件 | 添加安全调用或默认值 |

---

## 13. 最终清理

**影响**: 中 | **难度**: 低 | **前置依赖**: 所有前置阶段完成 | **状态**: ✅ 已完成

### 已完成项

#### 13.1 移除 Jetifier

- `gradle.properties`：移除 `android.enableJetifier=true`
- 验证：`./gradlew dependencies` 无 `com.android.support` 依赖

#### 13.2 启用 nonTransitiveRClass

- `gradle.properties`：`android.nonTransitiveRClass=false` → `true`
- 修复了 8 处第三方库 R 资源引用：

| 文件 | 原引用 | 修复为 |
|------|--------|--------|
| `App.kt` | `R.string.appcenter_distribute_*` | `com.microsoft.appcenter.distribute.R.string.*` |
| `BaseBottomSheetDialogFragment.kt` | `R.id.design_bottom_sheet` (2处) | `com.google.android.material.R.id.*` |
| `FullScreen.kt` | `R.id.compose_view_saveable_id_tag` | `androidx.compose.ui.R.id.*` |
| `TintCheckBox.kt` | `R.attr.checkboxStyle` | `androidx.appcompat.R.attr.*` |
| `TintToolbar.kt` | `R.attr.toolbarStyle` | `androidx.appcompat.R.attr.*` |
| `Util.kt` | `R.id.snackbar_action/text` | `com.google.android.material.R.id.*` |
| `utils.kt` | `R.attr.colorControlHighlight` (2处) | `androidx.appcompat.R.attr.*` |
| `ImagePicker.kt` | `R.style.Matisse_Dracula/Zhihu` | `com.zhihu.matisse.R.style.*` |

#### 13.3 启用 nonFinalResIds

- `gradle.properties`：新增 `android.nonFinalResIds=true`
- 验证：项目中无 `when` 语句使用 R 常量（Java `switch-case` 已全部转为 Kotlin）

#### 13.4 清理 TOML 残留条目

- 移除 `kotlin-kapt` 插件定义（kapt 已在 Phase 6 移除）
- 移除 `butterknife` 和 `butterknife-compiler` 库定义（ButterKnife 已在 Phase 6 移除）

#### 13.5 最后架构收口（稳定性）

- `DatabaseModule.kt`：移除 `allowMainThreadQueries()`，强制 Room 走后台线程
- `app/build.gradle.kts`：新增 `ksp` 参数 `room.schemaLocation="$projectDir/schemas"`，落盘 schema 版本历史
- 协程作用域统一：新增 `App.appScope`（`SupervisorJob + Dispatchers.Default`），替换 `GlobalScope`
- 移除阻塞调用：`AccountUtil`、`HistoryUtil` 中 `runBlocking` 全部清理
- 并发安全：`BlockManager` 增加列表锁与快照读取，避免并发读写竞态
- DAO 能力补齐：新增 `AccountDao.updateSync()`、`HistoryDao.getAllSync()/getByTypeSync()`

### 暂未执行项

| 项目 | 原因 |
|------|------|
| 移除 ImmersionBar | View 系 Activity（`BaseActivity`、`TranslucentThemeActivity`）仍在使用 |
| 统一 Glide/Sketch | Glide 用于 Matisse 图片选择器引擎，Sketch 用于主界面图片加载，各有职责 |
| 清理 XML 布局 | 需要将剩余 View 系 Activity 迁移到 Compose，属于功能重构范畴 |

---

## 实施优先级

| 阶段 | 优化项 | 预期收益 | 风险 | 工作量 | 前置依赖 |
|------|--------|---------|------|--------|---------|
| **1** | 迁移 SweetDependency → TOML | 解除后续升级阻塞 | 中 | 中 | 无 |
| **2** | 升级 JDK 11 → 17 | 解锁 Gradle 9 / AGP 9 | 低 | 低 | 无 |
| **3** | 升级 Gradle 9.3.1 + AGP 9.0.1 | 现代构建工具链 | 高 | 高 | #1, #2 |
| **4** | 升级 Kotlin 2.3.10 + KSP | K2 编译器、新 Compose Compiler | 中高 | 中 | #3 |
| **5** | Hilt 升级 + kapt → KSP | 更快的编译速度 | 中 | 中 | #4 |
| **6** | 移除 ButterKnife + kapt 插件 | 移除遗留依赖 | 低 | 低 | #5 |
| **7** | Compose BOM + Destinations 2.x | 现代导航框架 | 中高 | 高 | #4 |
| **8** | 替换 Accompanist 废弃 API | 移除废弃依赖 | 中 | 中 | #7 |
| **9** | Sketch 3 → 4 | 现代图片加载 | 中高 | 中高 | #4 |
| **10** | LitePal → Room | 现代数据库 + 编译时验证 | 高 | 高 | #4, #5 |
| **11** | 升级 Retrofit | 新功能支持 | 低 | 低 | 无 |
| **12** | Java → Kotlin 转换 | 代码一致性 | 中 | 高(108 文件) | #6 |
| **13** | 最终清理 | APK 体积、构建性能 | 低 | 低 | 全部 |
| **14** | 底层架构收尾 | 版本对齐、废弃 API 清理 | 低 | 中 | #13 |

---

## 14. 底层架构收尾

**影响**: 中 | **难度**: 中 | **前置依赖**: 所有前置阶段完成 | **状态**: ✅ 已完成

### 完成概要

对项目进行最后一轮底层架构清理，覆盖 AndroidX 核心库版本对齐、废弃 API 替换、协程统一、DataStore 优化等 8 个领域。

### 14.1 AndroidX 核心库版本升级

多个 AndroidX 基础库停留在 2023 年末版本，与 Compose BOM 2026.01.01 存在约 2 年差距。`navigation-compose` 2.7.6 与 Compose Destinations 2.2.0 要求的 2.8.9 不匹配。

| 组件 | 旧版本 | 新版本 |
|------|--------|--------|
| lifecycle | 2.7.0 | 2.8.7 |
| activity | 1.8.2 | 1.12.4 |
| navigation-compose | 2.7.6 | 2.8.9 |
| media3 | 1.2.1 | 1.6.0 |
| core-ktx | 1.12.0 | 1.15.0 |
| appcompat | 1.6.1 | 1.7.0 |
| datastore-preferences | 1.0.0 | 1.1.1 |
| material | 1.11.0 | 1.12.0 |
| annotation | 1.7.1 | 1.9.1 |
| browser | 1.7.0 | 1.8.0 |
| constraintlayout | 2.1.4 | 2.2.0 |
| constraintlayout-compose | 1.0.1 | 1.1.0 |
| window | 1.2.0 | 1.3.0 |

新增 `lifecycle-runtime-compose` 依赖，解锁 `collectAsStateWithLifecycle` API。

### 14.2 ProGuard 清理

移除已不存在的 LitePal 混淆规则（`proguard-rules.pro` 中 `org.litepal.**` 相关 keep 规则）。

### 14.3 移除冗余依赖

- 移除 `kotlin-stdlib` 显式依赖（AGP 9 + Kotlin 2.3.10 自动提供）
- 保留 `kotlin-reflect`（3 处实际使用：`MonetTestPage.kt`、`AppPreferencesUtils.kt`、`arch/Extensions.kt`）

### 14.4 thread {} → 协程

| 文件 | 变更 |
|------|------|
| `MoreSettingsPage.kt` | `LaunchedEffect` 内 `thread {}` → `withContext(Dispatchers.IO)` |
| `ImageCacheUtil.kt` | `clearImageDiskCache`/`clearImageAllCache` 改为 `suspend fun`，移除主线程判断和 `thread {}`，统一使用 `withContext(Dispatchers.IO)` |

### 14.5 resources.updateConfiguration() → createConfigurationContext()

`App.kt` 和 `BaseActivity.kt` 中已废弃的 `resources.updateConfiguration()` 替换为 `attachBaseContext()` + `createConfigurationContext()` 模式。字体缩放配置从每次 `getResources()` 调用时应用改为在 `attachBaseContext` 中一次性应用。

### 14.6 测试文件 Java → Kotlin

`ExampleUnitTest.java` 和 `ExampleInstrumentedTest.java` 转换为 Kotlin，项目现为完全 100% Kotlin（含测试代码）。

### 14.7 Handler/Looper → 协程

| 文件 | 变更 |
|------|------|
| `MainActivityV2.kt` | `Handler.postDelayed(100)` → `lifecycleScope.launch { delay(100); ... }` |
| `EditProfileActivity.kt` | `Handler.post {}` → `lifecycleScope.launch {}` |

保留 `VoicePlayerView.kt`、`TiebaLiteJavaScript.kt`、`PermissionUtils.kt` 中的 Handler 用法（JS Bridge / 第三方库回调场景）。

### 14.8 DataStore runBlocking 优化

- 精简 7 个同步 get 函数的实现（减少冗余代码）
- 新增 7 个 `suspend` 版本（`suspendGetInt`、`suspendGetString` 等），使用 `data.first()` 而非 `runBlocking`
- 删除未使用的 `DataStorePreference` 类（约 80 行）
- 保留同步版本供 `AppPreferencesUtils` 的 `ReadWriteProperty` 和 `attachBaseContext` 字体缩放等必须同步的场景使用

### 修改的文件

| 文件 | 变更类型 |
|------|---------|
| `gradle/libs.versions.toml` | 版本升级 + 新增 lifecycle-runtime-compose + 移除 kotlin-stdlib |
| `app/build.gradle.kts` | 新增 lifecycle-runtime-compose 依赖 + 移除 kotlin-stdlib |
| `app/proguard-rules.pro` | 移除 LitePal 规则 |
| `App.kt` | `getResources()` → `attachBaseContext()` |
| `BaseActivity.kt` | `getResources()` → `attachBaseContext()` |
| `ImageCacheUtil.kt` | `thread {}` → `suspend fun` + `withContext` |
| `MoreSettingsPage.kt` | `thread {}` → `withContext(Dispatchers.IO)` |
| `MainActivityV2.kt` | Handler → `lifecycleScope.launch` |
| `EditProfileActivity.kt` | Handler → `lifecycleScope.launch` |
| `DataStore.kt` | 精简同步 get + 新增 suspend 版本 + 删除 DataStorePreference |
| `ExampleUnitTest.java` → `.kt` | Java → Kotlin |
| `ExampleInstrumentedTest.java` → `.kt` | Java → Kotlin |

---

## 风险总结

| 风险项 | 严重程度 | 缓解措施 |
|--------|---------|---------|
| SweetDependency 不兼容 Gradle 9 | **阻塞** | 必须先迁移到 TOML |
| AGP 9.0 移除旧 Variant API | **高** | 提前准备 `androidComponents` 迁移代码 |
| K2 编译器暴露新类型错误 | **中** | 升级后全量编译，逐个修复 |
| LitePal → Room 数据迁移丢失 | **已解决** | `@ColumnInfo` 完全匹配 LitePal 小写列名；Migration 37→38 创建唯一索引并清理重复数据；Account 的 saveOrUpdate 使用 `@Transaction` 保留原始 id |
| Compose Destinations 2.x 与 KSP2 兼容性 | **已解决** | 2.2.0 修复了 KSP2 兼容性，迁移成功 |
| Sketch 4 API 不兼容 | **已解决** | ~20 文件迁移完成，核心变化：请求类型统一为 ImageRequest、Compose 组件包路径变更、StateImage/DrawableFetcher/EquitableDrawable 新体系 |
| Hilt KSP Alpha 状态 | **低** | 实际广泛使用，核心功能稳定 |
| 第三方库不兼容新 Gradle/AGP/Kotlin | **中** | Wire、dependency-analysis 等需逐一验证 |

---

## 参考资源

- [Gradle 9.x Release Notes](https://docs.gradle.org/9.3.1/release-notes.html)
- [AGP 9.0.1 Release Notes](https://developer.android.google.cn/studio/releases/gradle-plugin)
- [Kotlin 2.3.10 What's New](https://kotlinlang.org/docs/whatsnew23.html)
- [Compose Destinations v2 迁移指南](https://composedestinations.rafaelcosta.xyz/migrating-to-v2)
- [Sketch 4 文档](https://panpf.github.io/sketch/)
- [Room 迁移指南](https://developer.android.com/training/data-storage/room/sqlite-room-migration)
- [Dagger KSP 指南](https://dagger.dev/dev-guide/ksp.html)
- [Accompanist 废弃状态](https://google.github.io/accompanist/)
