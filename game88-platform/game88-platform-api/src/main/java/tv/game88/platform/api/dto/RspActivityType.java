package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RspActivityType {
    @Schema( title = "默认显示活动列表" )
    public  List<RspActivityInfo> activityList = new ArrayList<>();
    @Schema( title = "活动类型ID" )
    private Long                  id;
    @Schema( title = "类型名称" )
    private String                name; //名称
}
