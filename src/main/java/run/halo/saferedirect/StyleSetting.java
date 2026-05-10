package run.halo.saferedirect;

import lombok.Data;

/**
 * 样式设置模型，对应 setting.yaml 中 group=style
 */
@Data
public class StyleSetting {
    private Object theme = "minimal";
    private Object countdown = "5";
    private Object showTargetUrl = "true";
    private Object showQrCode = "false";
    private Object iconUrl = "";
    private Object customHtml = "";
    private Object backgroundUrl = "";
    private Object backgroundColor = "";
    private Object buttonMode = "both";

    public String getTheme() {
        return theme == null ? "minimal" : String.valueOf(theme);
    }

    public String getIconUrl() {
        return iconUrl == null ? "" : String.valueOf(iconUrl);
    }

    public String getCustomHtml() {
        return customHtml == null ? "" : String.valueOf(customHtml);
    }

    public String getBackgroundUrl() {
        return backgroundUrl == null ? "" : String.valueOf(backgroundUrl);
    }

    public String getBackgroundColor() {
        return backgroundColor == null ? "" : String.valueOf(backgroundColor);
    }

    public String getButtonMode() {
        return buttonMode == null ? "both" : String.valueOf(buttonMode);
    }

    public int getCountdown() {
        if (countdown instanceof Number) {
            return ((Number) countdown).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(countdown));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public boolean isShowTargetUrl() {
        if (showTargetUrl instanceof Boolean) {
            return (Boolean) showTargetUrl;
        }
        if (showTargetUrl instanceof Number) {
            return ((Number) showTargetUrl).intValue() != 0;
        }
        String strVal = String.valueOf(showTargetUrl);
        return "true".equalsIgnoreCase(strVal)
            || "yes".equalsIgnoreCase(strVal)
            || "1".equals(strVal);
    }

    public boolean isShowQrCode() {
        if (showQrCode instanceof Boolean) {
            return (Boolean) showQrCode;
        }
        if (showQrCode instanceof Number) {
            return ((Number) showQrCode).intValue() != 0;
        }
        String strVal = String.valueOf(showQrCode);
        return "true".equalsIgnoreCase(strVal)
            || "yes".equalsIgnoreCase(strVal)
            || "1".equals(strVal);
    }
}
