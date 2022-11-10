package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RspVipInfo {
    @Schema( title = "vip特权列表" )
    private List<RspVipSet> vipSetList;

    @Schema( title = "晋级彩金状态(0不可领取1未领取2已领取)" )
    private Integer levelBonusStatus = 0;
    @Schema( title = "周俸禄状态(0不可领取1未领取2已领取)" )
    private Integer weekBonusStatus  = 0;
    @Schema( title = "月俸禄状态(0不可领取1未领取2已领取)" )
    private Integer monthBonusStatus = 0;
}
