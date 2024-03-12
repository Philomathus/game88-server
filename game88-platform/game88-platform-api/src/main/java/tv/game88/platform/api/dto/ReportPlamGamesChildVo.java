package tv.game88.platform.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportPlamGamesChildVo {
    @Excel(name = "会员盈利")
    private String gameprofit;

    @Excel(name = "总投注金额")
    private String gamecell;

    @Excel(name = "子平台编号")
    private String agentchild;
}
