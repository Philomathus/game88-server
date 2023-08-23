package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.ValidatorUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.SmsPhoneCacheUtil;
import tv.game88.core.utils.SmsApi;
import tv.game88.wallet.api.dto.MobileLogin;
import tv.game88.wallet.api.dto.Phone;
import tv.game88.wallet.api.dto.RspMember;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletUserService;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:32:24
 */
@Log4j2
@Service
public class WalletUserServiceImpl extends ServiceImpl<WalletUserMapper, WalletUser> implements WalletUserService {
    @Resource
    private SmsPhoneCacheUtil smsPhoneCacheUtil;
    @Resource
    private SmsApi            smsApi;

    @Override
    public RspBase<?> sendSmsVerifyCode( Phone phone ) {
        if ( StringUtils.isBlank( phone.getPhone() ) ) {
            return RspBase.businessError( "请输入你的手机号" );
        }
        if ( ValidatorUtil.isMobile( phone.getPhone() ) ) {
            return RspBase.businessError( "手机号码不正确" );
        }
        if ( Objects.nonNull( smsPhoneCacheUtil.getSmsPhoneExpire( phone.getPhone() ) ) ) {
            return RspBase.businessError( "发送验证码频繁,请稍后发送" );
        }
        try {
            String code     = smsPhoneCacheUtil.getPhoneCode( phone.getPhone() );
            String indexStr = smsPhoneCacheUtil.getPhoneIndex( phone.getPhone() );
            int    index    = Integer.parseInt( indexStr == null ? "-1" : indexStr ) + 1;
            code = smsApi.sendSms( phone.getPhone(), index, code );
            smsPhoneCacheUtil.setSmsPhoneCache( phone.getPhone(), code, String.valueOf( index ) );
            return RspBase.ok();
        } catch ( Exception e ) {
            log.error( "发送短信失败phone:{}", phone.getPhone(), e );
            return RspBase.businessError( e.getMessage() );
        }
    }

    @Override
    public RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        return null;
    }

    @Override
    public RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        return null;
    }
}




