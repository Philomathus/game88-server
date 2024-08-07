package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.member.dto.RspConfigTradeType;
import tv.game88.core.member.entity.LogMoney;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.platform.api.service.LogMoneyService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会员资金信息Controller
 *
 * @author 77lm
 * @date 2021-10-26
 */
@RestController
@RequestMapping( "/member/logMoney" )
public class LogMoneyController extends BaseController {
    @Resource
    private LogMoneyService logMoneyService;

    /**
     * 查询 会员资金信息列表
     */
    @PreAuthorize( "@ss.hasPermi('member:logMoney:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LogMoney>> list( LogMoney logMoney ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LogMoney> list = logMoneyService.selectLogMoneyList( logMoney );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询资金交易类型列表
     */
    @PreAuthorize( "@ss.hasPermi('member:logMoney:list')" )
    @GetMapping( "/tradeTypeAll" )
    public RspBase<List<RspConfigTradeType>> tradeTypeAll() {
        return RspBase.ok( EnumMoney.getTradeTypes() );
    }

    /**
     * 行为类型统计
     */
    @PreAuthorize( "@ss.hasPermi('member:logMoney:list')" )
    @GetMapping( "/listCount" )
    public RspBase<?> listCount( LogMoney logMoney ) {
        return RspBase.ok( logMoneyService.listCount( logMoney ) );
    }

    /**
     * 查询 会员资金信息统计
     */
    @PreAuthorize( "@ss.hasPermi('member:logMoney:list')" )
    @GetMapping( "/totalCount" )
    public RspBase<?> totalCount( LogMoney logMoney ) {
        return RspBase.ok( logMoneyService.totalCount( logMoney ) );
    }

    /**
     * 导出 会员资金信息列表
     */
    @PreAuthorize( "@ss.hasPermi('member:logMoney:export')" )
    @Log( title = " 会员资金信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( LogMoney logMoney, HttpServletResponse response ) {
        List<LogMoney> list = logMoneyService.selectLogMoneyList( logMoney );
        ExportExcelUtil.exportBigExcel( list, "会员资金信息", "会员资金信息表", LogMoney.class, response );
    }
}
