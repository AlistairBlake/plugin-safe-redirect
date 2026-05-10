package run.halo.saferedirect;

import lombok.Data;

/**
 * 样式设置模型，对应 setting.yaml 中 group=style
 */
@Data
public class StyleSetting {
    private String theme = "dream";
    private Object countdown = "5";
    private Object showTargetUrl = "true";
    private Object showQrCode = "false";
    private String iconUrl = "";
    private String customHtml = "";
    private String backgroundUrl = "";
    private String backgroundColor = "";

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
        return "true".equalsIgnoreCase(String.valueOf(showTargetUrl));
    }

    public boolean isShowQrCode() {
        if (showQrCode instanceof Boolean) {
            return (Boolean) showQrCode;
        }
        return "true".equalsIgnoreCase(String.valueOf(showQrCode));
    }
}
