package run.halo.saferedirect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 路由配置类
 *
 * <p>注册插件的 HTTP 路由到 Spring WebFlux 路由系统。
 *
 * @author SafeRedirect Team
 * @since 1.0.0
 */
@Configuration
public class SafeRedirectConfig {

    @Bean
    public RouterFunction<ServerResponse> safeRedirectRoutes(SafeRedirectRouter router) {
        return router.route();
    }
}
