# Halo 安全跳转助手

为 Halo 博客系统提供安全的外部链接跳转中间页，有效防止钓鱼攻击，支持完全自定义样式和 HTML。

## ✨ 功能特性

- 🔒 **安全跳转**：拦截所有外部链接，防止钓鱼网站
- 🎨 **5 种内置主题**：默认、极简、科技、温暖、自定义
- 🖼️ **自定义图标**：支持自定义图片 URL 或使用内置 SVG 图标
- ⏱️ **倒计时跳转**：可配置自动跳转时间
- 📱 **二维码生成**：自动生成跳转链接二维码
- 📋 **域名白名单**：白名单域名直接跳转，无需中间页
- 🎯 **完全自定义**：支持自定义整个跳转页面 HTML
- ✨ **粒子背景动画**：动态粒子效果，提升视觉体验
- 📊 **外链追踪**：可选的外链点击统计功能

## 📸 截图

### 默认主题
![默认主题](screenshot-default.png)

### 极简主题
![极简主题](screenshot-minimal.png)

### 科技主题
![科技主题](screenshot-tech.png)

### 温暖主题
![温暖主题](screenshot-warm.png)

### 后台设置
![后台设置](screenshot-settings.png)

## 🚀 快速开始

### 环境要求

- Java 21（JDK 21）
- Halo 2.17.0 或更高版本
- Gradle 8.9（已包含）

### 安装方式

1. 下载最新的插件发布包
2. 在 Halo 后台进入 `插件` -> `安装`
3. 上传 `.jar` 文件
4. 启用插件

### 构建插件

```bash
# Windows
$env:JAVA_HOME = "E:\Java\jdk21"
$env:Path = "E:\Java\jdk21\bin;" + $env:Path
$env:GRADLE_OPTS = "-Dfile.encoding=UTF-8"
.\gradlew.bat clean build --no-daemon

# Linux / macOS
export JAVA_HOME=/path/to/jdk21
./gradlew clean build --no-daemon
```

构建产物位于：`build/libs/plugin-starter-1.0.0-SNAPSHOT.jar`

## ⚙️ 配置说明

插件在 Halo 后台的 `插件设置` 中进行配置，分为三个设置组：

### 基础设置

- **启用插件**：控制插件是否生效
- **页面标题**：跳转页面的标题
- **网站名称**：显示在跳转页面中
- **域名白名单**：白名单域名直接跳转（一行一个）
- **跳转倒计时**：自动跳转等待时间（秒）
- **启用二维码**：是否显示跳转链接二维码

### 样式设置

- **主题选择**：
  - `default` - 默认主题（渐变背景 + 粒子动画）
  - `minimal` - 极简主题（纯白背景，简洁设计）
  - `tech` - 科技主题（深色背景 + 霓虹效果）
  - `warm` - 温暖主题（暖色调背景）
  - `custom` - 自定义主题
- **自定义 CSS**：自定义样式（仅在自定义主题生效）
- **自定义图标**：自定义图标图片 URL（留空使用内置图标）
- **自定义整个跳转页面**：完全替换默认跳转页面

### 高级设置

- **启用外链追踪**：记录外链点击日志

## 🎨 自定义 HTML

插件支持完全自定义跳转页面，查看 [CUSTOM_HTML_EXAMPLE.md](CUSTOM_HTML_EXAMPLE.md) 获取详细示例。

### 变量说明

- `{url}` - 目标跳转链接
- `{sitename}` - 网站名称
- `{encodedurl}` - URL 编码后的链接

### 示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>跳转中 - {sitename}</title>
  <style>
    body { background: #f5f5f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
    .container { background: white; padding: 40px; border-radius: 8px; text-align: center; }
  </style>
</head>
<body>
  <div class="container">
    <h1>正在跳转...</h1>
    <p>{url}</p>
    <a href="{url}">立即跳转</a>
  </div>
</body>
</html>
```

## 🛠️ 开发

### 项目结构

```
plugin-starter/
├── src/main/
│   ├── java/run/halo/saferedirect/
│   │   ├── SafeRedirectPlugin.java      # 插件主类
│   │   ├── SafeRedirectRouter.java      # 跳转路由
│   │   ├── SafeRedirectHeadProcessor.java # 前端 JS 注入
│   │   ├── SafeRedirectConfig.java      # 配置类
│   │   ├── BasicSetting.java            # 基础设置
│   │   ├── StyleSetting.java            # 样式设置
│   │   └── AdvancedSetting.java        # 高级设置
│   └── resources/
│       ├── plugin.yaml                  # 插件清单
│       ├── logo.svg                     # 插件图标
│       └── extensions/
│           └── settings.yaml            # 设置定义
├── gradle/                             # Gradle 包装器
├── build.gradle                         # 构建配置
├── settings.gradle                      # 项目设置
├── LICENSE                             # 许可证
└── README.md                           # 项目说明
```

### 核心功能

1. **链接拦截**：通过 `SafeRedirectHeadProcessor` 向页面 head 注入 JS，拦截所有外部链接
2. **安全跳转**：`SafeRedirectRouter` 提供安全跳转中间页
3. **白名单检查**：`isWhitelisted()` 方法检查域名是否在白名单中
4. **主题系统**：`buildThemeStyles()` 动态生成不同主题的 CSS
5. **自定义页面**：`buildCustomPage()` 处理完全自定义的 HTML 页面

### 技术栈

- **后端**：Spring WebFlux、Halo Plugin SDK
- **前端**：原生 JavaScript（无框架依赖）、Canvas 粒子动画
- **构建**：Gradle 8.9、Java 21

## 📝 更新日志

### v1.0.0 (2026-03-30)

- ✅ 初始版本发布
- ✅ 5 种内置主题
- ✅ 域名白名单功能
- ✅ 二维码生成
- ✅ 倒计时跳转
- ✅ 完全自定义 HTML 页面
- ✅ 粒子背景动画

## 🔧 故障排查

### 插件不生效

1. 检查插件是否在后台启用
2. 检查主题是否正确设置了外链拦截
3. 查看 Halo 日志是否有错误信息

### 白名单不工作

1. 确认域名格式正确（如 `example.com`）
2. 域名需要一行一个，不要用逗号分隔
3. 查看日志确认白名单匹配结果

### 自定义 HTML 不显示

1. 确认 HTML 代码完整（包含 `<!DOCTYPE html>`、`<html>` 等标签）
2. 检查变量是否正确使用（`{url}`、`{sitename}`）
3. 查看浏览器控制台是否有 JS 错误

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

[GPL-3.0](LICENSE)

## 📮 联系方式

- 作者：小聂
- 主页：https://github.com/nieshilin/plugin-safe-redirect
- 问题反馈：https://github.com/nieshilin/plugin-safe-redirect/issues
