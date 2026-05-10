package run.halo.saferedirect;

import lombok.Data;

/**
 * 基本设置模型，对应 setting.yaml 中 group=basic
 */
@Data
public class BasicSetting {
    private Object pageTitle = "安全跳转提示";
    private Object siteName = "我的博客";
    private Object whitelistDomains = "";
    private Object enabled = "true";

    public String getPageTitle() {
        return pageTitle == null ? "安全跳转提示" : String.valueOf(pageTitle);
    }

    public String getSiteName() {
        return siteName == null ? "我的博客" : String.valueOf(siteName);
    }

    public String getWhitelistDomains() {
        return whitelistDomains == null ? "" : String.valueOf(whitelistDomains);
    }

    public boolean isEnabled() {
        if (enabled instanceof Boolean) {
            return (Boolean) enabled;
        }
        return "true".equalsIgnoreCase(String.valueOf(enabled));
    }
}
