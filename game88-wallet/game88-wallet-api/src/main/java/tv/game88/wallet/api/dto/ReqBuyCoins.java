package tv.game88.wallet.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqBuyCoins extends ReqTransaction {
    @NotNull( message = "购买数量" )
    @Min( 10 )
    private Long amount;
    @NotNull( message = "买家交易方式ID" )
    private Long payMethodId;
}
