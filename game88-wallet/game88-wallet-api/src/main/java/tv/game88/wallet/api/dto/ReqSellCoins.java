package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqSellCoins extends ReqFundPass {
    @Schema( title = "出售数量", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotNull( message = "出售数量不能为空" )
    @Min( value = 10, message = "最低出售数量为10G币" )
    private Long       sellNum;
    @Schema( title = "最低购买数量" )
    private Long       minBuyNum;
    @Schema( title = "收款方式", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotNull( message = "请选择收款方式" )
    private List<Long> payMethodIds;
    @Schema( title = "是否拆分", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotNull( message = "请选择是否拆分" )
    private Boolean    canSplit;
}
