package tv.game88.wallet.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode( callSuper = true )
public class ReqDepositOrder extends ReqOrderBase {
    @NotBlank( message = "用户的钱包地址不能为空" )
    private String walletAddress;
    @NotNull( message = "金币数量不能为空" )
    @Min( 1 )
    private Long   amount;

    private String notifyUrl;
    private String remark;
}
