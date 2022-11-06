package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.ValidatorUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.dto.RspMemberCard;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.service.RecommendService;
import tv.game88.pay.api.dto.*;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.entity.MemberRechargeBank;
import tv.game88.pay.api.entity.PayRechargeBank;
import tv.game88.pay.api.mapper.ConfigBankListMapper;
import tv.game88.pay.api.mapper.MemberRechargeBankMapper;
import tv.game88.pay.api.mapper.PayRechargeBankMapper;
import tv.game88.pay.api.service.MemberRechargeBankService;
import tv.game88.pay.api.service.MemberWithdrawDetailService;
import tv.game88.pay.api.utils.BankAddressUtil;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
public class MemberRechargeBankServiceImpl extends ServiceImpl<MemberRechargeBankMapper, MemberRechargeBank> implements MemberRechargeBankService {
    @Resource
    private PayRechargeBankMapper       payRechargeBankMapper;
    @Resource
    private ConfigBankListMapper        configBankListMapper;
    @Resource
    private MemberInfoMapper            memberInfoMapper;
    @Resource
    private MemberCardMapper            memberCardMapper;
    @Resource
    private MemberWithdrawDetailService memberWithdrawDetailService;
    @Resource
    private RecommendService            recommendService;
    @Resource
    private MemberMoneyManager          memberMoneyManager;

    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;
    @Resource
    private RedisUtils         redisUtils;

