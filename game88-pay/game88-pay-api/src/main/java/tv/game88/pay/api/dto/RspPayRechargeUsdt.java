package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class RspPayRechargeUsdt {
    @Schema( title = "USDT渠道系统编号" )
    private String     id;
    @Schema( title = "渠道名称" )
    private String     channelName;
    @Schema( title = "钱包二维码" )
    private String     icon;
    @Schema( title = "链名称" )
    private String     chainName;
    @Schema( title = "充值地址" )
    private String     rechargeAddress;
    @Schema( title = "usdt汇率" )
    private String     exchangeRate;
    @Schema( title = "优惠比例" )
    private BigDecimal discountBill;
    @Schema( title = "优惠比例格式化" )
    private String     discountBillStr;

    public void setDiscountBill( BigDecimal discount_bill ) {
        if ( discount_bill != null ) {
            this.discountBillStr = discount_bill.multiply( BigDecimal.valueOf( 100 ) ).setScale( 1, RoundingMode.HALF_UP )
                    .toString().concat( "%" );
        }
        this.discountBill = discount_bill;
    }
}
