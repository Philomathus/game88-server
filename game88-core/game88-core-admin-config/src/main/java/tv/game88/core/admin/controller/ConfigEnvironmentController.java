package tv.game88.core.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.cache.DictUtils;
import tv.game88.core.admin.entity.SysDictData;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.mapper.SysDictDataMapper;
import tv.game88.core.admin.service.ConfigEnvironmentService;
import tv.game88.core.config.entity.ConfigEnvironment;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 环境参数配置Controller
 *
 * @author MengJun
 */
@Log4j2
@RestController
@RequestMapping( "/config/env" )
public class ConfigEnvironmentController extends BaseController {
    @Resource
    private ConfigEnvironmentService configEnvironmentService;
    @Resource
    private DictUtils                dictUtils;
    @Resource
    private SysDictDataMapper        sysDictDataMapper;

    /**
     * 查询环境参数配置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:env:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigEnvironment>> list( ConfigEnvironment configEnvironment ) {
        List<ConfigEnvironment> list = configEnvironmentService.selectConfigEnvironmentList( configEnvironment );
        return RspBase.ok( list );
    }

    /**
     * 导出环境参数配置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:env:export')" )
    @Log( title = "导出环境参数配置列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( ConfigEnvironment configEnvironment ) {
        List<ConfigEnvironment> list = configEnvironmentService.selectConfigEnvironmentList( configEnvironment );
        return RspBase.ok(list);
    }

    /**
     * 获取环境参数配置详细信息
     */
    //@PreAuthorize( "@ss.hasPermi('config:env:query')" )
    @GetMapping( value = "/{envCode}" )
    public RspBase<ConfigEnvironment> getInfo( @PathVariable( "envCode" ) String envCode ) {
        return RspBase.ok( configEnvironmentService.selectConfigEnvironmentById( envCode ) );
    }

    /**
     * 新增环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('config:env:add')" )
    @Log( title = "新增环境参数配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigEnvironment configEnvironment ) {
        try {
            if ( "M".equals( configEnvironment.getMenuType() ) ) {
                //判断名称是否存在
                QueryWrapper<SysDictData> queryLabel = new QueryWrapper<SysDictData>()
                        .eq( "dict_type", "config_environment_group" )
                        .eq( "dict_label", configEnvironment.getEnvTitle() );
                if ( sysDictDataMapper.exists( queryLabel ) ) {
                    throw new BusinessException( "名称已存在" );
                }
                QueryWrapper<SysDictData> queryValue = new QueryWrapper<SysDictData>()
                        .eq( "dict_type", "config_environment_group" )
                        .eq( "dict_value", configEnvironment.getEnvSort() );
                //判断编码是否存在
                if ( sysDictDataMapper.exists( queryValue ) ) {
                    throw new BusinessException( "排序和值已存在" );
                }
                SysDictData dictData = new SysDictData();
                dictData.setDictSort( configEnvironment.getEnvSort() );
                dictData.setDictLabel( configEnvironment.getEnvTitle() );
                dictData.setDictValue( configEnvironment.getEnvSort() + "" );
                dictData.setDictType( "config_environment_group" );
                dictData.setListClass( "default" );
                dictData.setStatus( String.valueOf( 0 ) );
                dictUtils.clearDictCache( "config_environment_group" );
                //加入数据库
                return toResult( sysDictDataMapper.insertDictData( dictData ) );
            }
            return RspBase.ok( configEnvironmentService.insertConfigEnvironment( configEnvironment ) );
        } catch ( Exception e ) {
            return RspBase.businessError( e.getMessage() );
        }
    }

    /**
     * 修改环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('config:env:edit')" )
    @Log( title = "修改环境参数配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigEnvironment configEnvironment ) {
        return toResult( configEnvironmentService.updateConfigEnvironment( configEnvironment ) );
    }

    /**
     * 修改环境参数状态
     */
    @PreAuthorize( "@ss.hasPermi('config:env:edit')" )
    @Log( title = "修改环境参数状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody ConfigEnvironment configEnvironment ) {
        return toResult( configEnvironmentService.changeStatus( configEnvironment ) );
    }


    /**
     * 修改环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('config:env:edit')" )
    @Log( title = "批量修改环境参数配置", businessType = BusinessType.UPDATE )
    @PostMapping( "/editList" )
    public RspBase<?> edit( @RequestBody ArrayList<ConfigEnvironment> configEnvironments ) {
        try {
            for ( ConfigEnvironment configEnvironment : configEnvironments ) {
                configEnvironmentService.updateConfigEnvironment( configEnvironment );
            }
            return RspBase.ok( "操作成功" );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            return RspBase.businessError( "操作失败" );
        }

    }

    /**
     * 获取环境参数头所对应的index
     */
    @PreAuthorize( "@ss.hasPermi('config:env:edit')" )
    @GetMapping( "/getTitleIndex" )
    public RspBase<?> getTitleIndex( String title, String code ) {
        return RspBase.ok( configEnvironmentService.getTitleIndex( title, code ) );
    }

    /**
     * 删除环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('config:env:remove')" )
    @Log( title = "环境参数配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{envCodes}" )
    public RspBase<?> remove( @PathVariable String[] envCodes ) {
        return toResult( configEnvironmentService.deleteConfigEnvironmentByIds( envCodes ) );
    }

    /**
     * 删除环境参数配置
     */
    @PreAuthorize( "@ss.hasPermi('config:env:remove')" )
    @Log( title = "环境参数配置", businessType = BusinessType.CLEAN )
    @DeleteMapping( "/refreshCache" )
    public RspBase<?> refreshCache() {
        configEnvironmentService.refreshCache();
        return RspBase.ok();
    }


    @PreAuthorize( "@ss.hasPermi('config:env:list')" )
    @GetMapping( "/listRecommendPic" )
    public RspBase<List<ConfigEnvironment>> listRecommendPic( ConfigEnvironment configEnvironment ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigEnvironment> configEnvironments = configEnvironmentService.selectConfigRecommendPic( configEnvironment );
        return getRspBasePage( configEnvironments, pageDomain );
    }

    /**
     * 修改推广图
     */
    @PreAuthorize( "@ss.hasPermi('config:recommonPic:edit')" )
    @Log( title = "修改环境参数配置", businessType = BusinessType.UPDATE )
    @PutMapping( value = "/updateRecommendPic" )
    public RspBase<?> updateRecommendPic( @RequestBody ConfigEnvironment configEnvironment ) {
        configEnvironment.setEnvValue( "${domain.oss}" + configEnvironment.getEnvValue() );
        return toResult( configEnvironmentService.updateConfigEnvironment( configEnvironment ) );
    }

}
