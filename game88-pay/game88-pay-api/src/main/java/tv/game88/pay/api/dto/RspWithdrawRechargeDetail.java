package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspWithdrawRechargeDetail {
    @Schema( title = "充值/提现金额" )
    private BigDecimal money;
    @Schema( title = "订单号" )
    private String     orderNo;
    @Schema( title = "申请时间" )
    private String     requestTime;
    @Schema( title = "银行名称" )
    private String     bankName;
    @Schema( title = "订单状态" )
    private Integer    status;
    @Schema( title = "备注" )
    private String     remark;
    @Schema( title = "颜色" )
    private String     color;
}
