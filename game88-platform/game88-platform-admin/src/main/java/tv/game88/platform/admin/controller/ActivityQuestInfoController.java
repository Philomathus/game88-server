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
import tv.game88.platform.api.entity.ActivityQuestInfo;
import tv.game88.platform.api.service.ActivityQuestInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 任务信息Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/activity/activityQuestInfo" )
public class ActivityQuestInfoController extends BaseController {
	@Resource
	private ActivityQuestInfoService activityQuestInfoService;

	/**
	 * 查询任务信息列表
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:list')" )
	@GetMapping( "/list" )
	public RspBase<List<ActivityQuestInfo>> list( ActivityQuestInfo activityQuestInfo) {
		PageDomain pageDomain = TableSupport.buildPageRequest();
		startPage( pageDomain );
		List<ActivityQuestInfo> list = activityQuestInfoService.selectActivityQuestInfoList(activityQuestInfo);
		return getRspBasePage( list, pageDomain );
	}

	/**
	 * 导出任务信息列表
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:export')" )
	@Log( title = "任务信息", businessType = BusinessType.EXPORT )
	@GetMapping( "/export" )
	public void export(ActivityQuestInfo activityQuestInfo, HttpServletResponse response) {
		List<ActivityQuestInfo>      list = activityQuestInfoService.selectActivityQuestInfoList(activityQuestInfo);
		ExportExcelUtil.exportExcel( list, "任务信息", "任务信息表", ActivityQuestInfo.class, response );
	}

	/**
	 * 获取任务信息详细信息
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:query')" )
	@GetMapping( value = "/{id}" )
	public RspBase<ActivityQuestInfo> getInfo( @PathVariable( "id" ) Long id) {
		return RspBase.ok( activityQuestInfoService.getById(id) );
	}

	/**
	 * 新增任务信息
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:add')" )
	@Log( title = "任务信息", businessType = BusinessType.INSERT )
	@PostMapping
	public RspBase<?> add( @RequestBody ActivityQuestInfo activityQuestInfo) {
		return toResult( activityQuestInfoService.save(activityQuestInfo) );
	}

	/**
	 * 修改任务信息
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:edit')" )
	@Log( title = "任务信息", businessType = BusinessType.UPDATE )
	@PutMapping
	public RspBase<?> edit( @RequestBody ActivityQuestInfo activityQuestInfo) {
		return toResult( activityQuestInfoService.updateById(activityQuestInfo) );
	}

	/**
	 * 删除任务信息
	 */
	@PreAuthorize( "@ss.hasPermi('activity:activityQuestInfo:remove')" )
	@Log( title = "任务信息", businessType = BusinessType.DELETE )
	@DeleteMapping( "/{ids}" )
	public RspBase<?> remove( @PathVariable Long[] ids ) {
		return toResult( activityQuestInfoService.removeByIds( Arrays.asList( ids ) ) );
	}
}