package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqResetPasswd {
    @Schema( title = "旧密码" )
    private String oldPasswd;
    @Schema( title = "新密码" )
    private String newPasswd;

    @Schema( hidden = true )
    private String passwordEncrypt;
}
