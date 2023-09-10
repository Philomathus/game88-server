package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class MobileBind {
    @Schema( title = "手机号" )
    @NotBlank
    @Length( max = 11, min = 11 )
    private String mobile;
    @Schema( title = "验证码" )
    private String code;
    @Schema( title = "登录密码" )
    @NotBlank
    private String passwd;

    @Schema( hidden = true )
    private String passwordEncrypt;
}
