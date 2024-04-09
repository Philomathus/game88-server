 package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdCardDto {
    @Schema( title = "真实姓名" )
    private String realName;
    @Schema( title = "身份证号码" )
    private String idCardNumber;
    @Schema( title = "身份证正面" )
    private String idFrontPic;
    @Schema( title = "身份证反面" )
    private String idBackPic;
}
