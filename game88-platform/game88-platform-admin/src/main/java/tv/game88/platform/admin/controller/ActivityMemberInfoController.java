package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.entity.ActivityMemberInfo;
import tv.game88.platform.api.service.ActivityMemberInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 会员推广管理Controller
 *
 * @author 77tv
 * @date 2021-03-19
 */
@RestController
@RequestMapping( "/activity/memberInfo" )
public class ActivityMemberInfoController extends BaseController {
    @Resource
    private ActivityMemberInfoService activityMemberInfoService;

    /**
     * 查询会员推广管理列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:memberInfo:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ActivityMemberInfo>> list( ActivityMemberInfo activityMemberInfo ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ActivityMemberInfo> list = activityMemberInfoService.selectActivityMemberInfoList( activityMemberInfo );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('activity:memberInfo:list')" )
    @GetMapping( "/ipList" )
    public RspBase<List<Map>> ipList( ActivityMemberInfo activityMemberInfo ) {
        return RspBase.ok(activityMemberInfoService.selectIpList( activityMemberInfo ));
    }

    /**
     * 导出会员推广管理列表
     */
    @PreAuthorize( "@ss.hasPermi('activity:memberInfo:export')" )
    @Log( title = "会员推广管理", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ActivityMemberInfo activityMemberInfo, HttpServletResponse response ) {
        List<ActivityMemberInfo> list = activityMemberInfoService.selectActivityMemberInfoList( activityMemberInfo );
        ExportExcelUtil.exportExcel( list, "会员推广管理", "会员推广管理表", ActivityMemberInfo.class, response );
    }

    /**
     * 获取会员推广管理详细信息
     */
    @PreAuthorize( "@ss.hasPermi('activity:memberInfo:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ActivityMemberInfo> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( activityMemberInfoService.selectActivityMemberInfoById( id ) );
    }
}
