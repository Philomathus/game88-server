package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MobileBind {
    @Schema( title = "手机号" )
    private String mobile;
    @Schema( title = "验证码" )
    private String code;
    @Schema( title = "登录密码" )
    private String passwd;

    @Schema( hidden = true )
    private String passwordEncrypt;
}
