package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class RspLotteryInfo {
    @Schema( title = "彩种编号" )
    private Integer id;
    @Schema( title = "彩种名称" )
    private String  name;
    @Schema( title = "类型" )
    private String  type;
    @Schema( title = "图标" )
    private String  icon;
    @Schema( title = "周期" )
    private Integer cycle;

}
