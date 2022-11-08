package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.platform.api.service.ConfigSmsService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SMS短信服务配置Controller
 *
 * @author Rajesh
 */

@RestController
@RequestMapping( "/config/sms" )
public class ConfigSmsController extends BaseController {

    @Resource
    private ConfigSmsService configSmsService;

    /**
     * 查询SMS短信服务配置列表
     * Query SMS config list controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigSms>> list( ConfigSms configSms ) {
        List<ConfigSms> list = configSmsService.selectConfigSmsList( configSms );
        return RspBase.ok( list );
    }

    /**
     * 通过 id 获取 SMS config 服务配置详细信息
     * Get SMS config by id service controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:query')" )
    @GetMapping( "/id/{id}" )
    public RspBase<ConfigSms> getById( @PathVariable Long id ) {
        return RspBase.ok( configSmsService.selectConfigSmsById( id ) );
    }

    /**
     * 新增 SMS config 服务配置
     * Added SMS config service controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:add')" )
    @Log( title = "SMS短信服务配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigSms configSms ) {
        configSms.setCreateBy( SecurityUtils.getUsername() );
        configSms.setCreateTime( LocalDateTime.now() );
        return toResult( configSmsService.insertConfigSms( configSms ) );
    }


    /**
     * 修改短信配置配置
     * * Modify SMS config controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:edit')" )
    @Log( title = "SMS短信服务配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> update( @RequestBody ConfigSms configSms ) {
        configSms.setUpdateBy( SecurityUtils.getUsername() );
        configSms.setUpdateTime( LocalDateTime.now() );
        return toResult( configSmsService.updateConfigOSms( configSms ) );
    }


    /**
     * 删除 SMS 配置配置
     * Delete SMS config controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:delete')" )
    @Log( title = "SMS短信服务配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> delete( @PathVariable Long[] ids ) {
        return toResult( configSmsService.deleteServerSmsByIds( ids ) );
    }

    /**
     * 效果 smsConfig 配置
     * effect SmsConfig controller
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:effect')" )
    @Log( title = "SMS短信服务配置-激活", businessType = BusinessType.EFFECT )
    @PutMapping( "/effect/{id}" )
    public RspBase<?> effect( @PathVariable Long id ) {
        return RspBase.ok( configSmsService.effect( id, SecurityUtils.getUsername() ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:sms:effect')" )
    @Log( title = "SMS短信服务配置-取消激活", businessType = BusinessType.EFFECT )
    @PutMapping( "/noEffect/{id}" )
    public RspBase<?> noEffect( @PathVariable Long id ) {
        return RspBase.ok( configSmsService.noEffect( id, SecurityUtils.getUsername() ) );
    }

    /**
     * 测试SMS短信服务配置
     */
    @PreAuthorize( "@ss.hasPermi('config:sms:smsTest')" )
    @Log( title = "SMS短信服务配置", businessType = BusinessType.OTHER )
    @PutMapping( "/smsTest/{id}/{mobile}" )
    public RspBase<?> smsTest( @PathVariable long id, @PathVariable String mobile ) {
        return configSmsService.smsTest( id, mobile );
    }
}
