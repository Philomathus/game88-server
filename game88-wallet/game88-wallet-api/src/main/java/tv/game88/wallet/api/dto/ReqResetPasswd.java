package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqResetPasswd {
    @Schema( title = "旧密码" )
    @NotBlank
    private String oldPasswd;
    @Schema( title = "新密码" )
    @NotBlank
    private String newPasswd;

    @Schema( hidden = true )
    private String passwordEncrypt;
}
