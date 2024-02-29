package tv.game88.wallet.api.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.math.BigDecimal;

public class ReqConstant {

    public record ReqPayMethodId(@Schema( title = "支付方式ID" ) int payMethodId) {}
    public record ReqHasPay( String methodType ) {}

    public record ReqSetPasswd(
            @Schema( title = "新密码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotBlank( message = "新密码不能为空" ) String password,
            @Schema( title = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotBlank( message = "确认密码不能为空" ) String confirmPassword) {}

    public record ReqResetFundPasswd(
            @Schema( title = "资金老密码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotBlank( message = "资金老密码不能为空" ) String fundOldPass,
            @Schema( title = "资金新密码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotBlank( message = "资金新密码不能为空" ) String fundNewPass) {}

    public record ReqMerchantAddScore(Long merchantId, Integer googleAuthCode, BigDecimal score) {}

    public record ReqMerchantOtpCode(
            @Schema( title = "商户号", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "商户ID不能为空" ) Long merchantId,
            @Schema( title = "MFA验证码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "MFA验证码不能为空" ) Integer otpAuthCode) {}

    public record ReqBoundMerchantOtpSecret(
            @Schema( title = "商户号", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "商户ID不能为空" ) Long merchantId,
            @Schema( title = "MFA验证码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "MFA验证码不能为空" ) Integer otpAuthCode,
            @Schema( title = "MFA密钥", requiredMode = Schema.RequiredMode.REQUIRED ) @NotBlank( message = "MFA密钥不能为空" ) String otpAuthKey) {}

    public record ReqMerchantChangeStatus(
            @Schema( title = "商户号", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "商户ID不能为空" ) Long merchantId,
            @Schema( title = "MFA验证码", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "MFA验证码不能为空" ) Integer otpAuthCode,
            @Schema( title = "状态", requiredMode = Schema.RequiredMode.REQUIRED ) @NotNull( message = "状态不能为空" ) Integer status) {}
}
