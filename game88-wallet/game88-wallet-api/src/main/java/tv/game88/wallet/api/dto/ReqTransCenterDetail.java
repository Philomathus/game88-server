package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqTransCenterDetail extends PageDomain {
    @Schema( title = "收款方式类型" )
    private String  payMethodType;
    @Schema( title = "是否可拆分" )
    private Boolean canSplit;
    @Schema( title = "售卖数量" )
    private Long    amount;
}
