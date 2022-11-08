package tv.game88.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspQuestInfo {
    @Schema( title = "任务ID" )
    private String        id;
    @Schema( title = "游戏ID" )
    private Long          gameId;
    @Schema( title = "图标" )
    private String        icon;
    @Schema( title = "标题" )
    private String        title;
    @Schema( title = "描述" )
    private String        content;
    @Schema( title = "任务详情" )
    private String        detail;
    @Schema( title = "目标任务量" )
    private Integer       target;
    @Schema( title = "完成后增加的资金" )
    private BigDecimal    reward;
    @Schema( title = "当前任务数量" )
    private Integer       curNum     = 0;
    @Schema( title = "0=进行中1=已经完成2 领奖完成" )
    private Integer       status     = 0;
    @Schema( title = "平台ID(1=开元棋牌2=OG视讯 4=彩票)" )
    private Integer       platformId = 0;
    @Schema( title = "发布时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    private Long          typeId;
    private String        kindId;

}
