package tv.game88.wallet.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode( callSuper = true )
public class ReqDepositOrder extends ReqOrderBase {
    @NotNull( message = "金币数量不能为空" )
    @DecimalMin( "1" )
    private BigDecimal amount;

    private String notifyUrl;
    private String remark;
}
