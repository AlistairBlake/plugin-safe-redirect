# 自定义 CSS 样式指南

本文档说明如何为"安全跳转助手"插件编写自定义 CSS 样式。

## 概述

跳转页面使用纯手写 CSS（不依赖任何外部框架），所有样式类名使用 `sr-` 前缀命名，避免与主题样式冲突。

## 样式命名空间规范

所有样式类必须以 `sr-` 开头：

- `sr-card` - 主卡片容器
- `sr-icon-container` - 图标容器
- `sr-title` - 页面标题
- `sr-tip` - 提示文字
- `sr-btn` - 按钮基础样式
- `sr-btn-primary` - 主按钮（确认跳转）
- `sr-btn-secondary` - 次按钮（返回上页）
- `sr-countdown` - 倒计时提示框
- `sr-url-container` - URL 显示区域
- `sr-qrcode` - 二维码区域

## 完整样式清单

### 1. 基础重置与布局

```css
/* 基础重置 */
* { box-sizing: border-box; margin: 0; padding: 0; }

/* 页面背景与布局 */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 粒子背景画布 */
canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}
```

### 2. 卡片容器

```css
.sr-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 480px;
  width: 100%;
  padding: 40px;
  animation: fadeInUp 0.6s ease-out;
  backdrop-filter: blur(20px);
}
```

**可自定义项：**
- `background` - 卡片背景色（支持透明度）
- `border-radius` - 卡片圆角
- `box-shadow` - 卡片阴影
- `max-width` / `width` - 卡片宽度
- `padding` - 卡片内边距

### 3. 图标样式

```css
/* 图标容器 */
.sr-icon-container {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  animation: float 3s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3B82F6 0%, #8B5CF6 100%);
  border-radius: 16px; /* 圆角图标容器 */
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);
}

/* SVG 图标（默认） */
.sr-icon-svg {
  width: 40px;
  height: 40px;
  color: white;
}

.sr-icon-ring {
  opacity: 0.3;
}

.sr-icon-path {
  opacity: 0.9;
}

/* 自定义图片图标 */
.sr-icon-img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 8px; /* 图片图标圆角 */
}
```

**可自定义项：**
- `width` / `height` - 图标容器尺寸
- `background` - 图标容器背景（可改为纯色或图片）
- `border-radius` - 图标容器圆角（`50%` 为圆形，`16px` 为圆角矩形）
- `box-shadow` - 图标阴影

### 4. 文字样式

```css
.sr-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin-bottom: 12px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.sr-tip {
  font-size: 14px;
  color: #6b7280;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.sr-tip strong {
  color: #3B82F6;
}
```

### 5. URL 显示区域

```css
.sr-url-container {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.sr-url-label {
  font-size: 11px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.sr-url-text {
  font-size: 12px;
  color: #4b5563;
  font-family: 'Monaco', 'Courier New', monospace;
  word-break: break-all;
  display: block;
}
```

### 6. 二维码区域

```css
.sr-qrcode {
  text-align: center;
  margin-bottom: 24px;
}

.sr-qrcode-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.sr-qrcode-img {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
}
```

### 7. 倒计时提示框

```css
.sr-countdown {
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

.sr-countdown-icon {
  font-size: 18px;
}

.sr-countdown-text {
  font-size: 14px;
  color: #92400e;
}

#countdown-num {
  font-weight: 700;
  color: #f59e0b;
}
```

### 8. 按钮样式

```css
.sr-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

/* 按钮基础样式 */
.sr-btn {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  color: inherit; /* 确保图标颜色继承 */
}

/* 主按钮（确认跳转） */
.sr-btn-primary {
  background: linear-gradient(135deg, #3B82F6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.sr-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

/* 次按钮（返回上页） */
.sr-btn-secondary {
  background: transparent;
  color: #6b7280;
  border: 2px solid #e5e7eb;
}

.sr-btn-secondary:hover {
  border-color: #9ca3af;
  color: #374151;
  background: #f9fafb;
}

/* 按钮内 SVG 图标 */
.sr-btn svg {
  width: 18px;
  height: 18px;
  fill: currentColor; /* 关键：确保图标颜色继承父元素 */
}
```

**关键修复：**
- `color: inherit` - 确保按钮文字和图标颜色一致
- `fill: currentColor` - SVG 图标使用当前文字颜色

### 9. 动画效果

```css
/* 淡入上浮动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 浮动动画（图标） */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

/* 脉冲发光动画（图标阴影） */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: 0 10px 40px rgba(59, 130, 246, 0.6);
  }
}
```

### 10. 响应式设计

```css
@media (max-width: 480px) {
  .sr-card {
    padding: 24px;
  }
  
  .sr-title {
    font-size: 20px;
  }
  
  .sr-buttons {
    flex-direction: column; /* 按钮改为垂直排列 */
  }
}
```

## 如何应用自定义样式

目前插件不支持外部 CSS 文件加载，所有样式都在 `SafeRedirectRouter.java` 的 `buildRedirectPage()` 方法中硬编码。

如需自定义样式，需要：

1. 修改 `src/main/java/run/halo/saferedirect/SafeRedirectRouter.java`
2. 找到 `buildRedirectPage()` 方法中的 CSS 字符串
3. 按需修改样式属性
4. 重新构建插件

## 常见自定义示例

### 修改卡片颜色

```css
.sr-card {
  background: #ffffff; /* 纯白背景 */
  border: 2px solid #3B82F6; /* 蓝色边框 */
}
```

### 修改图标为圆形

```css
.sr-icon-container {
  border-radius: 50%; /* 圆形 */
}
```

### 修改按钮颜色

```css
.sr-btn-primary {
  background: #10b981; /* 绿色 */
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.sr-btn-primary:hover {
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}
```

### 移除动画效果

```css
.sr-icon-container {
  animation: none; /* 移除浮动动画 */
}

.sr-card {
  animation: none; /* 移除淡入动画 */
}
```

## 注意事项

1. **避免样式冲突**：所有类名必须使用 `sr-` 前缀
2. **SVG 图标颜色**：确保使用 `fill: currentColor` 继承颜色
3. **渐变背景**：线性渐变使用 `linear-gradient(direction, color1, color2)` 格式
4. **响应式适配**：在移动端（宽度 < 480px）时自动调整样式
5. **性能优化**：避免过度使用复杂动画或高分辨率背景图

## 颜色参考

| 用途 | 颜色 | 十六进制 |
|------|------|---------|
| 主色调 | 蓝色 | #3B82F6 |
| 次色调 | 紫色 | #8B5CF6 |
| 文字主色 | 深灰 | #1f2937 |
| 文字辅色 | 中灰 | #6b7280 |
| 边框色 | 浅灰 | #e5e7eb |
| 背景色 | 浅灰 | #f3f4f6 |
| 警告色 | 黄色 | #f59e0b |
