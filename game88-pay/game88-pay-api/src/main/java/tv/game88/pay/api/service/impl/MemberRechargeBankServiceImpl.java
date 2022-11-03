package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.ValidatorUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.dto.RspMemberCard;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.pay.api.dto.ReqMemberCard;
import tv.game88.pay.api.dto.RspConfigBank;
import tv.game88.pay.api.dto.RspWithdrawBank;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.entity.MemberRechargeBank;
import tv.game88.pay.api.mapper.ConfigBankListMapper;
import tv.game88.pay.api.mapper.ConfigBankMapper;
import tv.game88.pay.api.mapper.MemberRechargeBankMapper;
import tv.game88.pay.api.service.MemberRechargeBankService;
import tv.game88.pay.api.service.MemberWithdrawDetailService;
import tv.game88.pay.api.utils.BankAddressUtil;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class MemberRechargeBankServiceImpl extends ServiceImpl<MemberRechargeBankMapper, MemberRechargeBank> implements MemberRechargeBankService {
    @Resource
    private ConfigBankMapper            configBankMapper;
    @Resource
    private ConfigBankListMapper        configBankListMapper;
    @Resource
    private MemberInfoMapper            memberInfoMapper;
    @Resource
    private MemberCardMapper            memberCardMapper;
    @Resource
    private MemberWithdrawDetailService memberWithdrawDetailService;

    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;
    @Resource
    private RestTemplate       restTemplate;
    @Resource
    private RedisUtils         redisUtils;

    @Override
    public List<RspConfigBank> selectList( String memberId, Integer vip ) {
        List<RspConfigBank> configBankList = configBankMapper.selectRspList( vip );
        String              memberProvince = memberInfoMapper.selectMemberProvince( memberId );
        if ( StringUtils.isBlank( memberProvince ) || "无效IP格式".equals( memberProvince ) ) {
            try {
                String sf = memberInfoMapper.funGetaddressProvinces( ServletUtil.getIp() );
                if ( StringUtils.isNotBlank( sf ) ) {
                    MemberInfo update = new MemberInfo();
                    update.setId( memberId );
                    update.setLoginProvince( sf );
                    memberInfoMapper.updateById( update );

                    memberProvince = sf;
                }
            } catch ( Exception ignored ) {
            }
        }
        if ( StringUtils.isNotBlank( memberProvince ) && !"无效IP格式".equals( memberProvince ) ) {
            String finalMemberProvice = memberProvince;
            configBankList.removeIf( configBank -> {
                if ( StringUtils.isNotBlank( configBank.getRestProvince() ) ) {
                    // 银行卡省份限制,如果包含则剔除
                    return configBank.getRestProvince().contains( finalMemberProvice );
                }
                return false;
            } );
        }
        if ( !CollectionUtils.isEmpty( configBankList ) ) {
            String bankChargeLimit = configEnvCacheUtil.getConf( "bank_charge_limit", "" );
            for ( RspConfigBank rspConfigBank : configBankList ) {
                if ( StringUtils.isNotBlank( bankChargeLimit ) ) {
                    if ( StringUtils.isNotNull( rspConfigBank.getRechargeLimitMin() )
                            && StringUtils.isNull( rspConfigBank.getRechargeLimitMax() ) ) {
                        rspConfigBank.setBankChargeLimit(
                                rspConfigBank.getRechargeLimitMin() + "," + bankChargeLimit.split( "," )[ 1 ] );
                    } else if ( StringUtils.isNull( rspConfigBank.getRechargeLimitMin() )
                            && StringUtils.isNotNull( rspConfigBank.getRechargeLimitMax() ) ) {
                        rspConfigBank.setBankChargeLimit(
                                bankChargeLimit.split( "," )[ 0 ] + "," + rspConfigBank.getRechargeLimitMax() );
                    } else if ( StringUtils.isNotNull( rspConfigBank.getRechargeLimitMin() )
                            && StringUtils.isNotNull( rspConfigBank.getRechargeLimitMax() ) ) {
                        rspConfigBank.setBankChargeLimit(
                                rspConfigBank.getRechargeLimitMin() + "," + rspConfigBank.getRechargeLimitMax() );
                    } else {
                        rspConfigBank.setBankChargeLimit( bankChargeLimit );
                    }
                }
                if ( rspConfigBank.getBankAddress() == null ) {
                    rspConfigBank.setBankAddress( "" );
                }
            }
        }
        return configBankList;
    }

    @Override
    public RspBase<RspWithdrawBank> getBindCardList( String memberId ) {
        RspWithdrawBank     rspWithdrawBank = new RspWithdrawBank();
        List<RspMemberCard> memberCardList  = memberCardMapper.selectRspList( memberId );
        rspWithdrawBank.setMemberCardList( memberCardList );
        MemberInfo memberInfo = memberInfoMapper.selectById( memberId );
        rspWithdrawBank.setRspWithdrawInfo( memberWithdrawDetailService.getRspWithdrawInfo( memberInfo ) );
        if ( configEnvCacheUtil.getConfBool( "is_display_gopay" ) ) {
            rspWithdrawBank.getSpecialBankInfoMap().put( "GOPAY", configBankListMapper.findBankIdByNameOrCode( "GOPAY" ) );
        } else {
            memberCardList.removeIf( rspMemberCard -> "GOPAY".equalsIgnoreCase( rspMemberCard.getBankCode() )
                    || "GOPAY".equalsIgnoreCase( rspMemberCard.getBankName() ) );
        }
        if ( configEnvCacheUtil.getConfBool( "is_display_okpay" ) ) {
            rspWithdrawBank.getSpecialBankInfoMap().put( "OKPAY", configBankListMapper.findBankIdByNameOrCode( "OKPAY" ) );
        } else {
            memberCardList.removeIf( rspMemberCard -> "OKPAY".equalsIgnoreCase( rspMemberCard.getBankCode() )
                    || "OKPAY".equalsIgnoreCase( rspMemberCard.getBankName() ) );
        }
        if ( configEnvCacheUtil.getConfBool( "is_display_vippay" ) ) {
            rspWithdrawBank.getSpecialBankInfoMap().put( "VIPPAY", configBankListMapper.findBankIdByNameOrCode( "VIPPAY" ) );
        } else {
            memberCardList.removeIf( rspMemberCard -> "VIPPAY".equalsIgnoreCase( rspMemberCard.getBankCode() )
                    || "VIPPAY".equalsIgnoreCase( rspMemberCard.getBankName() ) );
        }
        return RspBase.ok( rspWithdrawBank );
    }

    @Override
    public boolean setBindCardDv( String memberId, Long cardId ) {
        MemberCard       update     = new MemberCard();
        List<MemberCard> resultList = memberCardMapper.selectMemberCard( memberId );
        for ( MemberCard card : resultList ) {
            if ( card.isDv() && !Objects.equals( cardId, card.getId() ) ) {
                update.setId( card.getId() );
                update.setDv( false );
                memberCardMapper.updateById( update );
                break;
            }
        }
        update.setId( cardId );
        update.setDv( true );
        return memberCardMapper.updateById( update ) > 0;
    }

    @Override
    public RspBase<?> setBindCard( String memberId, ReqMemberCard reqMemberCard ) {
        if ( !org.springframework.util.StringUtils.hasText( reqMemberCard.getBankAccount() ) ) {
            return RspBase.businessError( "银行卡号为空" );
        }
        reqMemberCard.setBankAccount( reqMemberCard.getBankAccount().trim() );
        if ( reqMemberCard.getBankAccount().length() > 100 ) {
            return RspBase.businessError( "请输入正确的银行卡号" );
        }
        if ( !ValidatorUtil.isAccount( reqMemberCard.getBankAccount() ) ) {
            return RspBase.businessError( "请输入正确的银行卡号" );
        }
        if ( StringUtils.isBlank( reqMemberCard.getRealName() ) ) {
            return RspBase.businessError( "姓名为空" );
        }
        ConfigBankList configBankList = configBankListMapper.selectById( reqMemberCard.getBankId() );
        if ( configBankList == null ) {
            return RspBase.businessError( "此银行卡类型未开放绑定功能,敬请期待" );
        }
        //卡号不能重复
        long countCard = memberCardMapper.selectCount( new QueryWrapper<MemberCard>().eq( "bank_account",
                reqMemberCard.getBankAccount() ) );
        if ( countCard > 0 ) {
            return RspBase.businessError( "该银行卡已经绑定,请输入其它银行卡号" );
        }
        Boolean          dfault     = true;
        List<MemberCard> resultList = memberCardMapper.selectMemberCard( memberId );
        if ( resultList.size() > 0 ) {
            dfault = false;
            if ( !resultList.get( 0 ).getRealName().equals( reqMemberCard.getRealName() ) ) {
                return RspBase.businessError( "姓名必须与首次绑定的一致" );
            }
        }
        if ( resultList.size() >= 10 ) {
            return RspBase.businessError( "提现卡超过10个，请解绑弃用提现卡" );
        }
        //加锁
        if ( !redisUtils.lock( "setBindCard" + reqMemberCard.getBankAccount(), 10 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        MemberCard memberCard = new MemberCard();
        memberCard.setBankAccount( reqMemberCard.getBankAccount() );
        memberCard.setBankAddress( reqMemberCard.getBankAddress() );
        memberCard.setCreateTime( new Date() );
        memberCard.setMemberId( memberId );
        memberCard.setRealName( reqMemberCard.getRealName() );
        memberCard.setDv( dfault );
        memberCard.setBankId( configBankList.getId() );
        String bankAccount = reqMemberCard.getBankAccount();
        try {
            memberCard.setRealBankAddress( getRealBankAddress( bankAccount ) );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        int isIns = memberCardMapper.insert( memberCard );
        return isIns > 0 ? RspBase.ok( "绑定成功" ) : RspBase.businessError( "绑定失败" );
    }

    private String getRealBankAddress( String bankAccount ) throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String secretId  = configEnvCacheUtil.getConf( "bank_card_id" );
        String secretKey = configEnvCacheUtil.getConf( "bank_card_key" );
        String url       = configEnvCacheUtil.getConf( "bank_card_url" );
        return BankAddressUtil.getBankAddress( bankAccount, secretId, secretKey, url );
    }
}

