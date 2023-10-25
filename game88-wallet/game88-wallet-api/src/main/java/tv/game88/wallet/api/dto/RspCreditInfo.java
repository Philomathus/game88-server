package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspCreditInfo {
    @Schema( title = "买单次数" )
    private Long    buyOrderNum;
    @Schema( title = "卖单次数" )
    private Long    sellOrderNum;
    @Schema( title = "30日成单数" )
    private Integer successNumMonth;
    @Schema( title = "30日成单率" )
    private String  successRateMonth;
    @Schema( title = "30日平均付款时间" )
    private String  receivedTimeMonth;
    @Schema( title = "30日平均放币时间" )
    private String  transferTimeMonth;
}
