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
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.entity.LotteryInfo;
import tv.game88.lottery.api.service.LotteryInfoService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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
    @Resource
    private LotteryCacheUtils  lotteryCacheUtils;

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

    @PreAuthorize( "@ss.hasPermi('lottery:info:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<LotteryInfo>> listAll(){
        return RspBase.ok(lotteryInfoService.list());
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
        LotteryInfo update = new LotteryInfo();
        update.setId( lotteryInfo.getId() );
        update.setIcon( lotteryInfo.getIcon() );
        return toResult( lotteryInfoService.updateById( update ) );
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

    /**
     * 修改活动信息激活状态 change status
     */
    @PreAuthorize( "@ss.hasPermi('lottery:info:effect')" )
    @Log( title = "激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Integer id, @PathVariable Boolean effect ) {
        if ( !SecurityUtils.getUsername().equals( "mengjun" ) ) {
            return RspBase.businessError( "您无权激活或关闭彩票" );
        }
        LotteryInfo lotteryInfo = new LotteryInfo();
        lotteryInfo.setId( id );
        lotteryInfo.setEffect( effect );
        boolean isUpdate = lotteryInfoService.updateById( lotteryInfo );
        if ( isUpdate ) {
            lotteryCacheUtils.clear();
        }
        return toResult( isUpdate );
    }

}