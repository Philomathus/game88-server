package tv.game88.pay.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.service.PayAgentPlatformService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 代付平台Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payAgentPlatform" )
public class PayAgentPlatformController extends BaseController {
    @Resource
    private PayAgentPlatformService payAgentPlatformService;
    @Resource
    private PayCacheUtil            payCacheUtil;

    /**
     * 查询代付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayAgentPlatform>> list( PayAgentPlatform payAgentPlatform ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayAgentPlatform> list = payAgentPlatformService.selectPayAgentPlatformList( payAgentPlatform );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询代付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayAgentPlatform>> listAll() {
        return RspBase.ok( payAgentPlatformService.selectPayAgentPlatformList( null ) );
    }

    /**
     * 导出代付平台列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:export')" )
    @Log( title = "代付平台", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<PayAgentPlatform>> export(PayAgentPlatform payAgentPlatform ) {
        return RspBase.ok( payAgentPlatformService.selectPayAgentPlatformList( payAgentPlatform ) );
    }

    /**
     * 获取代付平台详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayAgentPlatform> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( payAgentPlatformService.getById( id ) );
    }

    /**
     * 新增代付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:add')" )
    @Log( title = "代付平台", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayAgentPlatform payAgentPlatform ) {
        //代付通道编码唯一校验
        if ( StringUtils.isBlank( payAgentPlatform.getCode() ) ) {
            return RspBase.businessError( "代付平台编码不能为空" );
        }
        if ( payAgentPlatformService.count( new QueryWrapper<PayAgentPlatform>().eq( "code", payAgentPlatform.getCode() ) )
                > 0 ) {
            return RspBase.businessError( "此代付平台编码已存在，请更换另一个编码" );
        }
        if ( StringUtils.isNotBlank( payAgentPlatform.getWhiteIp() ) ) {
            payAgentPlatform.setWhiteIp( payAgentPlatform.getWhiteIp().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getHeaderValue() ) ) {
            payAgentPlatform.setHeaderValueExplain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignMd5() ) ) {
            payAgentPlatform.setSignMd5Explain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignPublicKey() ) ) {
            payAgentPlatform.setSignPublicKeyExplain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignPrivateKey() ) ) {
            payAgentPlatform.setSignPrivateKeyExplain( "" );
        }
        payAgentPlatform.setCreateBy( SecurityUtils.getUsername() );
        payAgentPlatform.setCreateTime( LocalDateTime.now() );
        return toResult( payAgentPlatformService.save( payAgentPlatform ) );
    }

    /**
     * 修改代付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:edit')" )
    @Log( title = "代付平台", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayAgentPlatform payAgentPlatform ) {
        if ( StringUtils.isBlank( payAgentPlatform.getCode() ) ) {
            return RspBase.businessError( "代付平台编码不能为空" );
        }
        if ( payAgentPlatformService.count( new QueryWrapper<PayAgentPlatform>()
                .eq( "code", payAgentPlatform.getCode() )
                .eq( "id", payAgentPlatform.getId() ) ) <= 0 ) {
            return RspBase.businessError( "代付平台编码不允许修改" );
        }
        if ( StringUtils.isNotBlank( payAgentPlatform.getWhiteIp() ) ) {
            payAgentPlatform.setWhiteIp( payAgentPlatform.getWhiteIp().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getHeaderValue() ) ) {
            payAgentPlatform.setHeaderValueExplain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignMd5() ) ) {
            payAgentPlatform.setSignMd5Explain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignPublicKey() ) ) {
            payAgentPlatform.setSignPublicKeyExplain( "" );
        }
        if ( BooleanUtils.isNotTrue( payAgentPlatform.getSignPrivateKey() ) ) {
            payAgentPlatform.setSignPrivateKeyExplain( "" );
        }
        payAgentPlatform.setUpdateBy( SecurityUtils.getUsername() );
        payAgentPlatform.setUpdateTime( LocalDateTime.now() );
        boolean isSave = payAgentPlatformService.updateById( payAgentPlatform );
        if ( isSave ) {
            payCacheUtil.clearPayAgentPlatform( payAgentPlatform.getCode() );
        }
        return toResult( isSave );
    }

    /**
     * 删除代付平台
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentPlatform:remove')" )
    @Log( title = "代付平台", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        List<Long>             idList            = Arrays.asList( ids );
        List<PayAgentPlatform> payAgentPlatforms = payAgentPlatformService.listByIds( idList );
        boolean                isSave            = payAgentPlatformService.removeBatchByIds( idList );
        if ( isSave ) {
            for ( PayAgentPlatform payAgentPlatform : payAgentPlatforms ) {
                payCacheUtil.clearPayAgentPlatform( payAgentPlatform.getCode() );
            }
        }
        return toResult( isSave );
    }
}
