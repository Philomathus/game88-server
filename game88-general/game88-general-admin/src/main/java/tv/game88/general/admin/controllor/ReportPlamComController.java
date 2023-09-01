package tv.game88.general.admin.controllor;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.general.api.entity.Agent;
import tv.game88.general.api.entity.ReportPlamCom;
import tv.game88.general.api.service.AgentService;
import tv.game88.general.api.service.IReportPlamComService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间Controller
 *
 * @author 77tv
 * @date 2021-01-25
 */
@RestController
@RequestMapping( "/admin/report-plam-com" )
@Log4j2
public class ReportPlamComController extends BaseController {
    @Resource
    private AgentService          agentService;
    @Resource
    private IReportPlamComService reportPlamComService;

    /**
     * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:report-plam-com:list')" )
    @GetMapping( "/list" )
    public Object list( ReportPlamCom reportPlamCom ) {
        log.warn( reportPlamCom.getAgentPlatform() );
        return reportPlamComService.selectReportPlamComList( reportPlamCom );
    }

    /**
     * 导出综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:report-plam-com:export')" )
    @Log( title = "综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportPlamCom reportPlamCom, HttpServletResponse response ) {
        List<ReportPlamCom> list = reportPlamComService.exportPlamComList( reportPlamCom );
        ExportExcelUtil.exportExcel( list, "综合数据报表", "综合数据报表", ReportPlamCom.class, response );
    }

    /* 代理平台选择列表
     *
     * @return
     */
    @GetMapping( "/agentPlatform" )
    public RspBase<List<Agent>> findEffectPayPlatform() {
        List<Agent> list = agentService.selectAllAgentList();
        return RspBase.ok( list );
    }
}
