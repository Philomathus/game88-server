package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.lottery.api.entity.LotteryInfo;
import tv.game88.lottery.api.service.LotteryInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 彩票信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/lottery/lotteryInfo" )
public class LotteryInfoController extends BaseController {
    @Resource
    private LotteryInfoService lotteryInfoService;

    /**
     * 查询彩票信息列表
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LotteryInfo>> list( LotteryInfo lotteryInfo ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LotteryInfo> list = lotteryInfoService.selectLotteryInfoList( lotteryInfo );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出彩票信息列表
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:export')" )
    @Log( title = "彩票信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( LotteryInfo lotteryInfo, HttpServletResponse response ) {
        List<LotteryInfo> list = lotteryInfoService.selectLotteryInfoList( lotteryInfo );
        ExportExcelUtil.exportExcel( list, "彩票信息", "彩票信息表", LotteryInfo.class, response );
    }

    /**
     * 获取彩票信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<LotteryInfo> getInfo( @PathVariable( "id" ) Integer id ) {
        return RspBase.ok( lotteryInfoService.getById( id ) );
    }

    /**
     * 新增彩票信息
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:add')" )
    @Log( title = "彩票信息", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody LotteryInfo lotteryInfo ) {
        return toResult( lotteryInfoService.save( lotteryInfo ) );
    }

    /**
     * 修改彩票信息
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:edit')" )
    @Log( title = "彩票信息", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody LotteryInfo lotteryInfo ) {
        return toResult( lotteryInfoService.updateById( lotteryInfo ) );
    }

    /**
     * 删除彩票信息
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:remove')" )
    @Log( title = "彩票信息", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Integer[] ids ) {
        return toResult( lotteryInfoService.removeByIds( Arrays.asList( ids ) ) );
    }
}