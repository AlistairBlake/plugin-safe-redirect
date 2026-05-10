package run.halo.saferedirect;

import lombok.Data;

/**
 * 样式设置模型，对应 setting.yaml 中 group=style
 */
@Data
public class StyleSetting {
    private String theme = "dream";
    private String countdown = "5";
    private String showTargetUrl = "true";
    private String showQrCode = "false";
    private String iconUrl = "";
    private String customHtml = "";
    private String backgroundUrl = "";
    private String backgroundColor = "";

    public int getCountdown() {
        try {
            return Integer.parseInt(countdown);
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public boolean isShowTargetUrl() {
        return "true".equals(showTargetUrl);
    }

    public boolean isShowQrCode() {
        return "true".equals(showQrCode);
    }
}
