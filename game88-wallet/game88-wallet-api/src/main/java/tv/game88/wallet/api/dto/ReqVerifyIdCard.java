package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReqVerifyIdCard {
    @Schema( title = "真实姓名" )
    @NotBlank
    private String realName;
    @Schema( title = "身份证号码" )
    @NotBlank
    private String idCardNumber;
    @Schema( title = "身份证正面" )
    @NotBlank
    private String idFrontPic;
    @Schema( title = "身份证反面" )
    @NotBlank
    private String idBackPic;
}
