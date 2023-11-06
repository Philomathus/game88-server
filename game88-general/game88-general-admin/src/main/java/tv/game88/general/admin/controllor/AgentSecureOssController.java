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
import tv.game88.general.api.entity.AgentSecureOss;
import tv.game88.general.api.service.AgentSecureOssService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 代理域名ossController
 *
 * @author 77tv
 * @date 2021-04-05
 */
@RestController
@RequestMapping( "/agent/secureOss" )
public class AgentSecureOssController extends BaseController {
    @Resource
    private AgentSecureOssService agentSecureOssService;

    /**
     * 查询代理域名oss列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:list')" )
    @GetMapping( "/list" )
    public RspBase<List<AgentSecureOss>> list( AgentSecureOss agentSecureOss ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<AgentSecureOss> list = agentSecureOssService.selectAgentSecureOssList( agentSecureOss );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询代理域名agent列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:list')" )
    @GetMapping( "/getAgentList" )
    public Object getAgentList() {
        return agentSecureOssService.getAgentList();
    }

    /**
     * 导出代理域名oss列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:export')" )
    @Log( title = "代理域名oss", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( AgentSecureOss agentSecureOss, HttpServletResponse response ) {
        List<AgentSecureOss> list = agentSecureOssService.selectAgentSecureOssList( agentSecureOss );
        ExportExcelUtil.exportExcel( list, "代理域名oss", "代理域名oss表", AgentSecureOss.class, response );
    }

    /**
     * 获取代理域名oss详细信息
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<AgentSecureOss> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( agentSecureOssService.getById( id ) );
    }

    /**
     * 新增代理域名oss
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:add')" )
    @Log( title = "代理域名oss", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody AgentSecureOss agentSecureOss ) {
        agentSecureOss.setCreateBy( SecurityUtils.getUsername() );
        agentSecureOss.setCreateTime( LocalDateTime.now() );
        return toResult( agentSecureOssService.save( agentSecureOss ) );
    }

    /**
     * 修改代理域名oss
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:edit')" )
    @Log( title = "代理域名oss", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody AgentSecureOss agentSecureOss ) {
        agentSecureOss.setUpdateBy( SecurityUtils.getUsername() );
        agentSecureOss.setUpdateTime( LocalDateTime.now() );
        return toResult( agentSecureOssService.updateById( agentSecureOss ) );
    }

    /**
     * 删除代理域名oss
     */
    @PreAuthorize( "@ss.hasPermi('agent:secureOss:remove')" )
    @Log( title = "代理域名oss", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        return toResult( agentSecureOssService.removeBatchByIds( Arrays.asList( ids ) ) );
    }
}
