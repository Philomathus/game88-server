package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReqFundPass {
    @Schema( title = "资金密码", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotBlank( message = "资金密码不能为空" )
    private String fundPass;
}
