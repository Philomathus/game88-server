package tv.game88.general.admin.controllor;

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
import tv.game88.general.api.entity.AgentHostClient;
import tv.game88.general.api.service.AgentHostClientService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 主播版本号Controller
 *
 * @author 77tv
 * @date 2021-03-18
 */
@RestController
@RequestMapping( "/admin/agentHostClient" )
public class AgentHostClientController extends BaseController {
    @Resource
    private AgentHostClientService agentHostClientService;

    /**
     * 查询主播版本号列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:list')" )
    @GetMapping( "/list" )
    public RspBase<List<AgentHostClient>> list( AgentHostClient agentHostClient ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<AgentHostClient> list = agentHostClientService.selectAgentHostClientList( agentHostClient );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出主播版本号列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:export')" )
    @Log( title = "主播版本号", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<AgentHostClient>>  export( AgentHostClient agentHostClient, HttpServletResponse response ) {
        return RspBase.ok(  agentHostClientService.selectAgentHostClientList( agentHostClient ) );
//        ExportExcelUtil.exportBigExcel( list, "主播版本号", "主播版本号表", AgentHostClient.class, response );
    }

    /**
     * 获取主播版本号详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<AgentHostClient> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( agentHostClientService.getById( id ) );
    }

    /**
     * 新增主播版本号
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:add')" )
    @Log( title = "主播版本号", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody AgentHostClient agentHostClient ) {
        agentHostClient.setCreateBy( SecurityUtils.getUsername() );
        agentHostClient.setCreateTime( LocalDateTime.now() );
        return toResult( agentHostClientService.save( agentHostClient ) );
    }

    /**
     * 修改主播版本号
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:edit')" )
    @Log( title = "主播版本号", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody AgentHostClient agentHostClient ) {
        agentHostClient.setUpdateBy( SecurityUtils.getUsername() );
        agentHostClient.setUpdateTime( LocalDateTime.now() );
        return toResult( agentHostClientService.updateById( agentHostClient ) );
    }

    /**
     * 删除主播版本号
     */
    @PreAuthorize( "@ss.hasPermi('admin:agentHostClient:remove')" )
    @Log( title = "主播版本号", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        return toResult( agentHostClientService.removeBatchByIds( Arrays.asList( ids ) ) );
    }
}
