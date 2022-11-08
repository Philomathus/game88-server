package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RspQuestType {
    @Schema( title = "默认显示任务列表" )
    public  List<RspQuestInfo> activityList = new ArrayList<>();
    @Schema( title = "任务类型ID" )
    private Long               id;
    @Schema( title = "任务名称" )
    private String             name;
}
