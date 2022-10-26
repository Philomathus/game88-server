package tv.game88.admin.system.controller;

import tv.game88.admin.system.service.IConfigEnvironmentService;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.common.base.BaseController;
import tv.game88.core.config.entity.ConfigEnvironment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 环境参数配置Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/admin/configEnvironment" )
public class ConfigEnvironmentController extends BaseController {
    @Resource
    private IConfigEnvironmentService configEnvironmentService;

    /**
     * 查询环境参数配置列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigEnvironment>> list( ConfigEnvironment configEnvironment ) {
        List<ConfigEnvironment> list = configEnvironmentService.selectConfigEnvironmentList( configEnvironment );
        return RspBase.ok( list );
    }

    /**
     * 查询环境参数配置列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:recommonPic:list')" )
    @GetMapping( "/listRecommonPic" )
    public RspBase<List<ConfigEnvironment>> listRecommonPic( ConfigEnvironment configEnvironment ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigEnvironment> list = configEnvironmentService.selectConfigEnvironmentTwo( configEnvironment );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取推广图详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:recommonPic:edit')" )
    @GetMapping( value = "/getRecommonPic/{envCode}" )
    public RspBase<?> getInfoRecommonPic( @PathVariable( "envCode" ) String envCode ) {
        return RspBase.ok( configEnvironmentService.selectConfigEnvironmentByIdTwo( envCode ) );
    }

    /**
     * 修改推广图
     */
    @PreAuthorize( "@ss.hasPermi('admin:recommonPic:edit')" )
    @PutMapping( value = "/updateRecommonPic" )
    public RspBase<?> updateRecommonPic( @RequestBody ConfigEnvironment configEnvironment ) {
        if ( StringUtils.isNotBlank( configEnvironment.getEnvValue() ) ) {
            configEnvironment.setEnvValue( "${domain.oss}" + configEnvironment.getEnvValue() );
        }
        return toResult( configEnvironmentService.updateConfigEnvironment( configEnvironment ) );
    }

    /**
     * 导出环境参数配置列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:export')" )
    @Log( title = "导出环境参数配置列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ConfigEnvironment configEnvironment, HttpServletResponse response ) {
        List<ConfigEnvironment> list = configEnvironmentService.selectConfigEnvironmentList( configEnvironment );
        ExportExcelUtil.exportExcel( list, "环境参数配置", "环境参数配置表", ConfigEnvironment.class, response );
    }

    /**
     * 获取环境参数配置详细信息
     */
    //@PreAuthorize( "@ss.hasPermi('admin:configEnvironment:query')" )
    @GetMapping( value = "/{envCode}" )
    public RspBase<ConfigEnvironment> getInfo( @PathVariable( "envCode" ) String envCode ) {
        return RspBase.ok( configEnvironmentService.selectConfigEnvironmentById( envCode ) );
    }

    /**
     * 新增环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:add')" )
    @Log( title = "新增环境参数配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigEnvironment configEnvironment ) {
        try {
            return RspBase.ok( configEnvironmentService.insertConfigEnvironment( configEnvironment ) );
        } catch ( Exception e ) {
            return RspBase.businessError( e.getMessage() );
        }
    }

    /**
     * 修改环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:edit')" )
    @Log( title = "修改环境参数配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigEnvironment configEnvironment ) {
        return toResult( configEnvironmentService.updateConfigEnvironment( configEnvironment ) );
    }

    /**
     * 修改环境参数状态
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:edit')" )
    @Log( title = "修改环境参数状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody ConfigEnvironment configEnvironment ) {
        return toResult( configEnvironmentService.changeStatus( configEnvironment ) );
    }


    /**
     * 修改环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:edit')" )
    @Log( title = "批量修改环境参数配置", businessType = BusinessType.UPDATE )
    @PostMapping( "/editList" )
    public RspBase<?> edit( @RequestBody ArrayList<ConfigEnvironment> configEnvironments ) {
        try {
            for ( ConfigEnvironment configEnvironment : configEnvironments ) {
                configEnvironmentService.updateConfigEnvironment( configEnvironment );
            }
            return RspBase.ok( "操作成功" );
        } catch ( Exception e ) {
            e.printStackTrace();
            return RspBase.businessError( "操作失败" );
        }

    }

    /**
     * 获取环境参数头所对应的index
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:edit')" )
    @GetMapping( "/getTitleIndex" )
    public RspBase<?> getTitleIndex( String title, String code ) {
        return RspBase.ok( configEnvironmentService.getTitleIndex( title, code ) );
    }

    /**
     * 删除环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:remove')" )
    @Log( title = "环境参数配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{envCodes}" )
    public RspBase<?> remove( @PathVariable String[] envCodes ) {
        return toResult( configEnvironmentService.deleteConfigEnvironmentByIds( envCodes ) );
    }

    /**
     * 删除环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('admin:configEnvironment:remove')" )
    @Log( title = "环境参数配置", businessType = BusinessType.CLEAN )
    @DeleteMapping( "/refreshCache" )
    public RspBase<?> refreshCache() {
        configEnvironmentService.refreshCache();
        return RspBase.ok();
    }
}
