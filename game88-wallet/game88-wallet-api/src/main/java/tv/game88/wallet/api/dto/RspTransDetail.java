package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.wallet.api.type.EnumTransDetail;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.time.LocalDateTime;

@Data
public class RspTransDetail {
    @Schema( title = "交易编号" )
    private String          transDetailId;
    @Schema( title = "交易数量" )
    private Long            amount;
    @Schema( title = "状态" )
    private WalletTransEnum status;
    @Schema( title = "交易时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime   time;
    @Schema( title = "买卖类型" )
    private EnumTransDetail type;
}
