package run.halo.saferedirect;

import lombok.Data;

/**
 * 高级设置模型，对应 setting.yaml 中 group=advanced
 */
@Data
public class AdvancedSetting {
    private String customCss = "";
    private String customTip = "";
    private boolean trackOutbound = false;
}
