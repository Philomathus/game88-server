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
import tv.game88.core.quest.entity.ActivityQuestType;
import tv.game88.platform.api.service.ActivityQuestTypeService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 任务类型Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/activity/activityQuestType" )
public class ActivityQuestTypeController extends BaseController {
    @Resource
    private ActivityQuestTypeService activityQuestTypeService;
    @Resource
    private ActivityCacheUtil        activityCacheUtil;

    /**
     * 查询任务类型列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ActivityQuestType>> list( ActivityQuestType activityQuestType ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ActivityQuestType> list = activityQuestTypeService.selectActivityQuestTypeList( activityQuestType );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询任务类型列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:list')" )
    @GetMapping("/listAll")
    public RspBase<List<ActivityQuestType>> listAll(){
        return RspBase.ok(activityQuestTypeService.list());
    }

    /**
     * 导出任务类型列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:export')" )
    @Log( title = "任务类型", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( ActivityQuestType activityQuestType, HttpServletResponse response ) {
        List<ActivityQuestType> list = activityQuestTypeService.selectActivityQuestTypeList( activityQuestType );
//        ExportExcelUtil.exportBigExcel( list, "任务类型", "任务类型表", ActivityQuestType.class, response );
        return RspBase.ok(list);
    }

    /**
     * 获取任务类型详细信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ActivityQuestType> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( activityQuestTypeService.getById( id ) );
    }

    /**
     * 新增任务类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:add')" )
    @Log( title = "任务类型", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ActivityQuestType activityQuestType ) {
        activityQuestType.setCreateBy( SecurityUtils.getUsername() );
        activityQuestType.setCreateTime( LocalDateTime.now() );
        boolean isSave = activityQuestTypeService.save( activityQuestType );
        if ( isSave ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_QUEST_TYPE_KEY );
        }
        return toResult( isSave );
    }

    /**
     * 修改任务类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:edit')" )
    @Log( title = "任务类型", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ActivityQuestType activityQuestType ) {
        boolean isUpdate = activityQuestTypeService.updateById( activityQuestType );
        if ( isUpdate ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_QUEST_TYPE_KEY );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除任务类型
     */
    @PreAuthorize( "@ss.hasPermi('activity:activityQuestType:remove')" )
    @Log( title = "任务类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = activityQuestTypeService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            activityCacheUtil.delActiveCache( ActivityCacheUtil.ACTIVITY_QUEST_TYPE_KEY );
        }
        return toResult( isRemove );
    }
}