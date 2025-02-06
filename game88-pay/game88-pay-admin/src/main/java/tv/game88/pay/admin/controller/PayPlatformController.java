package tv.game88.pay.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.service.PayChannelService;
import tv.game88.pay.api.service.PayPlatformService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付平台Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payPlatform" )
public class PayPlatformController extends BaseController {
    @Resource
    private PayPlatformService payPlatformService;
    @Resource
    private PayChannelService  payChannelService;
    @Resource
    private PayCacheUtil       payCacheUtil;

    /**
     * 查询支付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayPlatform>> list( PayPlatform payPlatform ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayPlatform> list = payPlatformService.selectPayPlatformList( payPlatform );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询支付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayPlatform>> listAll() {
        return RspBase.ok( payPlatformService.selectPayPlatformList( null ) );
    }

    /**
     * 导出支付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:export')" )
    @Log( title = "支付平台", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayPlatform payPlatform, HttpServletResponse response ) {
        List<PayPlatform> list = payPlatformService.selectPayPlatformList( payPlatform );
        ExportExcelUtil.exportBigExcel( list, "支付平台", "支付平台表", PayPlatform.class, response );
    }

    /**
     * 获取支付平台详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayPlatform> getInfo( @PathVariable( "id" ) Long id ) {
        PayPlatform payPlatform = payPlatformService.getById( id );
        if ( payPlatform != null ) {
            String a = "**********";
            if ( StringUtils.isNotBlank( payPlatform.getSignMd5() ) ) {
                payPlatform.setSignMd5( a );
            }
            if ( StringUtils.isNotBlank( payPlatform.getSignPrivateKey() ) ) {
                payPlatform.setSignPrivateKey( a );
            }
            if ( StringUtils.isNotBlank( payPlatform.getSignPublicKey() ) ) {
                payPlatform.setSignPublicKey( a );
            }
        }
        return RspBase.ok( payPlatform );
    }

    /**
     * 新增支付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:add')" )
    @Log( title = "支付平台", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayPlatform payPlatform ) {
        if ( StringUtils.isNotBlank( payPlatform.getWhiteIp() ) ) {
            payPlatform.setWhiteIp( payPlatform.getWhiteIp().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        }
        if ( StringUtils.isNotBlank( payPlatform.getSignMd5() ) ) {
            payPlatform.setSignMd5( AESCoder.encrypt( payPlatform.getSignMd5() ) );
        }
        if ( StringUtils.isNotBlank( payPlatform.getSignPrivateKey() ) ) {
            payPlatform.setSignPrivateKey( AESCoder.encrypt( payPlatform.getSignPrivateKey() ) );
        }
        if ( StringUtils.isNotBlank( payPlatform.getSignPublicKey() ) ) {
            payPlatform.setSignPublicKey( AESCoder.encrypt( payPlatform.getSignPublicKey() ) );
        }
        payPlatform.setCreateBy( SecurityUtils.getUsername() );
        payPlatform.setCreateTime( LocalDateTime.now() );
        return toResult( payPlatformService.save( payPlatform ) );
    }

    /**
     * 修改支付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:edit')" )
    @Log( title = "支付平台", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayPlatform payPlatform ) {
        //如果还有*号加密的保存用原来的
        PayPlatform payPlatformOld = payPlatformService.getById( payPlatform.getId() );
        String      a              = "***";
        if ( StringUtils.isNotBlank( payPlatform.getSignMd5() ) ) {
            if ( payPlatform.getSignMd5().contains( a ) ) {
                payPlatform.setSignMd5( payPlatformOld.getSignMd5() );
            } else {
                payPlatform.setSignMd5( AESCoder.encrypt( payPlatform.getSignMd5() ) );
            }
        }
        if ( StringUtils.isNotBlank( payPlatform.getSignPrivateKey() ) ) {
            if ( payPlatform.getSignPrivateKey().contains( a ) ) {
                payPlatform.setSignPrivateKey( payPlatformOld.getSignPrivateKey() );
            } else {
                payPlatform.setSignPrivateKey( AESCoder.encrypt( payPlatform.getSignPrivateKey() ) );
            }
        }
        if ( StringUtils.isNotBlank( payPlatform.getSignPublicKey() ) ) {
            if ( payPlatform.getSignPublicKey().contains( a ) ) {
                payPlatform.setSignPublicKey( payPlatformOld.getSignPublicKey() );
            } else {
                payPlatform.setSignPublicKey( AESCoder.encrypt( payPlatform.getSignPublicKey() ) );
            }
        }

        if ( StringUtils.isNotBlank( payPlatform.getWhiteIp() ) ) {
            payPlatform.setWhiteIp( payPlatform.getWhiteIp().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        }
        payPlatform.setUpdateBy( SecurityUtils.getUsername() );
        payPlatform.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = payPlatformService.updateById( payPlatform );
        if ( isUpdate ) {
            payCacheUtil.clearPayPlatform( payPlatform.getId() );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除支付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payPlatform:remove')" )
    @Log( title = "支付平台", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{id}" )
    public RspBase<?> remove( @PathVariable Long id ) {
        //查询此支付平台下还有无支付通道
        long a = payChannelService.count( new QueryWrapper<PayChannel>().eq( "type_id", id ).eq( "del_flag", 0 ) );
        if ( a > 0 ) {
            return RspBase.businessError( "此支付类型下还存在支付通道,删除失败" );
        }
        boolean isDel = payPlatformService.removeById( id );
        if ( isDel ) {
            payCacheUtil.clearPayPlatform( id );
        }
        return toResult( isDel );
    }
}
