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
import tv.game88.general.api.entity.Agent;
import tv.game88.general.api.entity.AgentHost;
import tv.game88.general.api.service.AgentHostService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 主播域名管理Controller
 *
 * @author 77tv
 * @date 2021-03-30
 */
@RestController
@RequestMapping( "/agent/agentHost" )
public class AgentHostController extends BaseController {
    @Resource
    private AgentHostService agentHostService;

    /**
     * 查询主播域名管理列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:list')" )
    @GetMapping( "/list" )
    public RspBase<List<AgentHost>> list( AgentHost agentHost ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<AgentHost> list = agentHostService.selectAgentHostList( agentHost );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出主播域名管理列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:export')" )
    @Log( title = "主播域名管理", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( AgentHost agentHost, HttpServletResponse response ) {
        List<AgentHost> list = agentHostService.selectAgentHostList( agentHost );
        ExportExcelUtil.exportBigExcel( list, "主播域名管理", "主播域名管理表", AgentHost.class, response );
    }

    /**
     * 获取主播域名管理详细信息
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<AgentHost> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( agentHostService.getById( id ) );
    }

    /**
     * 新增主播域名管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:add')" )
    @Log( title = "主播域名管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody AgentHost agentHost ) {
        agentHost.setCreateBy( SecurityUtils.getUsername() );
        agentHost.setCreateTime( LocalDateTime.now() );
        return toResult( agentHostService.save( agentHost ) );
    }

    /**
     * 修改主播域名管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:edit')" )
    @Log( title = "主播域名管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody AgentHost agentHost ) {
        agentHost.setUpdateBy( SecurityUtils.getUsername() );
        agentHost.setUpdateTime( LocalDateTime.now() );
        return toResult( agentHostService.updateById( agentHost ) );
    }

    /**
     * 删除主播域名管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentHost:remove')" )
    @Log( title = "主播域名管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( agentHostService.removeBatchByIds( Arrays.asList( ids ) ) );
    }

    /**
     * 代理管理状态修改
     */
    @Log( title = "代理管理状态修改", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody AgentHost agentHost ) {
        AgentHost newAgentHost = new AgentHost();
        newAgentHost.setId( agentHost.getId() );
        newAgentHost.setStatus( agentHost.getStatus());
        newAgentHost.setUpdateTime( LocalDateTime.now() );
        newAgentHost.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( agentHostService.updateById( newAgentHost ) );
    }
}
