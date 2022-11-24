package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RspCleanCodeInfo {

    @Schema( title = "洗码量总量" )
    private BigDecimal codeAmountTotal;
    @Schema( title = "洗码金额" )
    private BigDecimal cleanAmountTotal;
    @Schema( title = "上次结算时间" )
    private String     cleanTime;

    @Schema( title = "游戏类型列表" )
    private List<RspGameCategory> rspGameCategoryList;

    public String getCleanTime() {
        if ( null == cleanTime ) {
            return "";
        }
        return cleanTime;
    }
}
