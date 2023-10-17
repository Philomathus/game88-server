package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import javax.validation.constraints.Min;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqTransCenterDetail extends PageDomain {
    @Schema( title = "收款方式类型" )
    private WalletPayMethodEnum payMethodType;
    @Schema( title = "是否可拆分" )
    private Boolean             canSplit;
    @Schema( title = "最小售卖数量" )
    @Min( 1 )
    private Long                minAmount;
    @Schema( title = "最大售卖数量" )
    @Min( 1 )
    private Long                maxAmount;
}
