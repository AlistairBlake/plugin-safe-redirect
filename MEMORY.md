# Halo 安全跳转助手 - 长期记忆

## 项目信息

- **位置**：`e:\1\halo链接跳转\plugin-starter`
- **包名**：`run.halo.saferedirect`
- **Halo 版本**：2.17.0+
- **插件 ID**：`plugin-safe-redirect`
- **插件名称**：安全跳转助手
- **当前版本**：v1.0.0

## 核心功能

- 拦截文章外部链接 → 安全跳转中间页
- 5 种内置主题（默认、极简、科技、温暖、自定义）
- 自定义样式/图标/白名单/倒计时
- 完全自定义跳转页面 HTML
- 粒子背景动画
- 二维码生成
- 外链追踪

## 构建环境（重要）

- **JDK**：必须用 `E:\Java\jdk21`（JDK 21），不能用 JDK 17 或 JDK 25
- **构建命令**：
  ```powershell
  $env:JAVA_HOME = "E:\Java\jdk21"
  $env:Path = "E:\Java\jdk21\bin;" + $env:Path
  $env:GRADLE_OPTS = "-Dfile.encoding=UTF-8"
  .\gradlew.bat clean build --no-daemon
  ```
- **输出文件**：`build/libs/plugin-starter-1.0.0-SNAPSHOT.jar`
- **已删除**：前端 UI 代码（已禁用 buildFrontend）

## 项目结构

```
plugin-starter/
├── .github/workflows/ci.yaml       # GitHub Actions
├── src/main/
│   ├── java/run/halo/saferedirect/
│   │   ├── SafeRedirectPlugin.java      # 插件主类
│   │   ├── SafeRedirectRouter.java      # 路由（跳转页渲染）
│   │   ├── SafeRedirectHeadProcessor.java # 前端 JS 注入
│   │   ├── SafeRedirectConfig.java      # 配置类
│   │   ├── BasicSetting.java            # 基础设置
│   │   ├── StyleSetting.java            # 样式设置
│   │   └── AdvancedSetting.java        # 高级设置
│   └── resources/
│       ├── plugin.yaml                  # 插件清单（logo: logo.svg）
│       ├── logo.svg                     # 插件图标
│       ├── logo.png
│       └── extensions/settings.yaml     # 设置定义
├── build.gradle                      # 构建配置（已移除 Node.js）
├── gradle.properties                 # Gradle 属性
├── README.md                         # 项目说明（已更新）
├── CUSTOM_CSS_GUIDE.md               # 自定义 CSS 指南
├── CUSTOM_HTML_EXAMPLE.md            # 自定义 HTML 示例（5 个）
├── GITHUB_GUIDE.md                   # GitHub 上传指南
├── .gitignore                        # Git 忽略文件
└── LICENSE                           # GPL-3.0
```

## 技术要点

### 前端 JS 注入（SafeRedirectHeadProcessor）
- 拦截所有 `<a>` 标签
- 匹配外部链接（非当前域名）
- 重定向到 `/plugins/plugin-safe-redirect/go?url={encoded}`
- 支持白名单跳过拦截
- 使用 `data-no-redirect` 属性跳过特定链接

### 跳转路由（SafeRedirectRouter）
- 端点：`/plugins/plugin-safe-redirect/go?url={url}`
- 服务端渲染 HTML（无前端框架）
- 支持白名单直接跳转（302）
- 支持完全自定义 HTML 页面
- 5 种主题样式动态生成

### 主题系统
```java
// SafeRedirectRouter.java
private String buildThemeStyles(String theme) {
    switch (theme) {
        case "minimal": return "/* 极简主题 CSS */";
        case "tech": return "/* 科技主题 CSS */";
        case "warm": return "/* 温暖主题 CSS */";
        case "custom": return style.getCustomCss();
        default: return "/* 默认主题 CSS */";
    }
}
```

### 白名单检查
```java
// SafeRedirectRouter.java
private boolean isWhitelisted(String targetUrl, String whitelistDomains) {
    // 域名匹配：精确匹配或子域名匹配
    // example.com 匹配 example.com 和 sub.example.com
    // 大小写不敏感
}
```

### 自定义页面变量
- `{url}` - 目标链接（未编码）
- `{sitename}` - 网站名称
- `{encodedurl}` - URL 编码后的链接

## 编码规范

- **后端**：
  - 禁止使用 `System.out.println`
  - 必须使用 `@Slf4j` + `log.xxx()`
  - 响应式编程（WebFlux）

- **前端**：
  - 纯原生 JavaScript（无框架）
  - Canvas 粒子动画
  - 内联 CSS（约 100 行）

## 页面设计规范

### 粒子背景
- Canvas 渲染动态浮动粒子
- 粒子间自动连线（距离 < 150px）
- 粒子数量自适应屏幕尺寸

### 图标系统
- 支持自定义图片链接（通过设置 `iconUrl` 字段）
- 留空则使用内置 SVG 图标
- 图标容器：80x80 圆角矩形（16px），蓝紫渐变色

### 组件样式
- 卡片：白底、圆角 16px、阴影、毛玻璃效果
- 按钮：渐变主按钮 + 透明次按钮
- 倒计时：黄底、带图标的提示框
- 二维码：带边框、居中显示
- URL：灰色背景、等宽字体

## 已知问题和修复

| 问题 | 修复方案 |
|------|---------|
| 按钮图标不显示 | 改用 Unicode 符号（→、←） |
| 白名单跳转失败 | 添加异常处理，失败则显示中间页 |
| Console 界面残留 | 删除 `ui/` 目录和 `console/` 资源 |
| Logo 不更新 | 复制根目录 logo.svg 到 resources/ |
| 主题切换无效 | 实现 `buildThemeStyles()` 动态生成样式 |

## 文档清单

1. **README.md** - 项目说明、功能特性、安装使用
2. **CUSTOM_CSS_GUIDE.md** - 自定义 CSS 完整指南
3. **CUSTOM_HTML_EXAMPLE.md** - 5 个完整自定义 HTML 示例
4. **GITHUB_GUIDE.md** - GitHub 上传和发布指南

## GitHub 上传状态

- ✅ 项目已整理
- ✅ .gitignore 已创建
- ✅ README.md 已更新
- ✅ 构建配置已清理
- ✅ 文档已完善
- ⏸️ 待用户上传到 GitHub

## 用户偏好

- 使用 logo.svg 作为插件图标
- 不需要 Console 界面（已删除）
- 完全自定义 HTML 页面功能
- 域名白名单直接跳转
