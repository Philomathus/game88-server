package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspPayMethod2 {
    @Schema( title = "银行名称" )
    private String bankName;
    @Schema( title = "开户所在地 - 省" )
    private String payAddrProvince;
    @Schema( title = "开户所在地 - 市" )
    private String payAddrCity;
    @Schema( title = "银行卡号或支付宝账号" )
    private String account;
    @Schema( title = "真实姓名" )
    private String realName;
    @Schema( title = "收款码图片地址" )
    private String payPicAddr;
}
