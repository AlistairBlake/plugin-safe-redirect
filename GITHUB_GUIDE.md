# GitHub 上传指南

本文档介绍如何将 Halo 安全跳转助手插件上传到 GitHub。

## 📦 准备工作

### 1. 确认文件结构

项目已整理完成，当前文件结构：

```
plugin-starter/
├── .github/
│   └── workflows/
│       └── ci.yaml                    # GitHub Actions CI 配置
├── .editorconfig                      # 编辑器配置
├── .gitignore                        # Git 忽略文件
├── build.gradle                      # Gradle 构建配置
├── CUSTOM_CSS_GUIDE.md               # 自定义 CSS 指南
├── CUSTOM_HTML_EXAMPLE.md            # 自定义 HTML 示例
├── gradle.properties                 # Gradle 属性
├── gradlew                           # Gradle 包装器（Linux/Mac）
├── gradlew.bat                       # Gradle 包装器（Windows）
├── LICENSE                           # GPL-3.0 许可证
├── logo.png                          # 插件图标（PNG）
├── logo.svg                          # 插件图标（SVG，推荐）
├── README.md                         # 项目说明
├── settings.gradle                   # Gradle 设置
├── gradle/                           # Gradle 包装器文件
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    └── main/
        ├── java/run/halo/saferedirect/
        │   ├── SafeRedirectPlugin.java      # 插件主类
        │   ├── SafeRedirectRouter.java      # 路由
        │   ├── SafeRedirectHeadProcessor.java # 前端注入
        │   ├── SafeRedirectConfig.java      # 配置
        │   ├── BasicSetting.java            # 基础设置
        │   ├── StyleSetting.java            # 样式设置
        │   └── AdvancedSetting.java        # 高级设置
        └── resources/
            ├── plugin.yaml                  # 插件清单
            ├── logo.svg                     # 插件图标
            ├── logo.png                     # 插件图标
            └── extensions/
                └── settings.yaml            # 设置定义
```

### 2. 已删除的文件

- ✅ `build/` - 构建产物
- ✅ `.gradle/` - Gradle 缓存
- ✅ `.workbuddy/` - WorkBuddy 工作区
- ✅ `ui/` - 前端代码（已禁用）
- ✅ `jar_temp/` - 临时文件
- ✅ `workplace/` - 临时文件

## 🚀 上传到 GitHub

### 方式一：通过 GitHub 网页端

1. **创建新仓库**
   - 访问 https://github.com/new
   - 仓库名称：`plugin-safe-redirect`（或自定义名称）
   - 描述：`为 Halo 博客系统提供安全的外部链接跳转中间页`
   - 设置为 Public 或 Private（根据需求）
   - 勾选"Initialize this repository with a README"
   - 点击"Create repository"

2. **上传文件**
   - 进入仓库页面
   - 点击"uploading an existing file"链接
   - 将项目文件夹拖拽到浏览器中
   - 等待上传完成
   - 在底部填写提交信息：
     - `feat: 初始化安全跳转助手插件 v1.0.0`
   - 点击"Commit changes"

### 方式二：通过 Git 命令行（推荐）

1. **初始化 Git 仓库**

```bash
cd e:\1\halo链接跳转\plugin-starter
git init
```

2. **添加远程仓库**

```bash
git remote add origin https://github.com/你的用户名/plugin-safe-redirect.git
# 或者使用 SSH
git remote add origin git@github.com:你的用户名/plugin-safe-redirect.git
```

3. **添加所有文件**

```bash
git add .
```

4. **提交**

```bash
git commit -m "feat: 初始化安全跳转助手插件 v1.0.0"
```

5. **推送到 GitHub**

```bash
git branch -M main
git push -u origin main
```

## 📝 提交信息规范

使用语义化提交信息（Conventional Commits）：

- `feat:` 新功能
- `fix:` 修复 Bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 重构
- `test:` 测试相关
- `chore:` 构建/工具相关

示例：
```bash
git commit -m "feat: 添加白名单功能"
git commit -m "fix: 修复白名单跳转失败的问题"
git commit -m "docs: 更新 README 安装说明"
```

## 🏷️ 发布版本

### 1. 创建 Release

1. 进入 GitHub 仓库页面
2. 点击右侧"Releases" -> "Create a new release"
3. 填写信息：
   - **Tag version**: `v1.0.0`
   - **Release title**: `v1.0.0 - 初始版本发布`
   - **Description**:
     ```markdown
     ## 新功能

     - ✅ 5 种内置主题
     - ✅ 域名白名单功能
     - ✅ 二维码生成
     - ✅ 倒计时跳转
     - ✅ 完全自定义 HTML 页面
     - ✅ 粒子背景动画

     ## 安装

     1. 下载 `plugin-starter-1.0.0-SNAPSHOT.jar`
     2. 在 Halo 后台上传安装
     ```

### 2. 上传构建产物

**方法一：在 GitHub Release 中上传**

1. 在创建 Release 页面底部
2. 拖拽 `build/libs/plugin-starter-1.0.0-SNAPSHOT.jar` 文件
3. 点击"Publish release"

**方法二：通过 GitHub Actions 自动构建**

可以配置 CI/CD 自动构建和发布 Release（已包含 `.github/workflows/ci.yaml`）

## 🔐 保护敏感信息

确保以下文件未被提交到 Git（已在 `.gitignore` 中）：

- `build/` - 构建产物
- `.gradle/` - Gradle 缓存
- `*.log` - 日志文件
- `.env` - 环境变量（如果有）

检查命令：
```bash
git status
```

## 📌 后续维护

### 添加贡献指南

创建 `CONTRIBUTING.md`：

```markdown
# 贡献指南

感谢你对 Halo 安全跳转助手的关注！

## 开发

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'feat: 添加某功能'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

## 代码规范

- 使用 Java 21
- 遵循 Google Java Style Guide
- 添加必要的注释和文档

## 测试

确保所有功能正常工作后再提交 PR。
```

### 添加许可证说明

在 README 中添加使用说明：

```markdown
## 开源协议

本项目采用 GPL-3.0 开源协议，详见 [LICENSE](LICENSE) 文件。

### 使用许可

- ✅ 个人使用
- ✅ 商业使用
- ✅ 修改和分发
- ✅ 私人使用

### 条件

- ℹ️ 必须开源修改后的代码
- ℹ️ 必须保留原始许可证
- ℹ️ 不得移除原作者的版权声明
```

## 🎯 推荐的 GitHub 设置

1. **启用 Issues**：收集用户反馈和 Bug 报告
2. **启用 Discussions**：社区交流
3. **启用 Wiki**：详细文档
4. **设置 Labels**：分类管理 Issues
   - `bug` - Bug 报告
   - `enhancement` - 功能增强
   - `documentation` - 文档
   - `help wanted` - 需要帮助
5. **添加 Topics**：
   - `halo`
   - `halo-plugin`
   - `security`
   - `redirect`

## 📊 统计数据

上传后可以在 GitHub 查看项目统计数据：
- Stars 数量
- Fork 数量
- 克隆次数
- 访问者统计

## 🎉 完成

项目已成功上传到 GitHub！

后续更新：
```bash
# 修改代码后
git add .
git commit -m "fix: 修复某问题"
git push origin main
```

发布新版本：
1. 创建新 Tag：`git tag v1.0.1`
2. 推送 Tag：`git push origin v1.0.1`
3. 在 GitHub 创建新的 Release
