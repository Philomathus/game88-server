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
import tv.game88.core.quest.entity.ActivityType;
import tv.game88.platform.api.service.ActivityTypeService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 活动类型Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/activity/activityType" )
public class ActivityTypeController extends BaseController {
    @Resource
    private ActivityTypeService activityTypeService;
    @Resource
    private ActivityCacheUtil   activityCacheUtil;

    /**
     * 查询活动类型列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ActivityType>> list( ActivityType activityType ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ActivityType> list = activityTypeService.selectActivityTypeList( activityType );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * Activity Type List All
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:list')" )
    @GetMapping( "/listAll" )
    public List<ActivityType> listAll() {
       return activityTypeService.list();
    }

    /**
     * 导出活动类型列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:export')" )
    @Log( title = "活动类型", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( ActivityType activityType, HttpServletResponse response ) {
        List<ActivityType> list = activityTypeService.selectActivityTypeList( activityType );
//        ExportExcelUtil.exportBigExcel( list, "活动类型", "活动类型表", ActivityType.class, response );
        return RspBase.ok( list );
    }

    /**
     * 获取活动类型详细信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ActivityType> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( activityTypeService.getById( id ) );
    }

    /**
     * 新增活动类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:add')" )
    @Log( title = "活动类型", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ActivityType activityType ) {
        activityType.setCreateBy( SecurityUtils.getUsername() );
        activityType.setCreateTime( LocalDateTime.now() );
        boolean isSave = activityTypeService.save( activityType );
        if ( isSave ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_TYPE_KEY );
        }
        return toResult( isSave );
    }

    /**
     * 修改活动类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:edit')" )
    @Log( title = "活动类型", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ActivityType activityType ) {
        boolean isUpdate = activityTypeService.updateById( activityType );
        if ( isUpdate ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_TYPE_KEY );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除活动类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityType:remove')" )
    @Log( title = "活动类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = activityTypeService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_TYPE_KEY );
        }
        return toResult( isRemove );
    }
}