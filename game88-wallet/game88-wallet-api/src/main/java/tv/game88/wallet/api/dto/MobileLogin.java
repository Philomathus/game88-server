package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;

@EqualsAndHashCode( callSuper = true )
@Data
public class MobileLogin extends MobileBind {
    @Schema( title = "设备ID" )
    private String deviceId;
    @Schema( title = "ip" )
    private String ip;
    @Schema( title = "手机型号" )
    private String phoneModel;

    public String getIp() {
        if ( StringUtils.isBlank( ip ) ) {
            return ServletUtil.getIp();
        }
        return ip;
    }
}
