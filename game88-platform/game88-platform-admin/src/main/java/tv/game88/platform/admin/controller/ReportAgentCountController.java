package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.dto.ReportPlamHome;
import tv.game88.platform.api.entity.ReportAgentcount;
import tv.game88.platform.api.service.ReportAgentcountService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理统计，主要用于代理渠道的统计Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/report/agentCount" )
public class ReportAgentCountController extends BaseController {
    @Resource
    private ReportAgentcountService reportAgentcountService;

    /**
     * 首页注册人数
     */
    @PostMapping( value = "/record" )
    public RspBase record( HttpServletRequest request, Map map ) {
        String time;
        if ( map.containsKey( "time" ) ) {
            time = map.get( "time" ).toString();
        } else {
            time = LocalDateTimeUtils.format( LocalDate.now() );
        }
        //查询出款人数classTwo=106
        List<ReportPlamHome> classTwo106 = reportAgentcountService.findChartsOne( "106", time );

        List<ReportPlamHome> classTwo105 = reportAgentcountService.findChartsOne( "105", time );

        List<ReportPlamHome> classTwo104 = reportAgentcountService.findChartsOne( "104", time );

        List<ReportPlamHome> classTwo103 = reportAgentcountService.findChartsOne( "103", time );

        List<ReportPlamHome> classTwox212 = reportAgentcountService.findChartsOne( "212", time );

        List<ReportPlamHome> classTwox102 = reportAgentcountService.findChartsOne( "102", time );

        List<ReportPlamHome> classTwox211 = reportAgentcountService.findChartsOne( "211", time );

        List<ReportPlamHome> classTwox101 = reportAgentcountService.findChartsOne( "101", time );

        List<ReportPlamHome> classTwox210 = reportAgentcountService.findChartsOne( "210", time );

        List<ReportPlamHome> classTwox110 = reportAgentcountService.findChartsOne( "110", time );

        List<ReportPlamHome> classTwox209 = reportAgentcountService.findChartsOne( "209", time );

        List<ReportPlamHome> classTwox208 = reportAgentcountService.findChartsOne( "208", time );

        List<ReportPlamHome> classTwox109 = reportAgentcountService.findChartsOne( "109", time );

        List<ReportPlamHome> classTwox108 = reportAgentcountService.findChartsOne( "108", time );

        List<ReportPlamHome> classTwox107 = reportAgentcountService.findChartsOne( "107", time );
        Map<String, Object>  resultMap    = new HashMap<>();
        resultMap.put( "x106", classTwo106 );
        resultMap.put( "x105", classTwo105 );
        resultMap.put( "x104", classTwo104 );
        resultMap.put( "x103", classTwo103 );
        resultMap.put( "x212", classTwox212 );
        resultMap.put( "x102", classTwox102 );
        resultMap.put( "x211", classTwox211 );
        resultMap.put( "x101", classTwox101 );
        resultMap.put( "x210", classTwox210 );
        resultMap.put( "x110", classTwox110 );
        resultMap.put( "x209", classTwox209 );
        resultMap.put( "x208", classTwox208 );
        resultMap.put( "x109", classTwox109 );
        resultMap.put( "x108", classTwox108 );
        resultMap.put( "x107", classTwox107 );
        return RspBase.ok( resultMap );
    }

    /**
     * 查询代理统计，主要用于代理渠道的统计列表
     */
    @PreAuthorize( "@ss.hasPermi('report:agentCount:list')" )
    @GetMapping( "/list" )
    public Object list( ReportAgentcount reportAgentcount ) throws Exception {
        return reportAgentcountService.selectReportAgentcountList( reportAgentcount );
    }

    @PreAuthorize( "@ss.hasPermi('report:agentCount:export')" )
    @Log( title = "推广统计报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportAgentcount reportAgentcount, HttpServletResponse response ) throws ParseException {
        List<ReportAgentcount> list = reportAgentcountService.exportAgentcountList( reportAgentcount );
        ExportExcelUtil.exportExcel( list, "推广统计报表", "推广统计报表", ReportAgentcount.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('report:agentCount:generateData')" )
    @GetMapping( "/generateData" )
    public RspBase<?> generateData( ReportAgentcount reportAgentcount ) throws ParseException {
        return reportAgentcountService.plamagent_data( reportAgentcount );
    }

}
