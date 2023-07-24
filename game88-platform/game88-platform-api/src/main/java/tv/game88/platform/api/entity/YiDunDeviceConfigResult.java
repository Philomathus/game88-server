package tv.game88.platform.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YiDunDeviceConfigResult {
    private String deviceId;
    private Integer sdkType;
    private Map<String, Integer> checkResult;
    private String serializedCheckResult;
}
