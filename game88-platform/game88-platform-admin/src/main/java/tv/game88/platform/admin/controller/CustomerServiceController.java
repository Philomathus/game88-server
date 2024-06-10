package tv.game88.platform.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.platform.api.entity.CustomerService;
import tv.game88.platform.api.service.CustomerServiceService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping( "/config/customerService" )
public class CustomerServiceController extends BaseController {
    @Resource
    private CustomerServiceService customerServiceService;

    @PreAuthorize( "@ss.hasPermi('config:customerService:list')" )
    @GetMapping( "/list" )
    public RspBase<List<CustomerService>> list( CustomerService customerService ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        return getRspBasePage( customerServiceService.selectCustomerServiceList(customerService), pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('config:customerService:list')" )
    @GetMapping( value = "/{id}" )
    public RspBase<CustomerService> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( customerServiceService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:customerService:add')" )
    @Log( title = "客户服务", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody CustomerService customerService ) {
        customerService.setCreateBy( SecurityUtils.getUsername() );
        customerService.setCreateTime( LocalDateTime.now() );
        customerService.setStatus( false );
        return toResult( customerServiceService.save( customerService ) );
    }

    @PreAuthorize( "@ss.hasPermi('config:customerService:edit')" )
    @Log( title = "客户服务", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody CustomerService customerService ) {
        customerService.setUpdateBy( SecurityUtils.getUsername() );
        customerService.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = customerServiceService.updateById( customerService );
        return toResult( isUpdate );
    }

    @PreAuthorize( "@ss.hasPermi('config:customerService:delete')" )
    @Log( title = "客户服务", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = customerServiceService.removeByIds( Arrays.asList( ids ) );
        return toResult( isRemove );
    }

    @PreAuthorize( "@ss.hasPermi('config:customerService:effect')" )
    @Log( title = "激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{status}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean status ) {
        CustomerService update = new CustomerService();
        update.setId( id );
        update.setStatus( status );
        update.setUpdateBy( SecurityUtils.getUsername() );
        update.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = customerServiceService.updateById( update );
        return toResult( isUpdate );
    }

}