    @Override
    public List<RspPayRechargeBank> selectList( String memberId, Integer vip ) {
        List<RspPayRechargeBank> configBankList = payRechargeBankMapper.selectRspList( vip );
        String                   memberProvince = memberInfoMapper.selectMemberProvince( memberId );
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
            for ( RspPayRechargeBank rspPayRechargeBank : configBankList ) {
                if ( StringUtils.isNotBlank( bankChargeLimit ) ) {
                    if ( StringUtils.isNotNull( rspPayRechargeBank.getRechargeLimitMin() )
                            && StringUtils.isNull( rspPayRechargeBank.getRechargeLimitMax() ) ) {
                        rspPayRechargeBank.setBankChargeLimit(
                                rspPayRechargeBank.getRechargeLimitMin() + "," + bankChargeLimit.split( "," )[ 1 ] );
                    } else if ( StringUtils.isNull( rspPayRechargeBank.getRechargeLimitMin() )
                            && StringUtils.isNotNull( rspPayRechargeBank.getRechargeLimitMax() ) ) {
                        rspPayRechargeBank.setBankChargeLimit(
                                bankChargeLimit.split( "," )[ 0 ] + "," + rspPayRechargeBank.getRechargeLimitMax() );
                    } else if ( StringUtils.isNotNull( rspPayRechargeBank.getRechargeLimitMin() )
                            && StringUtils.isNotNull( rspPayRechargeBank.getRechargeLimitMax() ) ) {
                        rspPayRechargeBank.setBankChargeLimit(
                                rspPayRechargeBank.getRechargeLimitMin() + "," + rspPayRechargeBank.getRechargeLimitMax() );
                    } else {
                        rspPayRechargeBank.setBankChargeLimit( bankChargeLimit );
                    }
                }
                if ( rspPayRechargeBank.getBankAddress() == null ) {
                    rspPayRechargeBank.setBankAddress( "" );
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
        rspWithdrawBank.setRspWithdrawInfo( memberWithdrawDetailService.getRspWithdrawDetail( memberInfo ) );
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
        reqMemberCard.setBankAccount( reqMemberCard.getBankAccount().trim().replaceAll( " ", "" ) );
        if ( !ValidatorUtil.isAccount( reqMemberCard.getBankAccount() ) ) {
            return RspBase.businessError( "请输入正确的银行卡号" );
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
        boolean          dfault     = true;
        List<MemberCard> resultList = memberCardMapper.selectMemberCard( memberId );
        if ( resultList.size() > 0 ) {
            dfault = false;
            if ( !resultList.get( 0 ).getRealName().equals( reqMemberCard.getRealName() ) ) {
                return RspBase.businessError( "姓名必须与首次绑定的一致" );
            }
        }
        if ( resultList.size() >= 10 ) {
            return RspBase.businessError( "提现卡超过10个，请解绑其它提现卡" );
        }
        //加锁
        if ( !redisUtils.lock( "setBindCard" + reqMemberCard.getBankAccount(), 10 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        MemberCard memberCard = new MemberCard();
        memberCard.setBankAccount( reqMemberCard.getBankAccount() );
        memberCard.setBankAddress( reqMemberCard.getBankAddress() );
        memberCard.setCreateTime( LocalDateTime.now() );
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

    @Override
    public List<RspRechargeBankReport> selectReportList( ReqMemberRechargeBank req ) {
        return this.baseMapper.selectReportList( req );
    }

    @Override
    public Map selectReportListCount( ReqMemberRechargeBank req ) {
        return this.baseMapper.selectReportListCount( req );
    }

    @Override
    public List<MemberRechargeBank> selectMemberRechargeBankList( ReqMemberRechargeBank req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        List<MemberRechargeBank> memberRechargeBanks = this.baseMapper.selectMemberRechargeBankList( req );
        for ( MemberRechargeBank me : memberRechargeBanks ) {
            if ( Strings.isNotBlank( me.getRechargeRealName() ) && !me.getRealName().equals( me.getRechargeRealName() ) ) {
                me.setNameStatus( 0 );
            } else {
                me.setNameStatus( 1 );
            }
        }
        return memberRechargeBanks;
    }

    @Override
    public RspBase<?> firstAudit( ReqMemberRechargeBank req, String userName ) {
        MemberRechargeBank memberRechargeBank = this.baseMapper.selectById( req.getId() );
        if ( memberRechargeBank == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( memberRechargeBank.getStatus() != 0 ) {
            return RspBase.businessError( "订单状态有误，请刷新数据后重试" );
        }

        MemberRechargeBank update = new MemberRechargeBank();
        update.setRechargeOrderNo( memberRechargeBank.getRechargeOrderNo() );
        update.setStatus( 1 );//初审通过
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "更改状态失败" );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> finalAudit( ReqMemberRechargeBank req, String userName ) {
        MemberRechargeBank memberRechargeBank = this.baseMapper.selectById( req.getId() );
        if ( memberRechargeBank == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( memberRechargeBank.getStatus() != 1 ) {
            return RspBase.businessError( "订单状态有误，请刷新数据后重试" );
        }
        if ( !redisUtils.lock( "RechargeBankFinalAudit" + req.getId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        if ( memberRechargeBank.getStatus() != 1 ) {
            throw new BusinessException( "订单状态有误" );
        }

        MemberInfo memberInfo = memberInfoMapper.selectById( memberRechargeBank.getMemberId() );

        BigDecimal chargeGive = memberRechargeBank.getDiscountBill().multiply( memberRechargeBank.getRechargeMoney() )
                .setScale( 2, RoundingMode.HALF_UP ); // 充值彩金

        // 单日首次彩金
        BigDecimal ticketCattyRatio = configEnvCacheUtil.getConfBd( "recharge_day_first_rate" );

        int daySucess = this.baseMapper.countRechargeDaySucess( memberInfo.getId() );

        if ( daySucess == 0 ) {
            chargeGive = chargeGive.add( memberRechargeBank.getRechargeMoney().multiply( ticketCattyRatio )
                    .setScale( 2, RoundingMode.HALF_UP ) );
        }
        // 单日第二次彩金
        if ( daySucess == 1 ) {
            //每日公司入款第二次优惠比例
            BigDecimal ticketCattyRatioSnd = configEnvCacheUtil.getConfBd( "recharge_day_second_rate" );
            chargeGive = chargeGive.add( memberRechargeBank.getRechargeMoney().multiply( ticketCattyRatioSnd )
                    .setScale( 2, RoundingMode.HALF_UP ) );
        }

        //套利号无优惠
        if ( memberInfo.getStatus() == 4 ) {
            chargeGive = BigDecimal.ZERO;
        }

        if ( chargeGive.compareTo( BigDecimal.ZERO ) > 0 ) {
            memberMoneyManager.addMemberMoney( memberInfo.getId(), chargeGive, EnumMoney.ACTIVITY, 1, "审核人：" + userName );
        }

        memberMoneyManager.addMemberMoney( memberRechargeBank.getMemberId(), memberRechargeBank.getRechargeMoney(),
                EnumMoney.DEPOSIT, 1,
                "审核人：" + userName );

        MemberRechargeBank update = new MemberRechargeBank();
        update.setRechargeOrderNo( memberRechargeBank.getRechargeOrderNo() );
        update.setRemark( req.getRemark() );
        update.setStatus( 3 );//终审通过
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );

        //新增佣金记录
        recommendService.recommendProcess( memberInfo, memberRechargeBank.getRechargeMoney() );

        redisUtils.unLock( "RechargeBankFinalAudit" + req.getId() );
        int i = this.baseMapper.updateById( update );
        if ( i <= 0 ) {
            throw new BusinessException( "审核失败" );
        }
        return RspBase.ok( "审核成功" );
    }

    @Override
    public RspBase<?> refusedAudit( ReqMemberRechargeBank req, String userName ) {
        MemberRechargeBank memberRechargeBank = this.baseMapper.selectById( req.getId() );
        if ( memberRechargeBank == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( !redisUtils.lock( "RechargeBankRefusedAudit" + req.getId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        if ( memberRechargeBank.getStatus() == 3 ) {
            return RspBase.businessError( "该订单已审核通过,请刷新页面" );
        }

        MemberRechargeBank update = new MemberRechargeBank();
        update.setRechargeOrderNo( memberRechargeBank.getRechargeOrderNo() );
        update.setRemark( req.getRemark() );
        update.setStatus( 2 );//审核不通过
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        redisUtils.unLock( "RechargeBankRefusedAudit" + req.getId() );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "更改状态失败" );
    }

    @Override
    public RspBase<?> recoverAudit( ReqMemberRechargeBank req, String userName ) {
        MemberRechargeBank memberRechargeBank = this.baseMapper.selectById( req.getId() );
        if ( memberRechargeBank == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( 3 == memberRechargeBank.getStatus() ) {
            return RspBase.businessError( "订单已终审" );
        }

        if ( memberRechargeBank.getStatus() != 2 && memberRechargeBank.getStatus() != 4 ) {
            return RspBase.businessError( "只有拒绝和超时才能恢复审核" );
        }

        MemberRechargeBank update = new MemberRechargeBank();
        update.setRechargeOrderNo( memberRechargeBank.getRechargeOrderNo() );
        update.setStatus( 1 );
        update.setOpName( userName );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "更改状态失败" );
    }

    @Override
    public RspBase<?> bankCardRecharge( String memberId, ReqMemberCardRecharge req ) {
        if ( !ValidatorUtil.isChinese( req.getRechargeUserName() ) ) {
            return RspBase.businessError( "请输入真实的绑定银行卡姓名" );
        }
        MemberInfo memberInfo = memberInfoMapper.selectById( memberId );
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( memberInfo.getStatus() == 0 ) {
            return RspBase.businessError( "账号异常，请联系客服" );
        }
        PayRechargeBank payRechargeBank = payRechargeBankMapper.selectById( req.getBankBaseId() );
        if ( payRechargeBank == null ) {
            return RspBase.businessError( "充值银行卡不存在" );
        }
        if ( !payRechargeBank.getEffect() ) {
            return RspBase.businessError( "入款银行卡已停用,如有疑问请联系客服" );
        }

        return null;
    }
}

