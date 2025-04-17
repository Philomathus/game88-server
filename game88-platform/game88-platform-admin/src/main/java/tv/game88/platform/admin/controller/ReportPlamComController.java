package tv.game88.platform.admin.controller;

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
import tv.game88.platform.api.entity.ReportPlamCom;
import tv.game88.platform.api.service.ReportPlamComService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间Controller
 *
 * @author 77tv
 * @date 2021-01-25
 */
@RestController
@RequestMapping( "/report/report-plam-com" )
@Log4j2
public class ReportPlamComController extends BaseController {
    @Resource
    private ReportPlamComService reportPlamComService;

    /**
     * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     */
    @PreAuthorize( "@ss.hasPermi('report:plam-com:list')" )
    @GetMapping( "/list" )
    public Object list( ReportPlamCom reportPlamCom ) throws ParseException {
        return reportPlamComService.selectReportPlamComList( reportPlamCom );
    }

    /**
     * 导出综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     */
    @PreAuthorize( "@ss.hasPermi('report:plam-com:export')" )
    @Log( title = "综合数据", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export(ReportPlamCom reportPlamCom) {
        List<ReportPlamCom> list = reportPlamComService.exportPlamComList( reportPlamCom );
        return RspBase.ok(list);
    }
}