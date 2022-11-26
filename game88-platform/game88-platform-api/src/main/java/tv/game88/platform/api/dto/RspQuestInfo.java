package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspQuestInfo {
    @Schema( title = "任务ID" )
    private Long       id;
    @Schema( title = "图标" )
    private String     icon;
    @Schema( title = "标题" )
    private String     title;
    @Schema( title = "描述" )
    private String     content;
    @Schema( title = "目标任务量" )
    private Integer    target;
    @Schema( title = "完成后增加的资金" )
    private BigDecimal reward;
    @Schema( title = "当前任务数量" )
    private Integer    curNum     = 0;
    @Schema( title = "0=进行中1=已经完成2 领奖完成" )
    private Integer    status     = 0;
    @Schema( title = "平台ID" )
    private Integer    platformId = 0;
    @Schema( title = "游戏ID" )
    private Long       infoId;

    private Long typeId;

}
