package tv.game88.platform.admin.controller;

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
import tv.game88.core.quest.cache.ActivityCacheUtil;
import tv.game88.core.quest.entity.ActivityInfo;
import tv.game88.platform.api.service.ActivityInfoService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 活动信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/activity/activityInfo" )
public class ActivityInfoController extends BaseController {
    @Resource
    private ActivityInfoService activityInfoService;
    @Resource
    private ActivityCacheUtil   activityCacheUtil;

    /**
     * 查询活动信息列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ActivityInfo>> list( ActivityInfo activityInfo ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ActivityInfo> list = activityInfoService.selectActivityInfoList( activityInfo );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出活动信息列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:export')" )
    @Log( title = "活动信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( ActivityInfo activityInfo, HttpServletResponse response ) {
        List<ActivityInfo> list = activityInfoService.selectActivityInfoList( activityInfo );
//        ExportExcelUtil.exportBigExcel( list, "活动信息", "活动信息表", ActivityInfo.class, response );
        return RspBase.ok( list );
    }

    /**
     * 获取活动信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ActivityInfo> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( activityInfoService.getById( id ) );
    }

    /**
     * 新增活动信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:add')" )
    @Log( title = "活动信息", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ActivityInfo activityInfo ) {
        activityInfo.setCreateBy( SecurityUtils.getUsername() );
        activityInfo.setCreateTime( LocalDateTime.now() );
        activityInfo.setEffect( false );
        return toResult( activityInfoService.save( activityInfo ) );
    }

    /**
     * 修改活动信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:edit')" )
    @Log( title = "活动信息", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ActivityInfo activityInfo ) {
        activityInfo.setUpdateBy( SecurityUtils.getUsername() );
        activityInfo.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = activityInfoService.updateById( activityInfo );
        if ( isUpdate ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_INFO_KEY );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除活动信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:remove')" )
    @Log( title = "活动信息", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = activityInfoService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_INFO_KEY );
        }
        return toResult( isRemove );
    }

    /**
     * 修改活动信息激活状态
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityInfo:effect')" )
    @Log( title = "激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        ActivityInfo update = new ActivityInfo();
        update.setId( id );
        update.setEffect( effect );
        boolean isUpdate = activityInfoService.updateById( update );
        if ( isUpdate ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_INFO_KEY );
        }
        return toResult( isUpdate );
    }
}