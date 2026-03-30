# 自定义 HTML 代码示例

本文档提供多个自定义 HTML 代码示例，可以直接复制到插件设置中的"自定义整个跳转页面"字段。

## 注意事项

⚠️ **重要提示**：
1. 自定义 HTML 会**完全替换**默认跳转页面，不是追加到底部
2. 支持的变量替换：
   - `{url}` - 目标跳转链接
   - `{sitename}` - 网站名称
   - `{encodedurl}` - URL 编码后的链接（用于分享等）
3. 代码不会进行 HTML 转义，确保输入安全
4. 建议使用外部 CSS/JS 或内联样式，避免依赖外部资源

---

## 示例 1：极简风格跳转页

适合追求简洁的用户，无动画，纯文字。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>正在跳转 - {sitename}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 20px;
    }
    .container {
      max-width: 600px;
      width: 100%;
      background: white;
      padding: 40px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      text-align: center;
    }
    h1 { margin-bottom: 20px; color: #333; }
    .url-box {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      margin: 20px 0;
      word-break: break-all;
      font-family: monospace;
      color: #666;
    }
    .btn {
      display: inline-block;
      padding: 12px 30px;
      background: #007bff;
      color: white;
      text-decoration: none;
      border-radius: 4px;
      transition: background 0.3s;
    }
    .btn:hover { background: #0056b3; }
    .info { margin-top: 20px; color: #999; font-size: 14px; }
  </style>
</head>
<body>
  <div class="container">
    <h1>您即将离开 {sitename}</h1>
    <p>我们将跳转到以下外部链接：</p>
    <div class="url-box">{url}</div>
    <a href="{url}" class="btn">继续跳转</a>
    <p class="info">跳转过程可能需要几秒钟，请耐心等待</p>
  </div>
</body>
</html>
```

---

## 示例 2：科技感深色主题

适合科技类网站，深色背景 + 霓虹效果。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>安全跳转 - {sitename}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: 'Courier New', monospace;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      color: #00ff88;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      overflow: hidden;
    }
    .terminal {
      width: 90%;
      max-width: 700px;
      background: rgba(0,0,0,0.8);
      border: 2px solid #00ff88;
      border-radius: 10px;
      padding: 30px;
      box-shadow: 0 0 20px rgba(0,255,136,0.3);
    }
    .header {
      border-bottom: 1px solid #00ff88;
      padding-bottom: 15px;
      margin-bottom: 20px;
    }
    .logo { font-size: 24px; font-weight: bold; }
    .blink { animation: blink 1s infinite; }
    @keyframes blink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }
    .url-display {
      background: #0a0a0a;
      padding: 15px;
      border: 1px solid #00ff88;
      margin: 20px 0;
      font-size: 14px;
      word-break: break-all;
    }
    .btn-group { display: flex; gap: 15px; justify-content: center; margin-top: 25px; }
    .btn {
      padding: 12px 30px;
      background: transparent;
      color: #00ff88;
      border: 2px solid #00ff88;
      cursor: pointer;
      font-family: inherit;
      font-size: 16px;
      transition: all 0.3s;
      text-decoration: none;
    }
    .btn:hover { background: #00ff88; color: #000; }
    .status { margin-top: 20px; font-size: 12px; color: #666; }
    .status span { color: #ff6b6b; }
  </style>
</head>
<body>
  <div class="terminal">
    <div class="header">
      <span class="logo">&gt;_ SAFE_REDIRECT</span>
      <span class="blink">_</span>
    </div>
    <p>&gt; 检测到外部链接访问请求</p>
    <p>&gt; 目标域名：<span style="color: #ffd700">{sitename}</span></p>
    <div class="url-display">TARGET_URL: {url}</div>
    <p>&gt; 安全检查通过，允许跳转</p>
    <div class="btn-group">
      <a href="{url}" class="btn">[EXECUTE]</a>
      <a href="javascript:history.back()" class="btn">[CANCEL]</a>
    </div>
    <div class="status">
      <span>⚠ WARNING:</span> 您即将离开 {sitename}，请确认目标链接安全
    </div>
  </div>
</body>
</html>
```

---

## 示例 3：现代渐变风格

适合年轻化、设计感强的网站，渐变背景 + 卡片设计。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>跳转中 - {sitename}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 20px;
    }
    .card {
      background: white;
      width: 100%;
      max-width: 500px;
      border-radius: 20px;
      padding: 40px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      text-align: center;
      animation: slideUp 0.6s ease-out;
    }
    @keyframes slideUp {
      from { transform: translateY(30px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 40px;
    }
    h2 { color: #333; margin-bottom: 10px; }
    .subtitle { color: #666; margin-bottom: 25px; }
    .url-box {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 10px;
      margin: 20px 0;
      word-break: break-all;
      color: #555;
      font-family: monospace;
      font-size: 14px;
    }
    .btn {
      display: inline-block;
      padding: 15px 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      text-decoration: none;
      border-radius: 10px;
      font-weight: 600;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
      transition: all 0.3s;
    }
    .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
    }
    .warning {
      margin-top: 25px;
      padding: 15px;
      background: #fff3cd;
      border-left: 4px solid #ffc107;
      border-radius: 5px;
      color: #856404;
      font-size: 14px;
      text-align: left;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="icon">→</div>
    <h2>准备跳转</h2>
    <p class="subtitle">您即将离开 {sitename}</p>
    <div class="url-box">{url}</div>
    <a href="{url}" class="btn">立即跳转</a>
    <div class="warning">
      <strong>⚠ 安全提示</strong><br>
      此链接指向外部网站，{sitename} 对外部内容不承担责任。
    </div>
  </div>
</body>
</html>
```

---

## 示例 4：带倒计时的自动跳转页

5 秒后自动跳转，适合用户希望减少点击的场景。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>自动跳转 - {sitename}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 20px;
    }
    .container {
      background: white;
      width: 100%;
      max-width: 550px;
      border-radius: 12px;
      padding: 35px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.1);
      text-align: center;
    }
    .spinner {
      width: 50px;
      height: 50px;
      margin: 0 auto 20px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #3498db;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    h2 { color: #2c3e50; margin-bottom: 15px; }
    .countdown {
      font-size: 48px;
      font-weight: bold;
      color: #3498db;
      margin: 20px 0;
    }
    .url-text {
      color: #7f8c8d;
      word-break: break-all;
      font-size: 14px;
      background: #f8f9fa;
      padding: 12px;
      border-radius: 6px;
      margin: 15px 0;
    }
    .btn {
      display: inline-block;
      margin-top: 20px;
      padding: 12px 30px;
      background: #3498db;
      color: white;
      text-decoration: none;
      border-radius: 6px;
      transition: background 0.3s;
    }
    .btn:hover { background: #2980b9; }
    .manual-link {
      margin-top: 15px;
      font-size: 14px;
      color: #95a5a6;
    }
    .manual-link a { color: #3498db; text-decoration: none; }
  </style>
</head>
<body>
  <div class="container">
    <div class="spinner"></div>
    <h2>正在跳转...</h2>
    <div class="countdown" id="countdown">5</div>
    <p>我们将自动跳转到：</p>
    <div class="url-text">{url}</div>
    <a href="{url}" class="btn" id="jumpBtn">立即跳转</a>
    <p class="manual-link">
      <a href="javascript:cancelJump()">取消跳转</a>
    </p>
  </div>
  <script>
    let countdown = 5;
    let timer = null;
    const targetUrl = "{url}";

    function updateCountdown() {
      document.getElementById('countdown').textContent = countdown;
      if (countdown <= 0) {
        clearInterval(timer);
        window.location.href = targetUrl;
      }
      countdown--;
    }

    function cancelJump() {
      if (timer) {
        clearInterval(timer);
        timer = null;
        document.getElementById('countdown').textContent = "已取消";
        document.getElementById('jumpBtn').textContent = "继续跳转";
      }
    }

    timer = setInterval(updateCountdown, 1000);
  </script>
</body>
</html>
```

---

## 示例 5：移动端友好型

专门为手机优化，大按钮、清晰文字。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>跳转 - {sitename}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: #fff;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
    .content {
      width: 100%;
      max-width: 400px;
      text-align: center;
    }
    .icon {
      width: 100px;
      height: 100px;
      margin: 0 auto 25px;
      background: linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%);
      border-radius: 25px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 45px;
    }
    h1 { font-size: 28px; color: #333; margin-bottom: 15px; }
    .subtitle { font-size: 16px; color: #666; margin-bottom: 30px; line-height: 1.6; }
    .url-box {
      background: #f5f5f5;
      padding: 20px;
      border-radius: 12px;
      margin: 25px 0;
      word-break: break-all;
      font-size: 14px;
      color: #555;
      text-align: left;
    }
    .btn {
      display: block;
      width: 100%;
      padding: 18px;
      background: linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%);
      color: white;
      text-decoration: none;
      border-radius: 12px;
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 12px;
      transition: transform 0.2s;
    }
    .btn:active { transform: scale(0.98); }
    .btn-secondary {
      background: #f5f5f5;
      color: #666;
    }
    .notice {
      margin-top: 30px;
      padding: 15px;
      background: #fff3cd;
      border-radius: 8px;
      font-size: 13px;
      color: #856404;
      line-height: 1.5;
    }
  </style>
</head>
<body>
  <div class="content">
    <div class="icon">↗</div>
    <h1>准备跳转</h1>
    <p class="subtitle">您即将离开 {sitename}<br>前往外部链接</p>
    <div class="url-box">
      <strong>目标地址：</strong><br>
      {url}
    </div>
    <a href="{url}" class="btn">继续跳转</a>
    <a href="javascript:history.back()" class="btn btn-secondary">返回上页</a>
    <div class="notice">
      ⚠️ 请确认目标链接安全可靠<br>
      {sitename} 不对外部内容负责
    </div>
  </div>
</body>
</html>
```

---

## 使用方法

1. 在 Halo 后台进入"插件设置"
2. 找到"安全跳转助手"插件
3. 在"高级设置"或"样式设置"中找到"自定义整个跳转页面"字段
4. 将上面的任意一个示例代码完整复制粘贴进去
5. 保存设置

## 变量说明

| 变量 | 说明 | 示例 |
|------|------|------|
| `{url}` | 目标跳转链接（未编码） | `https://example.com/path` |
| `{sitename}` | 网站名称 | `我的博客` |
| `{encodedurl}` | URL 编码后的链接 | `https%3A%2F%2Fexample.com%2Fpath` |

## 注意事项

- 自定义 HTML 必须是完整的 HTML 文档（包含 `<!DOCTYPE html>`、`<html>`、`<head>`、`<body>` 等标签）
- 建议使用内联 CSS 或 `<style>` 标签，避免引用外部 CSS 文件
- JS 脚本可以使用 `<script>` 标签，但要注意安全性
- 确保响应式设计，兼容手机和桌面端
- 使用相对单位（rem、%、vw）而非固定像素
