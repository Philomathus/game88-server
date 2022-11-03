package tv.game88.pay.admin.controller;

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
import tv.game88.pay.api.entity.ConfigBank;
import tv.game88.pay.api.service.ConfigBankService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controller
 *
 * @author 77lm
 * @date 2021-10-14
 */
@RestController
@RequestMapping( "/pay/configBank" )
public class ConfigBankController extends BaseController {
    @Resource
    private ConfigBankService configBankService;

    /**
     * 查询列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigBank>> list( ConfigBank configBank ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigBank> list = configBankService.selectConfigBankList( configBank );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:export')" )
    @Log( title = "公司入款银行导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ConfigBank configBank, HttpServletResponse response ) {
        List<ConfigBank> list = configBankService.selectConfigBankList( configBank );
        ExportExcelUtil.exportExcel( list, "公司入款银行", "公司入款银行表", ConfigBank.class, response );
    }

    /**
     * 获取详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigBank> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( configBankService.getById( id ) );
    }

    /**
     * 新增
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:add')" )
    @Log( title = "公司入款银行新增", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigBank configBank ) throws Exception {
        SecurityUtils.verifyMFACode( configBank.getGoogleAuthCode() );

        configBank.setAccountName( configBank.getAccountName().trim() );
        configBank.setCreateTime( LocalDateTime.now() );
        configBank.setCreateBy( SecurityUtils.getUsername() );
        return toResult( configBankService.save( configBank ) );
    }

    /**
     * 修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:edit')" )
    @Log( title = "公司入款银行修改", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigBank configBank ) throws Exception {
        SecurityUtils.verifyMFACode( configBank.getGoogleAuthCode() );

        configBank.setAccountName( configBank.getAccountName().trim() );
        configBank.setUpdateTime( LocalDateTime.now() );
        configBank.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( configBankService.updateById( configBank ) );
    }

    /**
     * 删除
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:remove')" )
    @Log( title = "公司入款银行删除", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( configBankService.removeByIds( Arrays.asList( ids ) ) );
    }

    /**
     * 公司入款银行状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBank:effect')" )
    @Log( title = "公司入款银行状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        ConfigBank update = new ConfigBank();
        update.setId( id );
        update.setEffect( effect );
        return toResult( configBankService.updateById( update ) );
    }

}
