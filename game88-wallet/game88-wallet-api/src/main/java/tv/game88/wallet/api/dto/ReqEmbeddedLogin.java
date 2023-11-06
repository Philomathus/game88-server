package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReqEmbeddedLogin {
    @Schema( title = "商户号", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotNull
    private Long   merchantId;
    @Schema( title = "用户手机号", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 11, minLength = 11 )
    @NotBlank
    @Length( max = 11, min = 11 )
    private String phone;
    @Schema( title = "用户钱包地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED )
    private String walletAddress;
    @Schema( title = "签名数据", requiredMode = Schema.RequiredMode.REQUIRED )
    private String sign;
}
