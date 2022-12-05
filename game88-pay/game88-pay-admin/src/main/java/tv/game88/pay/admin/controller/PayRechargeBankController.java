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
import tv.game88.pay.api.entity.PayRechargeBank;
import tv.game88.pay.api.service.PayRechargeBankService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payRechargeBank" )
public class PayRechargeBankController extends BaseController {
    @Resource
    private PayRechargeBankService payRechargeBankService;

    /**
     * 查询列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayRechargeBank>> list( PayRechargeBank payRechargeBank ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayRechargeBank> list = payRechargeBankService.selectPayRechargeBankList( payRechargeBank );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayRechargeBank>> listAll() {
        return RspBase.ok( payRechargeBankService.list() );
    }

    /**
     * 导出列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:export')" )
    @Log( title = "公司入款银行导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayRechargeBank payRechargeBank, HttpServletResponse response ) {
        List<PayRechargeBank> list = payRechargeBankService.selectPayRechargeBankList( payRechargeBank );
        ExportExcelUtil.exportExcel( list, "公司入款银行", "公司入款银行表", PayRechargeBank.class, response );
    }

    /**
     * 获取详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayRechargeBank> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( payRechargeBankService.getById( id ) );
    }

    /**
     * 新增
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:add')" )
    @Log( title = "公司入款银行新增", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayRechargeBank payRechargeBank ) throws Exception {
        SecurityUtils.verifyMFACode( payRechargeBank.getGoogleAuthCode() );
        payRechargeBank.setCreateTime( LocalDateTime.now() );
        payRechargeBank.setCreateBy( SecurityUtils.getUsername() );
        return toResult( payRechargeBankService.save( payRechargeBank ) );
    }

    /**
     * 修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:edit')" )
    @Log( title = "公司入款银行修改", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayRechargeBank payRechargeBank ) throws Exception {
        SecurityUtils.verifyMFACode( payRechargeBank.getGoogleAuthCode() );
        payRechargeBank.setUpdateTime( LocalDateTime.now() );
        payRechargeBank.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( payRechargeBankService.updateById( payRechargeBank ) );
    }

    /**
     * 删除
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:remove')" )
    @Log( title = "公司入款银行删除", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( payRechargeBankService.removeByIds( Arrays.asList( ids ) ) );
    }

    /**
     * 公司入款银行状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:payRechargeBank:effect')" )
    @Log( title = "公司入款银行状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        PayRechargeBank update = new PayRechargeBank();
        update.setId( id );
        update.setEffect( effect );
        return toResult( payRechargeBankService.updateById( update ) );
    }

}
