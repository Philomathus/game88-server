package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.page.PageDomain;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.enums.EnumReqTime;

@Data
@Schema( title = "账户明细请求数据" )
public class ReqLogMoney extends PageDomain {
    @Schema( title = "交易状态" )
    private EnumMoney   enumMoney;
    @Schema( title = "交易时间" )
    private EnumReqTime enumReqTime;
}
