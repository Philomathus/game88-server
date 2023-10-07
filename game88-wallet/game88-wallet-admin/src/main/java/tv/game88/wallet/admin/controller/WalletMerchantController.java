package tv.game88.wallet.admin.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.GoogleAuthUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.service.WalletMerchantService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 钱包商户Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/admin/walletMerchant" )
public class WalletMerchantController extends BaseController {
    @Resource
    private WalletMerchantService walletMerchantService;
    @Resource
    private PasswordEncoder       passwordEncoder;

    /**
     * 查询钱包商户列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMerchant:list')" )
    @GetMapping( "/list" )
    public RspBase<List<WalletMerchant>> list( WalletMerchant walletMerchant ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletMerchant> list = walletMerchantService.selectWalletMerchantList( walletMerchant );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取钱包商户详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMerchant:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<WalletMerchant> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( walletMerchantService.getById( id ) );
    }

    /**
     * 新增钱包商户
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMerchant:add')" )
    @Log( title = "钱包商户", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody WalletMerchant walletMerchant ) {
        walletMerchant.setCreatedTime( LocalDateTime.now() );
        walletMerchant.setUpdatedTime( walletMerchant.getCreatedTime() );
        walletMerchant.setAmount( BigDecimal.ZERO );
        walletMerchant.setFrozenAmount( BigDecimal.ZERO );
        String md5Key = DigestUtils.md5Hex( System.currentTimeMillis() + RandomStringUtils.randomAlphabetic( 2 ) );
        walletMerchant.setMd5Key( AESCoder.encrypt( md5Key ) );
        // 初始密码a123456
        walletMerchant.setPassword( passwordEncoder.encode( "a123456" ) );
        walletMerchant.setStatus( 1 );
        return toResult( walletMerchantService.save( walletMerchant ) );
    }

    // TODO 添加商户余额, 需校验管理员MFA密钥

    // TODO 扣除商户余额

    // TODO 重置商户MD5密钥

    // TODO 重置商户登录密码

    // TODO 修改商户状态

    /**
     * 获取MFA验证码二维码
     */
    @GetMapping( "getOtpSecretQrcode" )
    public RspBase<Map<String, String>> getOtpSecretQrcode( String name ) {
        String secretKey    = GoogleAuthUtil.createSecretKey();
        String qrBarcodeUrl = GoogleAuthUtil.getQRBarcodeURL( name, "UPay管理后台", secretKey );
        return RspBase.ok( Map.of( "secretKey", secretKey, "qrBarcodeBase",
                GoogleAuthUtil.tranUrlToBase64String( qrBarcodeUrl ) ) );
    }

    // TODO 重置商户MFA秘钥

    // TODO 商户绑定MFA密钥
}