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
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.service.PayChannelService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付通道Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payChannel" )
public class PayChannelController extends BaseController {
    @Resource
    private PayChannelService payChannelService;
    @Resource
    private PayCacheUtil      payCacheUtil;

    /**
     * 查询支付通道列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayChannel>> list( PayChannel payChannel ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayChannel> list = payChannelService.selectPayChannelList( payChannel );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('pay:payChannel:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayChannel>> listAll(){
        return RspBase.ok(payChannelService.selectPayChannelList( null ));
    }

    /**
     * 导出支付通道列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:export')" )
    @Log( title = "支付通道", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayChannel payChannel, HttpServletResponse response ) {
        List<PayChannel> list = payChannelService.selectPayChannelList( payChannel );
        ExportExcelUtil.exportExcel( list, "支付通道", "支付通道表", PayChannel.class, response );
    }

    /**
     * 获取支付通道详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayChannel> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( payChannelService.getById( id ) );
    }

    /**
     * 新增支付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:add')" )
    @Log( title = "支付通道", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayChannel payChannel ) {
        payChannel.setQuickAmount( payChannel.getQuickAmount().trim().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        payChannel.setCreateBy( SecurityUtils.getUsername() );
        payChannel.setCreateTime( LocalDateTime.now() );
        return toResult( payChannelService.save( payChannel ) );
    }

    /**
     * 修改支付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:edit')" )
    @Log( title = "支付通道", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayChannel payChannel ) {
        payChannel.setQuickAmount( payChannel.getQuickAmount().trim().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        payChannel.setUpdateBy( SecurityUtils.getUsername() );
        payChannel.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = payChannelService.updateById( payChannel );
        if ( isUpdate ) {
            payCacheUtil.clearPayChannel( payChannel.getId() );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除支付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:remove')" )
    @Log( title = "支付通道", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        List<PayChannel> updateBatch = new ArrayList<>();
        for ( Long id : ids ) {
            PayChannel update = new PayChannel();
            update.setId( id );
            update.setEffect( false );
            update.setDelFlag( true );
            updateBatch.add( update );
        }
        return toResult( payChannelService.updateBatchById( updateBatch ) );
    }

    /**
     * 修改支付通道状态
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:effect')" )
    @Log( title = "支付通道激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        PayChannel update = new PayChannel();
        update.setId( id );
        update.setEffect( effect );
        return toResult( payChannelService.updateById( update ) );
    }

    /**
     * 修改支付通道回调状态
     */
    @PreAuthorize( "@ss.hasPermi('pay:payChannel:effect')" )
    @Log( title = "支付通道回调状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeCallback/{id}/{canCallback}" )
    public RspBase<?> changeCallback( @PathVariable Long id, @PathVariable Boolean canCallback ) {
        PayChannel update = new PayChannel();
        update.setId( id );
        update.setCanCallback( canCallback );
        boolean isUpdate = payChannelService.updateById( update );
        if ( isUpdate ) {
            payCacheUtil.clearPayChannel( id );
        }
        return toResult( isUpdate );
    }
}
