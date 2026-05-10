package run.halo.saferedirect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 安全跳转路由处理器
 *
 * <p>处理两个端点：
 * <ul>
 *   <li>GET /plugins/plugin-safe-redirect/go — 渲染安全跳转中间页</li>
 *   <li>GET /plugins/plugin-safe-redirect/assets/** — 静态资源（由框架自动处理）</li>
 * </ul>
 *
 * @author SafeRedirect Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafeRedirectRouter {
    private final ReactiveSettingFetcher settingFetcher;

    /**
     * 注册路由：/plugins/plugin-safe-redirect/go
     */
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
            .GET("/plugins/plugin-safe-redirect/go", this::handleRedirect)
            .build();
    }

    /**
     * 处理安全跳转请求
     * <p>
     * 请求示例：/plugins/plugin-safe-redirect/go?url=https%3A%2F%2Fgithub.com
     */
    private Mono<ServerResponse> handleRedirect(ServerRequest request) {
        String rawUrl = request.queryParam("url").orElse("");

        if (rawUrl.isBlank()) {
            return ServerResponse.badRequest()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue("<h1>缺少目标链接参数</h1>");
        }

        String targetUrl;
        try {
            targetUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                log.warn("Blocked non-http redirect attempt to: {}", targetUrl);
                return ServerResponse.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .bodyValue(buildErrorPage("非法链接", "只支持 HTTP/HTTPS 协议的外部链接跳转。"));
            }
        } catch (Exception e) {
            log.warn("Invalid URL encoding: {}", rawUrl);
            return ServerResponse.badRequest()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(buildErrorPage("链接格式错误", "无法解析目标链接，请检查链接是否正确。"));
        }

        final String finalUrl = targetUrl;

        return settingFetcher.fetch("basic", BasicSetting.class)
            .defaultIfEmpty(new BasicSetting())
            .flatMap(basicSetting -> handleRedirectInternal(finalUrl, basicSetting))
            .onErrorResume(e -> {
                log.error("Unexpected error in redirect handler: {}", finalUrl, e);
                return ServerResponse.status(500)
                    .contentType(MediaType.TEXT_HTML)
                    .bodyValue(buildErrorPage("服务器内部错误",
                        "插件执行时发生意外错误：<br><code>" + 
                        escapeHtml(e.getClass().getName() + ": " + e.getMessage()) +
                        "</code><br><br>" +
                        "<a href=\"" + escapeHtml(finalUrl) + "\">🔗 点击此处直接访问目标网站</a>"));
            });
    }

    /**
     * 内部跳转逻辑（可复用）
     */
    private Mono<ServerResponse> handleRedirectInternal(String finalUrl, BasicSetting basicSetting) {
        if (!basicSetting.isEnabled()) {
            try {
                return ServerResponse.temporaryRedirect(URI.create(finalUrl)).build();
            } catch (Exception e) {
                log.error("Failed to create redirect URI: {}", finalUrl, e);
                return buildErrorResponse(finalUrl, "重定向失败");
            }
        }

        boolean whitelisted = isWhitelisted(finalUrl, basicSetting.getWhitelistDomains());
        if (whitelisted) {
            log.debug("Whitelisted domain, direct redirect to: {}", finalUrl);
            try {
                return ServerResponse.temporaryRedirect(URI.create(finalUrl)).build();
            } catch (Exception e) {
                log.error("Failed to create redirect URI for whitelisted URL: {}", finalUrl, e);
                return buildErrorResponse(finalUrl, "白名单跳转失败");
            }
        }

        return settingFetcher.fetch("advanced", AdvancedSetting.class)
            .onErrorResume(e -> {
                log.warn("Failed to fetch advanced settings, using defaults: {}", e.getMessage());
                return Mono.just(new AdvancedSetting());
            })
            .defaultIfEmpty(new AdvancedSetting())
            .flatMap(advancedSetting -> {
                if (advancedSetting.isTrackOutbound()) {
                    log.info("Outbound link accessed: {}", finalUrl);
                }
                return settingFetcher.fetch("style", StyleSetting.class)
                    .onErrorResume(e -> {
                        log.warn("Failed to fetch style settings, using defaults: {}", e.getMessage());
                        return Mono.just(new StyleSetting());
                    })
                    .defaultIfEmpty(new StyleSetting())
                    .flatMap(styleSetting -> {
                        try {
                            String customHtml = styleSetting.getCustomHtml();
                            if (customHtml != null && !customHtml.trim().isEmpty()) {
                                return ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .bodyValue(buildCustomPage(finalUrl, basicSetting, customHtml.trim()));
                            }
                            return ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(buildRedirectPage(
                                    finalUrl, basicSetting, styleSetting, advancedSetting));
                        } catch (Exception e) {
                            log.error("Error building redirect page for: {}", finalUrl, e);
                            return buildErrorResponse(finalUrl, "页面构建失败: " + e.getMessage());
                        }
                    });
            });
    }

    /**
     * 构建统一的错误响应
     */
    private Mono<ServerResponse> buildErrorResponse(String targetUrl, String errorMessage) {
        return ServerResponse.ok()
            .contentType(MediaType.TEXT_HTML)
            .bodyValue(buildErrorPage("跳转处理失败",
                errorMessage + "<br><br>" +
                "<div style=\"text-align:center; margin-top:20px;\">" +
                "<a href=\"" + escapeHtml(targetUrl) + "\" " +
                "style=\"display:inline-block;padding:12px 24px;background:#3B82F6;color:white;" +
                "border-radius:8px;text-decoration:none;font-weight:bold;\">🔗 直接访问目标网站</a>" +
                "</div>"));
    }

    /**
     * 检查目标URL是否在白名单中
     */
    private boolean isWhitelisted(String targetUrl, String whitelistDomains) {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            return false;
        }
        if (whitelistDomains == null || whitelistDomains.trim().isEmpty()) {
            return false;
        }
        List<String> domains = Arrays.stream(whitelistDomains.split("[\n,]"))
            .map(String::trim)
            .filter(d -> !d.isEmpty())
            .toList();

        if (domains.isEmpty()) {
            return false;
        }

        try {
            URI uri = URI.create(targetUrl);
            String host = uri.getHost();
            if (host == null) {
                log.debug("Whitelist check: URL has no host - {}", targetUrl);
                return false;
            }
            String lowerHost = host.toLowerCase();
            boolean matched = domains.stream().anyMatch(domain -> {
                String lowerDomain = domain.toLowerCase();
                boolean result = lowerHost.equals(lowerDomain) || lowerHost.endsWith("." + lowerDomain);
                log.debug("Whitelist check: host={}, domain={}, matched={}", lowerHost, lowerDomain, result);
                return result;
            });
            log.info("Whitelist check final result: host={}, matched={}, whitelist={}", lowerHost, matched, domains);
            return matched;
        } catch (Exception e) {
            log.warn("Failed to parse URL for whitelist check: {}", targetUrl, e);
            return false;
        }
    }

    /**
     * 构建安全跳转页面 HTML
     */
    private String buildRedirectPage(String targetUrl,
                                     BasicSetting basic,
                                     StyleSetting style,
                                     AdvancedSetting advanced) {
        String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);
        String displayUrl = targetUrl.length() > 80 ? targetUrl.substring(0, 80) + "..." : targetUrl;
        String tipText = (advanced.getCustomTip() != null && !advanced.getCustomTip().isBlank())
            ? escapeHtml(advanced.getCustomTip())
            : "您即将离开 <strong>" + escapeHtml(basic.getSiteName())
                + "</strong>，前往以下外部网站。外部链接的内容不受本站控制，请谨慎访问。";

        int countdown = style.getCountdown();
        String countdownJs = countdown > 0 ? buildCountdownJs(countdown, targetUrl, style.getTheme()) : "";
        String countdownHtmlUI = buildCountdownHtml(countdown, style.getTheme());
        String qrCodeHtmlUI = buildQrCodeHtml(encodedUrl, style.isShowQrCode());
        String urlDisplayHtmlUI = buildUrlDisplayHtml(displayUrl, style.isShowTargetUrl());
        String themeStyles = buildThemeStyles(style.getTheme());
        String backgroundOverride = buildBackgroundOverride(style.getBackgroundUrl(), style.getBackgroundColor());
        String iconHtml = buildIconHtml(style.getIconUrl());

        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <meta name="robots" content="noindex,nofollow">
              <title>%s</title>
              <style>
            %s
            %s
                /* 二维码 */
                .sr-qrcode { text-align: center; margin-bottom: 24px; }
                .sr-qrcode-label { font-size: 12px; color: #9ca3af; margin-bottom: 8px; }
                .sr-qrcode-img { border: 2px solid #e5e7eb; border-radius: 8px; }

                /* 倒计时 */
                .sr-countdown { background: #fef3c7; border: 1px solid #f59e0b; border-radius: 8px; padding: 12px 16px; display: flex; align-items: center; gap: 8px; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                .sr-countdown-icon { font-size: 18px; }
                .sr-countdown-text { font-size: 14px; color: #92400e; }
                #countdown-num { font-weight: 700; color: #f59e0b; }

                /* 按钮 */
                .sr-buttons { display: flex; gap: 12px; margin-top: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                .sr-btn { flex: 1; padding: 14px 24px; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; text-decoration: none; }
                .sr-btn-primary { background: linear-gradient(135deg, #3B82F6 0%%, #2563eb 100%%); color: white; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); }
                .sr-btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4); }
                .sr-btn-secondary { background: transparent; color: #6b7280; border: 2px solid #e5e7eb; }
                .sr-btn-secondary:hover { border-color: #9ca3af; color: #374151; background: #f9fafb; }

                /* 动画 */
                @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
                @keyframes float { 0%%, 100%% { transform: translateY(0px); } 50%% { transform: translateY(-8px); } }
                @keyframes pulse { 0%%, 100%% { box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4); } 50%% { box-shadow: 0 10px 40px rgba(59, 130, 246, 0.6); } }

                /* 响应式 */
                @media (max-width: 480px) {
                  .sr-card { padding: 24px; }
                  .sr-title { font-size: 20px; }
                  .sr-buttons { flex-direction: column; }
                }
              </style>
              %s
            </head>
            <body>
              <canvas id="particle-canvas"></canvas>
              <div class="sr-card">
                <div class="sr-icon-container">%s</div>
                <h2 class="sr-title">%s</h2>
                <p class="sr-tip">%s</p>
            %s
            %s
            %s
                <div class="sr-buttons">
                  <a href="%s" rel="noopener noreferrer nofollow" id="confirm-btn" class="sr-btn sr-btn-primary">确认跳转</a>
                  <a href="javascript:history.back()" class="sr-btn sr-btn-secondary">返回上页</a>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(basic.getPageTitle()),
                themeStyles,
                backgroundOverride,
                countdownJs,
                iconHtml,
                escapeHtml(basic.getPageTitle()),
                tipText,
                urlDisplayHtmlUI,
                qrCodeHtmlUI,
                countdownHtmlUI,
                escapeHtml(targetUrl)
            );
    }

    /**
     * 构建倒计时 HTML（根据主题选择不同样式）
     */
    private String buildCountdownHtml(int countdown, String theme) {
        if (countdown <= 0) return "";
        
        if ("dream".equals(theme)) {
            return """
                    <div class="sr-countdown" id="progress-bar"></div>
                                    <div class="countdown-text">
                                      <span style="color: #FF9800; margin-right: 4px;">⚡</span>
                                      <span id="countdown-text-tip">将在 <span id="countdown-num">%d</span> 秒后自动跳转</span>
                                    </div>""".formatted(countdown);
        }
        
        return """
                <div class="sr-countdown mt-4">
                  <div class="sr-countdown-icon">⏱</div>
                  <span class="sr-countdown-text"><span id="countdown-num">%d</span> 秒后自动跳转...</span>
                </div>""".formatted(countdown);
    }

    /**
     * 构建二维码 HTML
     */
    private String buildQrCodeHtml(String encodedUrl, boolean showQrCode) {
        if (!showQrCode) return "";
        
        return """
                <div class="sr-qrcode animate-fade-in-delay-3">
                  <p class="sr-qrcode-label">扫码在移动端查看</p>
                  <img src="https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=%s" alt="二维码" class="sr-qrcode-img"/>
                </div>""".formatted(encodedUrl);
    }

    /**
     * 构建目标 URL 显示区域 HTML
     */
    private String buildUrlDisplayHtml(String displayUrl, boolean showTargetUrl) {
        if (!showTargetUrl) return "";
        
        return """
                <div class="sr-url-container animate-fade-in-delay-2">
                  <p class="sr-url-label">目标地址</p>
                  <code class="sr-url-text">%s</code>
                </div>""".formatted(displayUrl);
    }

    /**
     * 构建图标 HTML（自定义图片或默认 SVG）
     */
    private String buildIconHtml(String iconUrl) {
        if (iconUrl != null && !iconUrl.trim().isEmpty()) {
            return "<img src=\"%s\" alt=\"图标\" class=\"sr-icon-img\"/>".formatted(escapeHtml(iconUrl.trim()));
        }
        
        return """
            <svg class="sr-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" class="sr-icon-ring"/>
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" class="sr-icon-path"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" class="sr-icon-path"/>
            </svg>""";
    }

    /**
     * 构建错误页面
     */
    private String buildErrorPage(String title, String message) {
        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">"
            + "<title>错误 - " + escapeHtml(title) + "</title></head><body>"
            + "<h2>⚠️ " + escapeHtml(title) + "</h2>"
            + "<p>" + escapeHtml(message) + "</p>"
            + "<a href=\"javascript:history.back()\">返回上页</a>"
            + "</body></html>";
    }

    /**
     * 构建主题 CSS
     */
    /**
     * 构建倒计时 JS
     * @param seconds 倒计时秒数
     * @param targetUrl 目标 URL
     * @param theme 当前主题名称（用于区分 Dream 主题的进度条动画）
     */
    private String buildCountdownJs(int seconds, String targetUrl, String theme) {
        String escaped = targetUrl.replace("'", "\\'").replace("\"", "&quot;");

        // Dream 主题使用进度条动画
        if ("dream".equals(theme)) {
            return buildDreamCountdownJs(seconds, escaped);
        }

        // 其他主题使用传统倒计时 + 粒子动画
        return """
            <script>
              (function() {
                // 粒子背景动画
                var canvas = document.getElementById('particle-canvas');
                if (canvas) {
                  var ctx = canvas.getContext('2d');
                  var particles = [];
                  var mouse = { x: null, y: null };
                  
                  function resize() {
                    canvas.width = window.innerWidth;
                    canvas.height = window.innerHeight;
                  }
                  resize();
                  window.addEventListener('resize', resize);
                  
                  class Particle {
                    constructor() {
                      this.x = Math.random() * canvas.width;
                      this.y = Math.random() * canvas.height;
                      this.size = Math.random() * 2 + 1;
                      this.speedX = Math.random() * 0.8 - 0.4;
                      this.speedY = Math.random() * 0.8 - 0.4;
                      this.opacity = Math.random() * 0.5 + 0.2;
                    }
                    update() {
                      this.x += this.speedX;
                      this.y += this.speedY;
                      if (this.x < 0) this.x = canvas.width;
                      if (this.x > canvas.width) this.x = 0;
                      if (this.y < 0) this.y = canvas.height;
                      if (this.y > canvas.height) this.y = 0;
                    }
                    draw() {
                      ctx.beginPath();
                      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
                      ctx.fillStyle = 'rgba(150, 150, 150, ' + this.opacity + ')';
                      ctx.fill();
                    }
                  }
                  
                  function initParticles() {
                    particles = [];
                    var count = Math.min(60, Math.floor((canvas.width * canvas.height) / 15000));
                    for (var i = 0; i < count; i++) {
                      particles.push(new Particle());
                    }
                  }
                  initParticles();
                  
                  function connectParticles() {
                    for (var i = 0; i < particles.length; i++) {
                      for (var j = i + 1; j < particles.length; j++) {
                        var dx = particles[i].x - particles[j].x;
                        var dy = particles[i].y - particles[j].y;
                        var dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < 150) {
                          ctx.beginPath();
                          ctx.strokeStyle = 'rgba(150, 150, 150, ' + (0.15 - dist / 150 * 0.15) + ')';
                          ctx.lineWidth = 1;
                          ctx.moveTo(particles[i].x, particles[i].y);
                          ctx.lineTo(particles[j].x, particles[j].y);
                          ctx.stroke();
                        }
                      }
                    }
                  }
                  
                  function animate() {
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                    particles.forEach(function(p) { p.update(); p.draw(); });
                    connectParticles();
                    requestAnimationFrame(animate);
                  }
                  animate();
                }
                
                // 倒计时逻辑
                var remaining = %d;
                var el = document.getElementById('countdown-num');
                var btn = document.getElementById('confirm-btn');
                var timer = setInterval(function() {
                  remaining--;
                  if (el) el.textContent = remaining;
                  if (remaining <= 0) {
                    clearInterval(timer);
                    window.location.href = '%s';
                  }
                }, 1000);
                // 用户主动点击时清除倒计时
                if (btn) {
                  btn.addEventListener('click', function() { clearInterval(timer); });
                }
              })();
            </script>
            """.formatted(seconds, escaped);
    }

    /**
     * Dream 主题专用的进度条倒计时 JS
     * <p>
     * 特点：
     * - 使用 CSS 进度条动画代替数字倒计时
     * - 进度条从 0% 平滑过渡到 100%
     * - 支持用户点击按钮立即跳转
     *
     * @param seconds 倒计时秒数
     * @param escapedTargetUrl 已转义的目标 URL
     */
    private String buildDreamCountdownJs(int seconds, String escapedTargetUrl) {
        return """
            <script>
              (function() {
                var remaining = %d;
                var countdownElement = document.getElementById('countdown-num');
                var countdownTextTip = document.getElementById('countdown-text-tip');
                var progressBar = document.getElementById('progress-bar');
                var btn = document.getElementById('confirm-btn');

                // 初始化进度条
                if (progressBar) {
                  progressBar.style.setProperty('--progress-width', '0%%');
                  // 强制重绘以触发 transition
                  void progressBar.offsetWidth;
                  progressBar.style.setProperty('--progress-width', '100%%');
                  progressBar.style.transition = 'width %ds linear';
                }

                var timer = setInterval(function() {
                  remaining--;
                  if (countdownElement) countdownElement.textContent = remaining;
                  if (remaining <= 0) {
                    clearInterval(timer);
                    if (countdownTextTip) countdownTextTip.textContent = '正在跳转...';
                    if (progressBar) {
                      progressBar.style.setProperty('--progress-width', '100%%');
                      progressBar.style.transition = 'none';
                    }
                    setTimeout(function() {
                      window.location.href = '%s';
                    }, 100);
                  }
                }, 1000);

                // 用户主动点击时清除倒计时并立即跳转
                if (btn) {
                  btn.addEventListener('click', function() {
                    clearInterval(timer);
                    if (countdownElement) countdownElement.textContent = '0';
                    if (countdownTextTip) countdownTextTip.textContent = '正在跳转...';
                    if (progressBar) {
                      progressBar.style.setProperty('--progress-width', '100%%');
                      progressBar.style.transition = 'none';
                    }
                  });
                }
              })();
            </script>
            """.formatted(seconds, escapedTargetUrl);
    }

    /**
     * HTML 转义，防止 XSS
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    /**
     * 构建自定义页面（用户提供的完整 HTML）
     */
    private String buildCustomPage(String targetUrl, BasicSetting basic, String customHtml) {
        String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);
        // 支持变量替换：{url} = 目标URL，{sitename} = 网站名称（已进行HTML转义防止XSS）
        String processedHtml = customHtml
            .replace("{url}", escapeHtml(targetUrl))
            .replace("{sitename}", escapeHtml(basic.getSiteName()))
            .replace("{encodedurl}", escapeHtml(encodedUrl));
        
        return "<!DOCTYPE html>\n"
            + "<html lang=\"zh-CN\">\n"
            + "<head>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "  <title>" + escapeHtml(basic.getPageTitle()) + "</title>\n"
            + "  <style>\n"
            + "    body { margin: 0; padding: 0; }\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + processedHtml + "\n"
            + "</body>\n"
            + "</html>";
    }

    /**
     * 构建主题样式
     */
    private String buildThemeStyles(String theme) {
        if (theme == null) theme = "dream";
        
        switch (theme) {
            case "minimal":
                return """
                    /* 极简主题 */
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #ffffff; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
                    canvas { display: none; }
                    .sr-card { background: #ffffff; border: 2px solid #e5e7eb; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); max-width: 480px; width: 100%%; padding: 40px; animation: fadeInUp 0.6s ease-out; }
                    .sr-icon-container { width: 80px; height: 80px; margin: 0 auto 24px; display: flex; align-items: center; justify-content: center; background: #f3f4f6; border-radius: 50%%; }
                    .sr-icon-svg { width: 40px; height: 40px; color: #6b7280; }
                    .sr-icon-img { width: 48px; height: 48px; object-fit: contain; border-radius: 50%%; }
                    .sr-title { font-size: 22px; font-weight: 600; color: #1f2937; text-align: center; margin-bottom: 12px; animation: fadeInUp 0.6s ease-out 0.1s both; }
                    .sr-tip { font-size: 14px; color: #6b7280; text-align: center; line-height: 1.6; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.1s both; }
                    .sr-tip strong { color: #1f2937; font-weight: 600; }
                    .sr-url-container { background: #f9fafb; border-radius: 6px; padding: 16px; margin-bottom: 24px; border: 1px solid #e5e7eb; }
                    .sr-url-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
                    .sr-url-text { font-size: 12px; color: #4b5563; font-family: 'Monaco', 'Courier New', monospace; word-break: break-all; display: block; }
                    .sr-qrcode { text-align: center; margin-bottom: 24px; }
                    .sr-qrcode-label { font-size: 12px; color: #9ca3af; margin-bottom: 8px; }
                    .sr-qrcode-img { border: 1px solid #e5e7eb; border-radius: 6px; }
                    .sr-countdown { background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px 16px; display: flex; align-items: center; gap: 8px; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-countdown-icon { font-size: 16px; }
                    .sr-countdown-text { font-size: 14px; color: #4b5563; }
                    #countdown-num { font-weight: 600; color: #1f2937; }
                    .sr-buttons { display: flex; gap: 12px; margin-top: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-btn { flex: 1; padding: 12px 20px; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; text-decoration: none; }
                    .sr-btn-primary { background: #1f2937; color: white; }
                    .sr-btn-primary:hover { background: #374151; }
                    .sr-btn-secondary { background: #ffffff; color: #6b7280; border: 2px solid #e5e7eb; }
                    .sr-btn-secondary:hover { border-color: #9ca3af; color: #374151; background: #f9fafb; }
                    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
                    @media (max-width: 480px) {
                      .sr-card { padding: 24px; }
                      .sr-title { font-size: 18px; }
                      .sr-buttons { flex-direction: column; }
                    }
                    """;
                
            case "tech":
                return """
                    /* 科技主题 */
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: 'Courier New', monospace; background: linear-gradient(135deg, #0f0f23 0%%, #1a1a2e 100%%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
                    canvas { position: fixed; top: 0; left: 0; width: 100%%; height: 100%%; z-index: -1; }
                    .sr-card { background: rgba(26, 26, 46, 0.9); border: 1px solid #00f0ff; border-radius: 4px; box-shadow: 0 0 30px rgba(0, 240, 255, 0.2); max-width: 480px; width: 100%%; padding: 40px; animation: fadeInUp 0.6s ease-out; backdrop-filter: blur(10px); }
                    .sr-icon-container { width: 80px; height: 80px; margin: 0 auto 24px; animation: float 3s ease-in-out infinite; display: flex; align-items: center; justify-content: center; background: rgba(0, 240, 255, 0.1); border: 2px solid #00f0ff; border-radius: 4px; box-shadow: 0 0 20px rgba(0, 240, 255, 0.3); }
                    .sr-icon-svg { width: 40px; height: 40px; color: #00f0ff; }
                    .sr-icon-img { width: 48px; height: 48px; object-fit: contain; }
                    .sr-title { font-size: 20px; font-weight: 700; color: #00f0ff; text-align: center; margin-bottom: 12px; animation: fadeInUp 0.6s ease-out 0.1s both; text-transform: uppercase; letter-spacing: 2px; }
                    .sr-tip { font-size: 13px; color: #a0aec0; text-align: center; line-height: 1.6; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.1s both; }
                    .sr-tip strong { color: #00f0ff; }
                    .sr-url-container { background: rgba(0, 240, 255, 0.05); border: 1px solid #00f0ff; border-radius: 4px; padding: 16px; margin-bottom: 24px; }
                    .sr-url-label { font-size: 10px; color: #00f0ff; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
                    .sr-url-text { font-size: 11px; color: #00f0ff; font-family: 'Courier New', monospace; word-break: break-all; display: block; }
                    .sr-qrcode { text-align: center; margin-bottom: 24px; }
                    .sr-qrcode-label { font-size: 11px; color: #00f0ff; margin-bottom: 8px; }
                    .sr-qrcode-img { border: 1px solid #00f0ff; border-radius: 4px; }
                    .sr-countdown { background: rgba(0, 240, 255, 0.1); border: 1px solid #00f0ff; border-radius: 4px; padding: 12px 16px; display: flex; align-items: center; gap: 8px; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-countdown-icon { font-size: 16px; }
                    .sr-countdown-text { font-size: 13px; color: #00f0ff; }
                    #countdown-num { font-weight: 700; color: #00f0ff; }
                    .sr-buttons { display: flex; gap: 12px; margin-top: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-btn { flex: 1; padding: 14px 24px; border: 2px solid #00f0ff; border-radius: 4px; font-size: 14px; font-weight: 700; cursor: pointer; transition: all 0.3s ease; text-decoration: none; text-transform: uppercase; letter-spacing: 1px; }
                    .sr-btn-primary { background: #00f0ff; color: #0f0f23; }
                    .sr-btn-primary:hover { background: #00c8d4; box-shadow: 0 0 20px rgba(0, 240, 255, 0.4); }
                    .sr-btn-secondary { background: transparent; color: #00f0ff; }
                    .sr-btn-secondary:hover { background: rgba(0, 240, 255, 0.1); }
                    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
                    @keyframes float { 0%%, 100%% { transform: translateY(0px); } 50%% { transform: translateY(-8px); } }
                    @media (max-width: 480px) {
                      .sr-card { padding: 24px; }
                      .sr-title { font-size: 16px; }
                      .sr-buttons { flex-direction: column; }
                    }
                    """;
                
            case "warm":
                return """
                    /* 温暖主题 */
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: linear-gradient(135deg, #ffecd2 0%%, #fcb69f 100%%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
                    canvas { display: none; }
                    .sr-card { background: rgba(255, 255, 255, 0.9); border-radius: 20px; box-shadow: 0 10px 40px rgba(251, 146, 60, 0.3); max-width: 480px; width: 100%%; padding: 40px; animation: fadeInUp 0.6s ease-out; backdrop-filter: blur(10px); }
                    .sr-icon-container { width: 80px; height: 80px; margin: 0 auto 24px; animation: float 3s ease-in-out infinite; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f97316 0%%, #fbbf24 100%%); border-radius: 20px; box-shadow: 0 8px 20px rgba(249, 115, 22, 0.4); }
                    .sr-icon-svg { width: 40px; height: 40px; color: white; }
                    .sr-icon-img { width: 48px; height: 48px; object-fit: contain; border-radius: 12px; }
                    .sr-title { font-size: 24px; font-weight: 700; color: #1f2937; text-align: center; margin-bottom: 12px; animation: fadeInUp 0.6s ease-out 0.1s both; }
                    .sr-tip { font-size: 14px; color: #6b7280; text-align: center; line-height: 1.6; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.1s both; }
                    .sr-tip strong { color: #f97316; }
                    .sr-url-container { background: #fff7ed; border-radius: 12px; padding: 16px; margin-bottom: 24px; border: 1px solid #fed7aa; }
                    .sr-url-label { font-size: 11px; color: #fb923c; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
                    .sr-url-text { font-size: 12px; color: #4b5563; font-family: 'Monaco', 'Courier New', monospace; word-break: break-all; display: block; }
                    .sr-qrcode { text-align: center; margin-bottom: 24px; }
                    .sr-qrcode-label { font-size: 12px; color: #fb923c; margin-bottom: 8px; }
                    .sr-qrcode-img { border: 2px solid #fed7aa; border-radius: 12px; }
                    .sr-countdown { background: #fff7ed; border: 1px solid #f97316; border-radius: 12px; padding: 12px 16px; display: flex; align-items: center; gap: 8px; margin-bottom: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-countdown-icon { font-size: 18px; }
                    .sr-countdown-text { font-size: 14px; color: #9a3412; }
                    #countdown-num { font-weight: 700; color: #f97316; }
                    .sr-buttons { display: flex; gap: 12px; margin-top: 24px; animation: fadeInUp 0.6s ease-out 0.3s both; }
                    .sr-btn { flex: 1; padding: 14px 24px; border: none; border-radius: 12px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; text-decoration: none; }
                    .sr-btn-primary { background: linear-gradient(135deg, #f97316 0%%, #fbbf24 100%%); color: white; box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3); }
                    .sr-btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(249, 115, 22, 0.4); }
                    .sr-btn-secondary { background: #ffffff; color: #6b7280; border: 2px solid #fed7aa; }
                    .sr-btn-secondary:hover { border-color: #f97316; color: #f97316; background: #fff7ed; }
                    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
                    @keyframes float { 0%%, 100%% { transform: translateY(0px); } 50%% { transform: translateY(-8px); } }
                    @media (max-width: 480px) {
                      .sr-card { padding: 24px; }
                      .sr-title { font-size: 20px; }
                      .sr-buttons { flex-direction: column; }
                    }
                    """;

            case "dream":
                return """
                    /* Dream 主题 - 毛玻璃梦幻风格 */
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background-image: linear-gradient(135deg, #a0a0a0 0%%, #8c8c8c 100%%);
                        background-position: center;
                        background-size: cover;
                        background-repeat: no-repeat;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                        overflow-x: hidden;
                    }
                    canvas { display: none; }

                    /* 毛玻璃卡片主体 */
                    .sr-card {
                        text-align: center;
                        padding: 35px;
                        border-radius: 24px;
                        animation: fadein 0.3s ease-out;
                        width: 400px;
                        max-width: 90%%;
                        border: 2px solid rgba(255, 255, 255, 0.4);
                        background: rgba(255, 255, 255, 0.85);
                        backdrop-filter: blur(25px);
                        -webkit-backdrop-filter: blur(25px);
                        box-shadow: 0 12px 40px rgba(31, 38, 135, 0.2);
                        position: relative;
                        z-index: 2;
                        isolation: isolate;
                    }

                    /* 卡片光晕边框效果 */
                    .sr-card::before {
                        content: '';
                        position: absolute;
                        top: -6px;
                        left: -6px;
                        right: -6px;
                        bottom: -6px;
                        border-radius: 26px;
                        background: linear-gradient(145deg,
                            rgba(255,255,255,0.3) 0%%,
                            rgba(255,255,255,0.1) 100%%);
                        z-index: -1;
                    }

                    /* 圆形图标容器 */
                    .sr-icon-container {
                        width: 100px;
                        height: 100px;
                        margin: 0 auto 15px auto;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        border-radius: 50%%;
                        border: 3px solid rgba(255, 255, 255, 0.3);
                        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                        transition: transform 0.3s ease;
                        background: linear-gradient(135deg, #abedd8 0%%, #74ebd5 100%%);
                    }

                    .sr-icon-container:hover {
                        transform: scale(1.05);
                    }

                    .sr-icon-svg { width: 50px; height: 50px; color: white; }
                    .sr-icon-img { width: 94px; height: 94px; object-fit: contain; border-radius: 50%%; }

                    /* 标题样式 */
                    .sr-title {
                        font-size: 20px;
                        font-weight: bold;
                        color: #333333;
                        text-align: center;
                        margin-bottom: 20px;
                        animation: fadein 0.3s ease-out 0.1s both;
                    }

                    /* 提示文字 */
                    .sr-tip {
                        font-size: 16px;
                        line-height: 1.5;
                        color: #555555;
                        text-align: center;
                        margin-bottom: 10px;
                        letter-spacing: 1px;
                        animation: fadein 0.3s ease-out 0.15s both;
                        word-wrap: break-word;
                        white-space: pre-wrap;
                    }

                    .sr-tip strong { color: #abedd8; font-weight: 600; }

                    /* URL 显示区域 */
                    .sr-url-container {
                        border: 1px solid #e8eef5;
                        backdrop-filter: blur(10px);
                        -webkit-backdrop-filter: blur(10px);
                        font-size: 14px;
                        display: block;
                        margin-top: 5px;
                        margin-bottom: 25px;
                        padding: 15px;
                        border-radius: 8px;
                        background-color: #F7F9FE;
                        animation: fadein 0.3s ease-out 0.2s both;
                    }

                    .sr-url-label {
                        font-size: 11px;
                        color: #abedd8;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 8px;
                        display: block;
                    }

                    .sr-url-text {
                        font-size: 12px;
                        color: #4b5563;
                        font-family: 'Monaco', 'Courier New', monospace;
                        word-break: break-all;
                        display: block;
                    }

                    /* 二维码 */
                    .sr-qrcode {
                        text-align: center;
                        margin-bottom: 25px;
                        animation: fadein 0.3s ease-out 0.25s both;
                    }
                    .sr-qrcode-label { font-size: 11px; color: #abedd8; margin-bottom: 8px; }
                    .sr-qrcode-img {
                        border: 2px solid rgba(171, 237, 216, 0.3);
                        border-radius: 12px;
                        box-shadow: 0 4px 12px rgba(171, 237, 216, 0.2);
                    }

                    /* 进度条倒计时（Dream 特色） */
                    .sr-countdown {
                        width: 100%%;
                        border-radius: 5px;
                        overflow: hidden;
                        height: 10px;
                        margin-top: 20px;
                        margin-bottom: 15px;
                        background: rgba(255, 255, 255, 0.1);
                        backdrop-filter: blur(5px);
                        -webkit-backdrop-filter: blur(5px);
                        box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
                        position: relative;
                        animation: fadein 0.3s ease-out 0.3s both;
                    }

                    .sr-countdown::after {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        height: 100%%;
                        width: var(--progress-width, 0%%);
                        background-color: #abedd8;
                        transition: width linear;
                        border-radius: 5px;
                        box-shadow: 0 0 10px rgba(171, 237, 216, 0.5);
                    }

                    .sr-countdown-icon { display: none; }
                    .sr-countdown-text {
                        margin-top: 12px;
                        font-size: 12px;
                        color: #666666;
                        text-align: center;
                    }

                    #countdown-num {
                        font-weight: bold;
                        color: #abedd8;
                        font-size: 14px;
                    }

                    /* 按钮组 */
                    .sr-buttons {
                        display: flex;
                        justify-content: center;
                        gap: 20%%;
                        margin-top: 20px;
                        animation: fadein 0.3s ease-out 0.35s both;
                    }

                    .sr-btn {
                        padding: 10px 24px;
                        border-radius: 16px;
                        border: none;
                        font-size: 16px;
                        font-weight: 500;
                        cursor: pointer;
                        text-decoration: none;
                        color: #ffffff;
                        background: linear-gradient(135deg, #abedd8 0%%, #74ebd5 100%%);
                        backdrop-filter: blur(4px);
                        -webkit-backdrop-filter: blur(4px);
                        transition: all 0.3s ease;
                        line-height: 20px;
                        backface-visibility: hidden;
                        transform: translateZ(0);
                        box-shadow: 0 2px 6px rgba(171, 237, 216, 0.4);
                        will-change: transform, opacity;
                    }

                    .sr-btn-primary:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px rgba(171, 237, 216, 0.6);
                        background: linear-gradient(135deg, #96ddb8 0%%, #60d5c8 100%%);
                    }

                    .sr-btn-secondary {
                        background: transparent;
                        color: #999999;
                        border: 2px solid rgba(0, 0, 0, 0.1);
                        box-shadow: none;
                    }

                    .sr-btn-secondary:hover {
                        border-color: #abedd8;
                        color: #abedd8;
                        background: rgba(171, 237, 216, 0.1);
                    }

                    /* 动画 */
                    @keyframes fadein {
                        from { opacity: 0; transform: translateY(20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }

                    @keyframes float {
                        0%%, 100%% { transform: translateY(0px); }
                        50%% { transform: translateY(-8px); }
                    }

                    /* 响应式 */
                    @media (max-width: 768px) {
                        .sr-card { width: 75%% !important; max-width: 400px !important; }
                    }

                    @media (max-width: 480px) {
                        .sr-card { padding: 24px; }
                        .sr-title { font-size: 18px; }
                        .sr-buttons { flex-direction: column; gap: 12px; }
                    }
                    """;

            case "custom":
                return """
                    /* 自定义主题 - 使用自定义 CSS */
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    /* 管理员可以在高级设置中添加自定义 CSS */
                    """;

            default:
                // 兜底：未知主题名时回退到 Dream 主题
                log.warn("Unknown theme '{}', falling back to 'dream'", theme);
                return buildThemeStyles("dream");
        }
    }

    /**
     * 构建自定义背景覆盖样式
     * <p>
     * 支持两种模式：
     * <ul>
     *   <li>仅设置 backgroundUrl：使用背景图片覆盖主题默认背景</li>
     *   <li>设置 backgroundColor：作为叠加层或纯色背景</li>
     * </ul>
     * 此方法的 CSS 优先级高于 buildThemeStyles() 中的 body 背景
     */
    private String buildBackgroundOverride(String backgroundUrl, String backgroundColor) {
        StringBuilder css = new StringBuilder();

        boolean hasBgImage = (backgroundUrl != null && !backgroundUrl.trim().isEmpty());
        boolean hasBgColor = (backgroundColor != null && !backgroundColor.trim().isEmpty());

        if (!hasBgImage && !hasBgColor) {
            return "";
        }

        css.append("    /* 自定义背景覆盖 */\n");

        if (hasBgImage || hasBgColor) {
            css.append("    body {\n");

            if (hasBgImage && backgroundUrl != null) {
                String safeUrl = escapeHtml(backgroundUrl.trim());
                css.append("      background-image: url('").append(safeUrl).append("') !important;\n");
                css.append("      background-size: cover !important;\n");
                css.append("      background-position: center !important;\n");
                css.append("      background-repeat: no-repeat !important;\n");
                css.append("      background-attachment: fixed !important;\n");
            }

            if (hasBgColor && backgroundColor != null) {
                String safeColor = backgroundColor.trim();
                // 检测是否是渐变（包含 gradient 关键字）
                if (safeColor.toLowerCase().contains("gradient")) {
                    // 如果是渐变且没有背景图片，直接作为背景
                    if (!hasBgImage) {
                        css.append("      background: ").append(safeColor).append(" !important;\n");
                    } else {
                        // 如果有背景图片，颜色作为半透明叠加层
                        css.append("      background-color: ").append(safeColor).append(" !important;\n");
                        css.append("      background-blend-mode: overlay !important;\n");
                    }
                } else {
                    // 纯色背景
                    if (!hasBgImage) {
                        css.append("      background: ").append(safeColor).append(" !important;\n");
                    } else {
                        css.append("      background-color: ").append(safeColor).append(" !important;\n");
                        css.append("      background-blend-mode: overlay !important;\n");
                    }
                }
            }

            css.append("    }\n\n");

            // 隐藏粒子动画画布（自定义背景时通常不需要）
            if (hasBgImage) {
                css.append("    canvas#particle-canvas { display: none !important; }\n\n");
            }
        }

        return css.toString();
    }
}

