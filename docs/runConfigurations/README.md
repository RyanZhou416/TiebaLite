# 运行配置说明

## 为什么选 Release 运行后版本号还是 debug？

部分 Android Studio 版本会**忽略**运行配置里保存的「构建变体」，实际安装的是 **Build Variants 面板**里当前选中的变体。

### 做法一：运行前先切 Build Variant（推荐）

用 **「Release (保留数据)」** 时：

1. 菜单 **Build → Select Build Variant**（或左侧 **Build Variants** 工具窗口）
2. 在 **app** 模块那一行，把 **Active Build Variant** 从 `debug` 改成 **`release`**
3. 再在运行配置里选 **「Release (保留数据)」** 点 Run

这样安装的就是 release，版本号会带 `-release`。

用完要开发调试时，把 Build Variant 改回 `debug` 即可。

### 做法二：用 Gradle 安装 Release（一定装 release）

若希望不依赖 Build Variants 面板、每次都能装上 release，可手动加一个 **Gradle** 运行配置：

1. **Run → Edit Configurations → + → Gradle**
2. **Name** 填：`Install Release`
3. **Tasks** 填：`:app:installRelease`
4. **Gradle project** 选当前项目（TiebaLite）
5. 勾选 **Store as project file**，可选保存到 `.run/Install_Release.run.xml`
6. Apply → OK

之后在运行配置里选 **Install Release** 再 Run，会执行 `:app:installRelease`，**一定会**构建并安装 release。安装后需在手机上手动打开应用。

## 配置与版本号

| 配置               | 构建变体 | 版本号后缀  |
|--------------------|----------|-------------|
| Debug / app        | debug    | `-debug`    |
| Release (保留数据) | release  | `-release`  |
