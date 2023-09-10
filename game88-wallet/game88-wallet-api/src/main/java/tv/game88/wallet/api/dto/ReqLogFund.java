package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;
import tv.game88.wallet.api.type.EnumReqTime;
import tv.game88.wallet.api.type.WalletUserFundEnum;

@EqualsAndHashCode( callSuper = true )
@Data
@Schema( title = "用户资金明细请求数据" )
public class ReqLogFund extends PageDomain {
    @Schema( title = "交易状态" )
    private WalletUserFundEnum enumFund;
    @Schema( title = "交易时间" )
    private EnumReqTime        enumReqTime;
}
