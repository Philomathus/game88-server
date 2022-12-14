package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspVipPayLogin {
    @Schema( title = "H5跳转地址" )
    private String     url;
    @Schema( title = "钱包地址" )
    private String     walletAddress;
    @Schema( title = "vipPay余额" )
    private BigDecimal balance;
}
