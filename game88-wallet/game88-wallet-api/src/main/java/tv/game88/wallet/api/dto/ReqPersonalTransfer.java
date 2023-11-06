package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqPersonalTransfer extends ReqFundPass {
    @Schema( title = "用户钱包地址" )
    @NotBlank( message = "用户钱包地址不能为空" )
    private String walletUserAddress;

    @Schema( title = "用户钱包地址" )
    @NotNull( message = "G币数量不能为空" )
    @Min( value = 1, message = "最低转账数量1 G币" )
    private Long amount;
}
