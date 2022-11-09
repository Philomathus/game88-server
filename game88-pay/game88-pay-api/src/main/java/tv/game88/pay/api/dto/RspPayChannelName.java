package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspPayChannelName {
    @Schema( title = "主键" )
    private Long       id;
    @Schema( title = "通道名称" )
    private String     channelName;
    @Schema( title = "平台名称" )
    private String     platformName;
    @Schema( title = "通道费率" )
    private BigDecimal payRate;
}
