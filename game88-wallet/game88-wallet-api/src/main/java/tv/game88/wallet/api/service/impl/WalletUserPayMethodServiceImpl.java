package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigBankListCache;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.wallet.api.dto.ReqPayMethod;
import tv.game88.wallet.api.dto.RspPayMethod;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.mapper.WalletUserPayMethodMapper;
import tv.game88.wallet.api.service.WalletUserPayMethodService;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author meng.jun
 * @description 针对表【wallet_user_pay_method】的数据库操作Service实现
 * @createDate 2023-08-21 17:33:52
 */
@Service
public class WalletUserPayMethodServiceImpl extends ServiceImpl<WalletUserPayMethodMapper, WalletUserPayMethod> implements WalletUserPayMethodService {
    @Resource
    private WalletUserMapper    walletUserMapper;
    @Resource
    private ConfigBankListCache configBankListCache;

    @Override
    public RspBase<?> bindNewPayMethod( String userId, ReqPayMethod reqPayMethod ) {
        boolean accountExist = this.baseMapper.exists( new QueryWrapper<WalletUserPayMethod>().eq( "bank_account", reqPayMethod.getAccount() ) );
        if( accountExist ){
            return RspBase.businessError( "该银行卡已经绑定,请输入其它银行卡号" );
        }

        WalletUser walletUser = walletUserMapper.selectById( userId );
        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
        }
        if ( walletUser.getIsVerified() < 2 || StringUtils.isBlank( walletUser.getRealName() ) ) {
            return RspBase.businessError( "用户未实名或实名未认证" );
        }
        if ( walletUser.getFundPassword() == null ) {
            return RspBase.businessError( "必须设置资金密码才能绑定支付方式" );
        }
        switch ( reqPayMethod.getMethodType() ) {
        case CREDIT_CARD -> {
            if ( reqPayMethod.getBankId() == null ) {
                return RspBase.businessError( "请选择开户行" );
            }
            if ( StringUtils.isBlank( reqPayMethod.getAccount() ) ) {
                return RspBase.businessError( "请输入银行卡号" );
            }
//            if ( !ValidatorUtil.checkBankCard( reqPayMethod.getAccount() ) ) {
//                return RspBase.businessError( "请输入正确的银行卡号" );
//            }
            if ( StringUtils.isBlank( reqPayMethod.getRealName() ) ) {
                return RspBase.businessError( "请输入微信实名姓名" );
            }
        }
        case WECHAT_PAY -> {
            if ( StringUtils.isBlank( reqPayMethod.getRealName() ) ) {
                return RspBase.businessError( "请输入微信实名姓名" );
            }
            if ( StringUtils.isBlank( reqPayMethod.getPayPicAddr() ) ) {
                return RspBase.businessError( "请上传收款码" );
            }
        }
        case ALIPAY -> {
            if ( StringUtils.isBlank( reqPayMethod.getAccount() ) ) {
                return RspBase.businessError( "请输入支付宝账号" );
            }
            if ( StringUtils.isBlank( reqPayMethod.getPayPicAddr() ) ) {
                return RspBase.businessError( "请上传收款码" );
            }
            reqPayMethod.setRealName( walletUser.getRealName() );
        }
        }
        WalletUserPayMethod walletUserPayMethod = new WalletUserPayMethod();
        walletUserPayMethod.setUserId( userId );
        walletUserPayMethod.setMethodType( reqPayMethod.getMethodType() );
        walletUserPayMethod.setPayAddrProvince( reqPayMethod.getPayAddrProvince() );
        walletUserPayMethod.setPayAddrCity( reqPayMethod.getPayAddrCity() );
        walletUserPayMethod.setRealName( reqPayMethod.getRealName() );
        walletUserPayMethod.setBankId( reqPayMethod.getBankId() );
        walletUserPayMethod.setBankAccount( reqPayMethod.getAccount() );
        walletUserPayMethod.setPayPicAddr( reqPayMethod.getPayPicAddr() );
        walletUserPayMethod.setCreateTime( LocalDateTime.now() );
        walletUserPayMethod.setAuditStatus( reqPayMethod.getMethodType() == WalletPayMethodEnum.ALIPAY ? 0 : 1 );
        int i = this.baseMapper.insert( walletUserPayMethod );
        return i > 0 ? RspBase.ok( "新增支付方式成功" ) : RspBase.businessError( "新增支付方式异常，请稍后再试" );
    }

    @Override
    public RspBase<?> unBindPayMethod( String userId, int payMethodId ) {
        WalletUserPayMethod walletUserPayMethod = this.baseMapper.selectById( payMethodId );
        if ( walletUserPayMethod == null ) {
            return RspBase.businessError( "支付方式不存在" );
        }
        if ( !walletUserPayMethod.getUserId().equals( userId ) ) {
            return RspBase.businessError( "绑定用户错误" );
        }
        int i = this.baseMapper.deleteById( payMethodId );
        return i > 0 ? RspBase.ok( "解绑支付方式成功" ) : RspBase.businessError( "解绑支付方式异常，请稍后再试" );
    }

    @Override
    public RspBase<Map<String, List<RspPayMethod>>> getPayMethod( String userId ) {
        List<WalletUserPayMethod> walletUserPayMethods = this.baseMapper.selectList( new LambdaQueryWrapper<WalletUserPayMethod>()
                .eq( WalletUserPayMethod::getUserId, userId )
                .eq( WalletUserPayMethod::getAuditStatus, 1 ) );

        Map<String, List<RspPayMethod>> resultMap = new LinkedHashMap<>();
        if ( !CollectionUtils.isEmpty( walletUserPayMethods ) ) {
            List<RspConfigBankList> effectList     = configBankListCache.getEffectList();
            String                  domainOssValue = ConfigDomainCacheUtil.me.getDomainOssValue();
            for ( WalletPayMethodEnum payMethodEnum : WalletPayMethodEnum.values() ) {
                resultMap.put( payMethodEnum.name(), new ArrayList<>() );
                for ( WalletUserPayMethod userPayMethod : walletUserPayMethods ) {
                    if ( userPayMethod.getMethodType() == payMethodEnum ) {
                        RspPayMethod rspPayMethod = new RspPayMethod();
                        rspPayMethod.setMethodId( userPayMethod.getMethodId() );
                        rspPayMethod.setRealName( userPayMethod.getRealName() );
                        rspPayMethod.setAccount( userPayMethod.getBankAccount() );

                        if ( StringUtils.isNotBlank( userPayMethod.getPayPicAddr() ) && !userPayMethod
                                .getPayPicAddr()
                                .startsWith( "http" ) ) {
                            rspPayMethod.setPayPicAddr( domainOssValue + userPayMethod.getPayPicAddr() );
                        } else {
                            rspPayMethod.setPayPicAddr( userPayMethod.getPayPicAddr() );
                        }
                        for ( RspConfigBankList rspConfigBank : effectList ) {
                            if ( Objects.equals( rspConfigBank.getId(), userPayMethod.getBankId() ) ) {
                                rspPayMethod.setBankName( rspConfigBank.getBankName() );
                                if ( StringUtils.isNotBlank( rspConfigBank.getBankIcon() ) && !rspConfigBank
                                        .getBankIcon()
                                        .startsWith( "http" ) ) {
                                    rspPayMethod.setBankIcon( domainOssValue + rspConfigBank.getBankIcon() );
                                }
                            }
                        }
                        resultMap.get( payMethodEnum.name() ).add( rspPayMethod );
                    }
                }
            }
        }
        return RspBase.ok( "获取成功", resultMap );
    }

    @Override
    public RspBase<Boolean> hasPayMethod( String userId , String methodType) {

        if(methodType== null ){
            return RspBase.ok( this.baseMapper.exists( new LambdaQueryWrapper<WalletUserPayMethod>()
                    .eq( WalletUserPayMethod::getUserId, userId )
                    .eq( WalletUserPayMethod::getAuditStatus, 1 ) ) );
        }else{
            return RspBase.ok( this.baseMapper.exists( new LambdaQueryWrapper<WalletUserPayMethod>()
                    .eq( WalletUserPayMethod::getUserId, userId )
                    .eq( WalletUserPayMethod::getMethodType ,  methodType  )
                    .eq( WalletUserPayMethod::getAuditStatus, 1 ) ) );
        }
    }
}