package tv.game88.wallet.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode( callSuper = true )
public class ReqWithdrawOrder extends ReqOrderBase {
    @NotBlank( message = "用户的钱包地址不能为空" )
    private String walletAddress;
    @NotNull( message = "金币数量不能为空" )
    @Min( 1 )
    private Long   amount;

    private String notifyUrl;
    private String merchantPlatformId;
    private String remark;
}
