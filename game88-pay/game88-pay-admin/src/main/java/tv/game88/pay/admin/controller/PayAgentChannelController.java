package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.service.PayAgentChannelService;
import tv.game88.pay.api.service.PayAgentService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 代付通道Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payAgentChannel" )
public class PayAgentChannelController extends BaseController {
    @Resource
    private PayAgentChannelService payAgentChannelService;
    @Resource
    private PayAgentService        payAgentService;
    @Resource
    private PayCacheUtil           payCacheUtil;

    /**
     * 查询代付通道列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayAgentChannel>> list( PayAgentChannel payAgentChannel ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayAgentChannel> list = payAgentChannelService.selectPayAgentChannelList( payAgentChannel );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 激活中的代付通道选择列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:list')" )
    @GetMapping( "/effectList" )
    public RspBase<List<PayAgentChannel>> effectList() {
        PayAgentChannel payAgentChannel = new PayAgentChannel();
        payAgentChannel.setEffect( true );
        return RspBase.ok( payAgentChannelService.selectPayAgentChannelList( payAgentChannel ) );
    }

    /**
     * 导出代付通道列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:export')" )
    @Log( title = "代付通道", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<PayAgentChannel>> export( PayAgentChannel payAgentChannel ) {
        return RspBase.ok( payAgentChannelService.selectPayAgentChannelList( payAgentChannel ) );
    }

    /**
     * 获取代付通道详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayAgentChannel> getInfo( @PathVariable( "id" ) Long id ) {
        PayAgentChannel payAgentChannel = payAgentChannelService.getById( id );
        if ( payAgentChannel != null ) {
            String a = "**********";
            if ( StringUtils.isNotBlank( payAgentChannel.getHeaderValue() ) ) {
                payAgentChannel.setHeaderValue( a );
            }
            if ( StringUtils.isNotBlank( payAgentChannel.getSignMd5() ) ) {
                payAgentChannel.setSignMd5( a );
            }
            if ( StringUtils.isNotBlank( payAgentChannel.getSignPrivateKey() ) ) {
                payAgentChannel.setSignPrivateKey( a );
            }
            if ( StringUtils.isNotBlank( payAgentChannel.getSignPublicKey() ) ) {
                payAgentChannel.setSignPublicKey( a );
            }
        }
        return RspBase.ok( payAgentChannel );
    }

    /**
     * 新增代付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:add')" )
    @Log( title = "代付通道", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayAgentChannel payAgentChannel ) {
        if ( StringUtils.isNotBlank( payAgentChannel.getHeaderValue() ) ) {
            payAgentChannel.setHeaderValue( AESCoder.encrypt( payAgentChannel.getHeaderValue() ) );
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignMd5() ) ) {
            payAgentChannel.setSignMd5( AESCoder.encrypt( payAgentChannel.getSignMd5() ) );
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignPrivateKey() ) ) {
            payAgentChannel.setSignPrivateKey( AESCoder.encrypt( payAgentChannel.getSignPrivateKey() ) );
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignPublicKey() ) ) {
            payAgentChannel.setSignPublicKey( AESCoder.encrypt( payAgentChannel.getSignPublicKey() ) );
        }
        payAgentChannel.setCreateBy( SecurityUtils.getUsername() );
        payAgentChannel.setCreateTime( LocalDateTime.now() );
        return toResult( payAgentChannelService.save( payAgentChannel ) );
    }

    /**
     * 修改代付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:edit')" )
    @Log( title = "代付通道", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayAgentChannel payAgentChannel ) {
        //如果还有*号加密的保存用原来的
        PayAgentChannel payAgentChannelOld = payAgentChannelService.getById( payAgentChannel.getId() );
        String          a                  = "***";
        if ( StringUtils.isNotBlank( payAgentChannel.getHeaderValue() ) ) {
            if ( payAgentChannel.getHeaderValue().contains( a ) ) {
                payAgentChannel.setHeaderValue( payAgentChannelOld.getHeaderValue() );
            } else {
                payAgentChannel.setHeaderValue( AESCoder.encrypt( payAgentChannel.getHeaderValue() ) );
            }
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignMd5() ) ) {
            if ( payAgentChannel.getSignMd5().contains( a ) ) {
                payAgentChannel.setSignMd5( payAgentChannelOld.getSignMd5() );
            } else {
                payAgentChannel.setSignMd5( AESCoder.encrypt( payAgentChannel.getSignMd5() ) );
            }
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignPrivateKey() ) ) {
            if ( payAgentChannel.getSignPrivateKey().contains( a ) ) {
                payAgentChannel.setSignPrivateKey( payAgentChannelOld.getSignPrivateKey() );
            } else {
                payAgentChannel.setSignPrivateKey( AESCoder.encrypt( payAgentChannel.getSignPrivateKey() ) );
            }
        }
        if ( StringUtils.isNotBlank( payAgentChannel.getSignPublicKey() ) ) {
            if ( payAgentChannel.getSignPublicKey().contains( a ) ) {
                payAgentChannel.setSignPublicKey( payAgentChannelOld.getSignPublicKey() );
            } else {
                payAgentChannel.setSignPublicKey( AESCoder.encrypt( payAgentChannel.getSignPublicKey() ) );
            }
        }
        payAgentChannel.setUpdateBy( SecurityUtils.getUsername() );
        payAgentChannel.setUpdateTime( LocalDateTime.now() );
        boolean isSave = payAgentChannelService.updateById( payAgentChannel );
        if ( isSave ) {
            payCacheUtil.clearPayAgentChannel( payAgentChannel.getId() );
        }
        return toResult( isSave );
    }

    /**
     * 删除代付通道
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:remove')" )
    @Log( title = "代付通道", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isSave = payAgentChannelService.removeBatchByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            for ( Long id : ids ) {
                payCacheUtil.clearPayAgentChannel( id );
            }
        }
        return toResult( isSave );
    }

    /**
     * 代付通道状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:effect')" )
    @Log( title = "代付通道激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        PayAgentChannel update = new PayAgentChannel();
        update.setId( id );
        update.setEffect( effect );
        return toResult( payAgentChannelService.updateById( update ) );
    }

    /**
     * 代付下单
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:order')" )
    @Log( title = "代付下单", businessType = BusinessType.ORDER )
    @PostMapping( "/payAgentOrder" )
    public RspBase<?> payAgentOrder( @RequestBody ReqPayAgent reqPayAgent ) throws Exception {
        SecurityUtils.verifyMFACode( reqPayAgent.getGoogleAuthCode() );
        return payAgentService.payAgentOrder( reqPayAgent, SecurityUtils.getUsername() );
    }

    /**
     * 批量代付下单
     */
    @PreAuthorize( "@ss.hasPermi('pay:agentChannel:order')" )
    @Log( title = "批量代付下单", businessType = BusinessType.ORDER )
    @PostMapping( "/payAgentOrders" )
    public RspBase<?> payAgentOrders( @RequestBody ReqPayAgent reqPayAgent ) throws Exception {
        SecurityUtils.verifyMFACode( reqPayAgent.getGoogleAuthCode() );
        return payAgentService.payAgentOrders( reqPayAgent, SecurityUtils.getUsername() );
    }
}
