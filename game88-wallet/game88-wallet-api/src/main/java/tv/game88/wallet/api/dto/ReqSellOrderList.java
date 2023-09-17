package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqSellOrderList extends PageDomain {
    @Schema( title = "收款方式" )
    private String  payMethodType;
    @Schema( title = "挂单状态", description = "0挂单中 1交易中" )
    private Integer status;
    @Schema( title = "是否可拆分" )
    private Boolean canSplit;

    @Hidden
    private String userId;
}
