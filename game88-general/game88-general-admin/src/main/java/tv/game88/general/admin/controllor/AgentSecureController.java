package tv.game88.general.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.general.api.entity.AgentSecure;
import tv.game88.general.api.service.AgentSecureService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 域名加密管理Controller
 *
 * @author 77tv
 * @date 2021-04-01
 */
@RestController
@RequestMapping( "/agent/agentSecure" )
public class AgentSecureController extends BaseController {
    @Resource
    private AgentSecureService agentSecureService;

    /**
     * 查询域名加密管理列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:list')" )
    @GetMapping( "/list" )
    public RspBase<List<AgentSecure>> list( AgentSecure agentSecure ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<AgentSecure> list = agentSecureService.selectAgentSecureList( agentSecure );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出域名加密管理列表
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:export')" )
    @Log( title = "域名加密管理", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( AgentSecure agentSecure, HttpServletResponse response ) {
        List<AgentSecure> list = agentSecureService.selectAgentSecureList( agentSecure );
        ExportExcelUtil.exportExcel( list, "域名加密管理", "域名加密管理表", AgentSecure.class, response );
    }

    /**
     * 获取域名加密管理详细信息
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<AgentSecure> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( agentSecureService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:edit')" )
    @PutMapping( value = "/{agent}" )
    public RspBase<?> uploadAgent( @PathVariable( "agent" ) String agent ) {
        return toResult( agentSecureService.uploadAgent( agent ) );
    }

    /**
     * 新增域名加密管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:add')" )
    @Log( title = "域名加密管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody AgentSecure agentSecure ) throws Exception {
        agentSecure.setCreateBy( SecurityUtils.getUsername() );
        agentSecure.setCreateTime( LocalDateTime.now() );
        agentSecure.setSecureUrls( RSACoder.encryptByPublicKey( agentSecure.getUrls(), agentSecure.getPublickey() ) );
        return toResult( agentSecureService.save( agentSecure ) );
    }


    /**
     * 新增域名加密管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:add')" )
    @Log( title = "测试域名加密", businessType = BusinessType.INSERT )
    @PostMapping( "testUrl" )
    public RspBase<?> testUrl( @RequestBody AgentSecure agentSecure ) throws Exception {
        return RspBase.ok( RSACoder.decryptByPrivateKey( agentSecure.getUrls(), agentSecure.getPrivatekey() ) );
    }

    /**
     * 修改域名加密管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:edit')" )
    @Log( title = "域名加密管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody AgentSecure agentSecure ) throws Exception {
        agentSecure.setUpdateBy( SecurityUtils.getUsername() );
        agentSecure.setUpdateTime( LocalDateTime.now() );
        agentSecure.setSecureUrls( RSACoder.encryptByPublicKey( agentSecure.getUrls(), agentSecure.getPublickey() ) );
        return toResult( agentSecureService.updateById( agentSecure ) );
    }

    /**
     * 删除域名加密管理
     */
    @PreAuthorize( "@ss.hasPermi('agent:agentSecure:remove')" )
    @Log( title = "域名加密管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( agentSecureService.removeBatchByIds( Arrays.asList( ids ) ) );
    }
}
