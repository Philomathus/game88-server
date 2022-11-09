package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.entity.ConfigOss;
import tv.game88.platform.api.service.ConfigOssService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * oss文件存储服务配置Controller接口
 *
 * @author Rajesh
 */

@RestController
@RequestMapping( "/config/oss" )
public class ConfigOssController extends BaseController {

    @Resource
    private ConfigOssService configOssService;

    /**
     * 查询oss文件存储服务配置列表
     * select all ConfigOss controller
     *
     * @param configOss oss文件存储服务配置
     *
     * @return oss文件存储服务配置集合
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigOss>> list( ConfigOss configOss ) {
        List<ConfigOss> list = configOssService.selectConfigOssList( configOss );
        return RspBase.ok( list );
    }

    /**
     * 按 ID 选择 configOss
     * select configOss By Id controller
     *
     * @param id oss
     *
     * @return 结果
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigOss> getInfo( @PathVariable Long id ) {
        return RspBase.ok( configOssService.selectConfigOssById( id ) );
    }

    /**
     * 修改oss文件存储服务配置控制器层
     * insert Oss config controller
     *
     * @param configOss oss文件存储服务配置
     *
     * @return 结果
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:add')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody ConfigOss configOss ) {
        configOss.setCreateBy( SecurityUtils.getUsername() );
        configOss.setCreateTime( LocalDateTime.now() );
        return toResult( configOssService.insertConfigOss( configOss ) );
    }

    /**
     * 修改oss文件存储服务配置控制器层
     * Modify the service configuration controller
     *
     * @param configOss oss文件存储服务配置
     *
     * @return 结果
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:edit')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigOss configOss ) {
        configOss.setUpdateBy( SecurityUtils.getUsername() );
        configOss.setUpdateTime( LocalDateTime.now() );
        return toResult( configOssService.updateConfigOss( configOss ) );
    }

    /**
     * 批量删除oss文件存储服务配置控制器层
     * delete configOss by Ids service implementation
     *
     * @param ids 需要删除的oss文件存储服务配置ID
     *
     * @return 结果
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:remove')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> deleteByIDs( @PathVariable Long[] ids ) {
        return toResult( configOssService.deleteConfigOssByIds( ids ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:effect')" )
    @PatchMapping( "/effect/{id}" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.EFFECT )
    public RspBase<?> effect( @PathVariable long id ) {
        return toResult( configOssService.effect( id ) );
    }


}
