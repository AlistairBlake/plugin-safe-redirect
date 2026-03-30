package run.halo.saferedirect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * 安全链接跳转插件主类
 *
 * <p>功能说明：
 * <ul>
 *   <li>拦截文章页面的外部链接点击</li>
 *   <li>显示安全跳转中间提示页面</li>
 *   <li>支持多种视觉主题（默认/极简/科技/温暖/自定义）</li>
 *   <li>支持白名单域名直接跳转</li>
 *   <li>支持倒计时自动跳转</li>
 * </ul>
 *
 * @author SafeRedirect Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class SafeRedirectPlugin extends BasePlugin {

    public SafeRedirectPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        log.info("Plugin [safe-redirect] v{} started — 安全链接跳转已启用",
            getContext().getVersion());
    }

    @Override
    public void stop() {
        log.info("Plugin [safe-redirect] stopped — 安全链接跳转已停用");
    }
}
