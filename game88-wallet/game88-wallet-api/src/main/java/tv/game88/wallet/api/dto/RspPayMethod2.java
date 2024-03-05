package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

@Data
public class RspPayMethod2 {
    @Schema( title = "支付方式ID" )
    private Long   methodId;
    @Schema( title = "银行名称" )
    private String bankName;
    @Schema( title = "开户所在地 - 省" )
    private String payAddrProvince;
    @Schema( title = "开户所在地 - 市" )
    private String payAddrCity;
    @Schema( title = "银行卡号或支付宝账号" )
    private String bankAccount;
    @Schema( title = "真实姓名" )
    private String realName;
    @Schema( title = "收款码图片地址" )
    private String payPicAddr;

    @Schema( title = "卡类型" )
    private WalletPayMethodEnum type;
}
