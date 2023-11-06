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
import tv.game88.pay.api.entity.PayRechargeUsdt;
import tv.game88.pay.api.service.PayRechargeUsdtService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * USDT渠道 Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payRechargeUsdt" )
public class PayRechargeUsdtController extends BaseController {
    @Resource
    private PayRechargeUsdtService payRechargeUsdtService;

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayRechargeUsdt>> list( PayRechargeUsdt payRechargeUsdt ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayRechargeUsdt> list = payRechargeUsdtService.selectPayRechargeUsdtList( payRechargeUsdt );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayRechargeUsdt>> listAll() {
        return RspBase.ok(payRechargeUsdtService.list());
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:export')" )
    @Log( title = "导出USDT渠道列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayRechargeUsdt payRechargeUsdt, HttpServletResponse response ) {
        List<PayRechargeUsdt> list = payRechargeUsdtService.selectPayRechargeUsdtList( payRechargeUsdt );
        ExportExcelUtil.exportExcel( list, "USDT渠道", "USDT渠道表", PayRechargeUsdt.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayRechargeUsdt> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( payRechargeUsdtService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:add')" )
    @Log( title = "新增USDT渠道", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayRechargeUsdt payRechargeUsdt ) {
        payRechargeUsdt.setCreateTime( LocalDateTime.now() );
        payRechargeUsdt.setCreateBy( SecurityUtils.getUsername() );
        return toResult( payRechargeUsdtService.save( payRechargeUsdt ) );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:edit')" )
    @Log( title = "修改USDT渠道", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayRechargeUsdt payRechargeUsdt ) {
        payRechargeUsdt.setUpdateTime( LocalDateTime.now() );
        payRechargeUsdt.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( payRechargeUsdtService.updateById( payRechargeUsdt ) );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:remove')" )
    @Log( title = "删除USDT渠道", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( payRechargeUsdtService.removeBatchByIds( Arrays.asList( ids ) ) );
    }

    @PreAuthorize( "@ss.hasPermi('pay:rechargeUsdt:edit')" )
    @Log( title = "USDT渠道激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        PayRechargeUsdt update = new PayRechargeUsdt();
        update.setId( id );
        update.setEffect( effect );
        return toResult( payRechargeUsdtService.updateById( update ) );
    }

}
