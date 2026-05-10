package run.halo.saferedirect;

import lombok.Data;

/**
 * 基本设置模型，对应 setting.yaml 中 group=basic
 */
@Data
public class BasicSetting {
    private String pageTitle = "安全跳转提示";
    private String siteName = "我的博客";
    private String whitelistDomains = "";
    private String enabled = "true";

    public boolean isEnabled() {
        return "true".equals(enabled);
    }
}
