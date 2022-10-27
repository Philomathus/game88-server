package tv.game88.admin.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.admin.system.service.ConfigOssService;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.config.entity.ConfigOss;

import java.util.List;

/**
 * oss文件存储服务配置Controller接口
 *
 * @author Rajesh
 * @date 2022-10-26
 */

@RestController
@RequestMapping( "/admin/configOss" )
public class ConfigOssController extends BaseController {

    @Autowired
    private ConfigOssService configOssService;

    @PreAuthorize( "@ss.hasPermi('config:oss:list')" )
    @GetMapping("/list")
    public RspBase<List<ConfigOss>> list(ConfigOss configOss){
       List<ConfigOss> list =  configOssService.list(configOss);
       return RspBase.ok(list);
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigOss> getInfo(@PathVariable Long id ) {
        return RspBase.ok( configOssService.selectConfigOssById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:add')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add(@Validated @RequestBody ConfigOss configOss){
       return toResult(configOssService.insertConfigOss(configOss));
    }

    /**
     * update configOss
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:edit')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigOss configOss ) {
         return toResult(configOssService.updateConfigOss(configOss));
    }

    /**
     * Delete the oss configuration
     * 删除oss配置
     */
    @PreAuthorize( "@ss.hasPermi('config:oss:remove')" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> deleteByIDs( @PathVariable Long[] ids ) {
        return toResult( configOssService.deleteConfigOssByIds( ids ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:deleteOne')" )
    @Log( title = "oss配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{id}" )
    public RspBase<?> delete(@PathVariable Long id){
        return toResult(configOssService.deleteConfigOssById(id));
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:effect')" )
    @PatchMapping( "/effect/{id}" )
    @Log( title = "oss文件存储服务配置", businessType = BusinessType.EFFECT )
    public RspBase<?> effect( @PathVariable long id ) {
        return toResult( configOssService.effect( id ) );
    }

}
