package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;

@Data
public class MobileLogin extends MobileBind {
    @Schema( title = "邀请码" )
    private String inviterCode;
    @Schema( title = "渠道号(可选字段)" )
    private String channelCode;
    @Schema( title = "设备ID" )
    private String deviceId;
    @Schema( title = "ip" )
    private String ip;
    @Schema( title = "手机型号" )
    private String phoneModel;

    @Schema( title = "提交二次校验的验证数据，NECaptchaValidate值" )
    private String validate;

    public String getIp() {
        if ( StringUtils.isBlank( ip ) ) {
            return ServletUtil.getIp();
        }
        return ip;
    }
}
