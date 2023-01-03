package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.pay.api.entity.BankCardAddress;
import tv.game88.pay.api.service.BankCardAddressService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 【请填写功能名称】Controller
 *
 * @author Rajesh
 * @date 2022-12-28
 */
@RestController
@RequestMapping("/pay/bankCardAddress")
public class BankCardAddressController extends BaseController {

    @Resource
    private BankCardAddressService bankCardAddressService;

    /**
     * 查询【请填写功能名称】列表 list data
     */
    @PreAuthorize( "@ss.hasPermi('pay:bankCardAddress:list')" )
    @GetMapping("/list")
    public RspBase<List<BankCardAddress>> list(BankCardAddress bankCardAddress){
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<BankCardAddress> bankCardAddressList = bankCardAddressService.selectBankCardAddressList(bankCardAddress);
        return getRspBasePage( bankCardAddressList,pageDomain );
    }

    /**
     * 新增【请填写功能名称】add new bank card address
     */
    @PreAuthorize( "@ss.hasPermi('pay:bankCardAddress:add')" )
    @Log( title = "【请填写功能名称】", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody BankCardAddress bankCardAddress ){
        return RspBase.ok(bankCardAddressService.save( bankCardAddress ));
    }

    /**
     * 获取【请填写功能名称】详细信息
     * fetch data by id
     */
    @PreAuthorize( "@ss.hasPermi('pay:bankCardAddress:query')" )
    @GetMapping("/{id}")
    public RspBase<?> getById(@PathVariable String id){
        return RspBase.ok(bankCardAddressService.getById( id ));
    }

    /**
     * 删除【请填写功能名称】
     * delete by id
     */
    @PreAuthorize( "@ss.hasPermi('pay:bankCardAddress:remove')" )
    @Log( title = "银行字典列表", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        return RspBase.ok( bankCardAddressService.removeByIds( Arrays.asList( ids ) ) );
    }

    /**
     * 改变【出款银行状态】
     *  update
     */
    @PreAuthorize( "@ss.hasPermi('pay:bankCardAddress:edit')" )
    @Log( title = "出款银行状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeBankAddressStatus" )
    public RspBase<?> changeStatus( @RequestBody BankCardAddress bankCardAddress ) {
        return RspBase.ok(bankCardAddressService.updateBankCardAddress(bankCardAddress));
    }

}


















