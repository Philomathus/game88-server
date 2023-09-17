package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class RspSellOrderDetail {
    @Schema( title = "挂单编号" )
    private String                     transactionId;
    @Schema( title = "挂单状态", description = "0挂单中 1挂单中 2已售罄 3取消挂单" )
    private Integer                    status;
    @Schema( title = "售卖数量" )
    private Long                       amount;
    @Schema( title = "是否可拆分" )
    private Boolean                    canSplit;
    @Schema( title = "创建时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime              createTime;
    @Schema( title = "收款方式" )
    private Map<String, RspPayMethod2> rspPayMethodMap = new HashMap<>();
}
