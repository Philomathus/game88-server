package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RspBuyOrderDetail {
    @Schema( title = "昵称" )
    private String  nikeName;
    @Schema( title = "等级" )
    private Integer level;
    @Schema( title = "头像" )
    private String  headImg;
    @Schema( title = "买单次数" )
    private Long    buyOrderNum;
    @Schema( title = "卖单次数" )
    private Long    sellOrderNum;

    @Schema( title = "挂单ID" )
    private String  transactionId;
    @Schema( title = "可购买总额(G币)" )
    private Long    amount;
    @Schema( title = "是否可拆分" )
    private Boolean canSplit;
    @Schema( title = "30日成单数" )
    private Integer successNumMonth;
    @Schema( title = "30日成单率" )
    private String  successRateMonth;
    @Schema( title = "30日平均付款时间" )
    private String  receivedTimeMonth;
    @Schema( title = "30日平均放币时间" )
    private String  transferTimeMonth;
    @Schema( title = "卖家收款方式类型", description = "英文逗号,分割" )
    private String  payMethodTypes;

    @Schema( title = "买家收款方式" )
    private Map<String, RspPayMethod2> rspPayMethodMap = new HashMap<>();
}
