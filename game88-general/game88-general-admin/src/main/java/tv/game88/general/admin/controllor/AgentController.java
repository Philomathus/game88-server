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
import tv.game88.general.api.service.AgentService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 代理管理Controller
 *
 * @author 77tv
 * @date 2021-04-16
 */
@RestController
@RequestMapping( "/admin/agent" )
public class AgentController extends BaseController {
    @Resource
    private AgentService agentService;

    /**
     * 查询代理管理列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:list')" )
    @GetMapping( "/list" )
    public RspBase<List<Agent>> list( Agent agent ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<Agent> list = agentService.selectAgentList( agent );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出代理管理列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:export')" )
    @Log( title = "代理管理", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<Agent>> export( Agent Agent, HttpServletResponse response ) {
        return RspBase.ok(  agentService.selectAgentList( Agent ) );
//        ExportExcelUtil.exportBigExcel( list, "代理管理", "代理管理表", Agent.class, response );
    }

    /**
     * 获取代理管理详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:query')" )
    @GetMapping( value = "/{key}" )
    public RspBase<Agent> getInfo( @PathVariable( "key" ) String key ) {
        return RspBase.ok( agentService.getById( key ) );
    }

    /**
     * 新增代理管理
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:add')" )
    @Log( title = "代理管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody Agent agent ) {
        agent.setCreateTime( LocalDateTime.now() );
        agent.setCreateBy( SecurityUtils.getUsername() );
        agent.setStatus(agent.getStatus().equals("true") ? "1" : "0");
        return toResult( agentService.save( agent ) );
    }

    /**
     * 修改代理管理
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:edit')" )
    @Log( title = "代理管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody Agent agent ) {
        agent.setUpdateTime( LocalDateTime.now() );
        agent.setUpdateBy( SecurityUtils.getUsername() );
        agent.setStatus(agent.getStatus().equals("true") ? "1" : "0");
        return toResult( agentService.updateById( agent ) );
    }

    /**
     * 删除代理管理
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:remove')" )
    @Log( title = "代理管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{key}" )
    public RspBase<?> remove( @PathVariable String key ) {
        return toResult( agentService.removeById(  key )  );
    }

    /**
     * 代理管理状态修改
     */
    @PreAuthorize( "@ss.hasPermi('admin:agent:edit')" )
    @Log( title = "代理管理状态修改", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody Agent agent ) {
        Agent newAgent = new Agent();
        newAgent.setKey( agent.getKey() );
        newAgent.setUpdateTime( LocalDateTime.now() );
        newAgent.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( agentService.updateById( newAgent ) );
    }
}
