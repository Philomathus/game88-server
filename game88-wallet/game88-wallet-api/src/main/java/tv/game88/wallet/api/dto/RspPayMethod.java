package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspPayMethod {
    @Schema( title = "支付方式ID" )
    private Integer methodId;
    @Schema( title = "银行名称" )
    private String  bankName;
    @Schema( title = "银行图标" )
    private String  bankIcon;
    @Schema( title = "银行卡号或支付宝账号" )
    private String  account;
    @Schema( title = "真实姓名" )
    private String  realName;
    @Schema( title = "收款码图片地址" )
    private String  payPicAddr;
}
