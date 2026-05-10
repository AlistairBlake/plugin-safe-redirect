package run.halo.saferedirect;

import lombok.Data;

/**
 * 高级设置模型，对应 setting.yaml 中 group=advanced
 */
@Data
public class AdvancedSetting {
    private Object customCss = "";
    private Object customTip = "";
    private Object trackOutbound = "false";

    public String getCustomCss() {
        return customCss == null ? "" : String.valueOf(customCss);
    }

    public String getCustomTip() {
        return customTip == null ? "" : String.valueOf(customTip);
    }

    public boolean isTrackOutbound() {
        if (trackOutbound instanceof Boolean) {
            return (Boolean) trackOutbound;
        }
        return "true".equalsIgnoreCase(String.valueOf(trackOutbound));
    }
}
