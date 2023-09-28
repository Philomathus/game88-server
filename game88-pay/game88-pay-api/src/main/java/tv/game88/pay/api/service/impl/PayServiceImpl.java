package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.manager.MemberRecommendManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.manager.MemberQuestManager;
import tv.game88.core.quest.mapper.ActivityQuestInfoMapper;
import tv.game88.pay.api.base.BasePay;
import tv.game88.pay.api.base.PayProcessorFactoryUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.dto.RspPayChannel;
import tv.game88.pay.api.entity.*;
import tv.game88.pay.api.mapper.*;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class PayServiceImpl implements PayService {
    @Resource
    private MemberMoneyManager                  memberMoneyManager;
    @Resource
    private MemberRechargeOnlineMapper          memberRechargeOnlineMapper;
    @Resource
    private PayChannelMapper                    payChannelMapper;
    @Resource
    private PayChannelMoneyMapper               payChannelMoneyMapper;
    @Resource
    private MemberInfoMapper                    memberInfoMapper;
    @Resource
    private PayLogMapper                        payLogMapper;
    @Resource
    private ActivityCashBackFirstRechargeMapper cashBackFirstRechargeMapper;
    @Resource
    private MemberRecommendManager              memberRecommendManager;
    @Resource
    private MemberQuestManager                  memberQuestManager;
    @Resource
    private ActivityQuestInfoMapper             questInfoMapper;

    @Resource
    private PayProcessorFactoryUtil payProcessorFactoryUtil;
    @Resource
    private RedisUtils              redisUtil;
    @Resource
    private ConfigEnvCacheUtil      configEnvCacheUtil;
    @Resource
    private PayCacheUtil            payCacheUtil;

    @Override
    public List<PayType> findPayTypeList( PlatformUser platformUser, String deviceType ) {
        List<PayType> payTypeList = payCacheUtil.getPayTypeList();
        if ( !CollectionUtils.isEmpty( payTypeList ) ) {
            //移除 类型层级比会员vip层级大 的类型
            payTypeList.removeIf( payType -> payType.getOpenLevelMin() != null && payType.getOpenLevelMax() != null
                    && platformUser.getVip() != null && ( platformUser.getVip() < payType.getOpenLevelMin()
                    || platformUser.getVip() > payType.getOpenLevelMax() ) );
            payTypeList.removeIf( payType -> payType.getType() == 2 && platformUser.getStatus() == 6 );
            payTypeList.removeIf( payType -> "1".equals( deviceType ) && StringUtils.isNotBlank( payType.getDeviceType() )
                    && !payType
                    .getDeviceType()
                    .contains( "1" ) ); //移除ios外的支付类型
            payTypeList.removeIf( payType -> "2".equals( deviceType ) && StringUtils.isNotBlank( payType.getDeviceType() )
                    && !payType
                    .getDeviceType()
                    .contains( "2" ) ); //移除安卓外的支付类型
            String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
            for ( PayType payType : payTypeList ) {
                if ( StringUtils.isNotBlank( payType.getIconUrl() ) && !payType
                        .getIconUrl()
                        .startsWith( "http" ) ) {
                    payType.setIconUrl( domainValue + payType.getIconUrl() );
                }
            }
        }
        return payTypeList;
    }

    //测试号测试通道用
    @Override
    public List<RspPayChannel> findPayChannelList( Long typeId, PlatformUser platformUser ) {
        List<RspPayChannel> payChannelList = payChannelMapper.selectRspListByTypeId( typeId );
        //移除 通道层级比会员vip层级大 的通道
        payChannelList.removeIf( rspPayChannel -> {
            Integer vip = platformUser.getVip();
            if ( vip != null ) {
                Integer openLevel    = rspPayChannel.getOpenLevelMin();
                Integer openLevelMax = rspPayChannel.getOpenLevelMax();
                return ( openLevel != null && vip < openLevel ) || ( openLevelMax != null && vip > openLevelMax );
            }
            return false;
        } );
        return payChannelList;
    }

    @Override
    public List<RspPayChannel> findPayChannel( Long typeId, PlatformUser platformUser ) {
        List<Long> moneyList = payChannelMoneyMapper.selectMoney( typeId, platformUser.getVip() );
        if ( CollectionUtils.isEmpty( moneyList ) ) {
            return Collections.emptyList();
        }
        RspPayChannel rspPayChannel = new RspPayChannel();
        rspPayChannel.setQuickAmount( StringUtils.join( moneyList, "," ) );
        rspPayChannel.setId( typeId );
        rspPayChannel.setName( "固定金额" );
        rspPayChannel.setRechargeMin( new BigDecimal( moneyList.get( 0 ) ) );
        rspPayChannel.setRechargeMax( new BigDecimal( moneyList.get( moneyList.size() - 1 ) ) );
        rspPayChannel.setOpenLevelMin( 0 );
        rspPayChannel.setOpenLevelMax( 0 );
        return Collections.singletonList( rspPayChannel );
    }

    @Override
    public String updatePayJourStatus( MemberRechargeOnline memberRechargeOnline, String[] notifyResultWays, String mark ) {
        // 下单金额与实际金额不符拒绝回调
        if ( memberRechargeOnline.getRealMoney() == null ) {
            log.warn( "请注意保存实际金额!!!" );
            return notifyResultWays[ 1 ];
        }
        if ( memberRechargeOnline.getRealMoney() != null && memberRechargeOnline
                .getRealMoney()
                .compareTo( memberRechargeOnline.getMoney() ) != 0 ) {
            // 35是新火箭支付平台ID，0.85是扣除15%费率后的值，判断下单金额扣除15%费率后是否与实际金额相等，不相等拒绝回调
            if ( memberRechargeOnline.getPlatformId() == -1 ) {
                if ( memberRechargeOnline
                        .getMoney()
                        .multiply( new BigDecimal( "0.85" ) )
                        .compareTo( memberRechargeOnline.getRealMoney() ) != 0 ) {
                    log.warn( "下单金额与实际金额不符拒绝回调 - orderNo:{};money:{};subMoney:{}", memberRechargeOnline.getOrderNo(),
                            memberRechargeOnline.getMoney(), memberRechargeOnline.getRealMoney() );
                    return notifyResultWays[ 1 ];
                }
            } else {
                log.warn( "下单金额与实际金额不符拒绝回调 - orderNo:{};money:{};subMoney:{}", memberRechargeOnline.getOrderNo(),
                        memberRechargeOnline.getMoney(), memberRechargeOnline.getRealMoney() );
                return notifyResultWays[ 1 ];
            }
        }

        try {
            SpringUtils
                    .getBean( PayService.class )
                    .updatePayJourStatus( memberRechargeOnline, mark );
            return notifyResultWays[ 0 ];
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            return notifyResultWays[ 1 ];
        }
    }


    @Transactional( rollbackFor = Exception.class )
    public void updatePayJourStatus( MemberRechargeOnline memberRechargeOnline, String mark ) {
        //更新支付订单状态
        MemberRechargeOnline updatePayJour = new MemberRechargeOnline();
        updatePayJour.setOrderNo( memberRechargeOnline.getOrderNo() );
        updatePayJour.setStatus( 1 );
        updatePayJour.setUpdateTime( LocalDateTime.now() );
        updatePayJour.setRealMoney( memberRechargeOnline.getRealMoney() );
        updatePayJour.setUpperOrderNo( memberRechargeOnline.getUpperOrderNo() );
        updatePayJour.setPatchOrder( memberRechargeOnline.getPatchOrder() );
        updatePayJour.setRemark( memberRechargeOnline.getRemark() );
        if ( updatePayJour.getMoney() != null ) {
            updatePayJour.setMoney( memberRechargeOnline.getMoney() );
        }
        MemberInfo memberInfo = memberInfoMapper.selectById( memberRechargeOnline.getMemberId() );
        if ( memberInfo
                .getAccountCharge()
                .compareTo( BigDecimal.ZERO ) == 0 ) {
            updatePayJour.setFirst( true );
        }

        BigDecimal payJourMoney = BooleanUtils.isTrue( memberRechargeOnline.getPatchOrder() ) ?
                memberRechargeOnline.getRealMoney() : memberRechargeOnline.getMoney();

        //支付通道优惠比例
        BigDecimal chargeGive = null;

        String firstRechargeRate = configEnvCacheUtil.getConf( "pay_first_recharge_rate" );
        String nextRechargeRate  = configEnvCacheUtil.getConf( "pay_next_recharge_rate" );

        if ( StringUtils.isNotBlank( firstRechargeRate ) && StringUtils.isNotBlank( nextRechargeRate ) ) {
            String[] payFirstPlatformRates = firstRechargeRate.split( ";" );

            String[] payNextPlatformRates = nextRechargeRate.split( ";" );

            for ( String payPlatformRate : payFirstPlatformRates ) {
                String[] firstPaySplit = payPlatformRate.split( "," );
                if ( memberRechargeOnline
                        .getPlatformId()
                        .toString()
                        .equals( firstPaySplit[ 0 ] ) &&
                        memberRechargeOnlineMapper.successTodayCount( memberInfo.getId(), memberRechargeOnline.getPlatformId() )
                                == 0 ) {
                    BigDecimal firstRate = new BigDecimal( firstPaySplit[ 1 ] );
                    chargeGive = payJourMoney
                            .multiply( firstRate )
                            .setScale( 2, RoundingMode.HALF_UP );
                    log.warn( "首冲 {},{},{}", chargeGive, memberRechargeOnline.getPlatformId(), memberInfo.getId() );
                    break;
                }
            }
            if ( chargeGive == null ) {
                for ( String payPlatformRate : payNextPlatformRates ) {
                    String[] firstPaySplit = payPlatformRate.split( "," );
                    if ( memberRechargeOnline
                            .getPlatformId()
                            .toString()
                            .equals( firstPaySplit[ 0 ] ) &&
                            memberRechargeOnlineMapper.successTodayCount( memberInfo.getId(),
                                    memberRechargeOnline.getPlatformId() )
                                    > 0 ) {
                        BigDecimal firstRate = new BigDecimal( firstPaySplit[ 1 ] );
                        chargeGive = payJourMoney
                                .multiply( firstRate )
                                .setScale( 2, RoundingMode.HALF_UP );
                        log.warn( "每笔 {},{},{}", chargeGive, memberRechargeOnline.getPlatformId(), memberInfo.getId() );
                        break;
                    }
                }
            }
        }

        if ( chargeGive == null ) {
            PayChannel payChannel = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
            if ( Objects.equals( memberRechargeOnline.getPlatformId(), ConstantsPay.VIPPAY_PAY_PLATFORM_ID ) ) { // 24 是vipPay

                String newVipPayRate = configEnvCacheUtil.getConf( "new_vippay_rate" );

                if ( StringUtils.isNotBlank( newVipPayRate ) ) {
                    String[] newVipPayRates = newVipPayRate.split( ";" );
                    for ( String rates : newVipPayRates ) {
                        String[]   spit   = rates.split( "," );
                        BigDecimal amount = new BigDecimal( spit[ 0 ] );
                        if ( payJourMoney.compareTo( amount ) >= 0 ) {
                            chargeGive = payJourMoney
                                    .multiply( new BigDecimal( spit[ 1 ] ) )
                                    .setScale( 2, RoundingMode.HALF_UP );
                        }

                    }
                } else {
                    chargeGive = configEnvCacheUtil
                            .getConfBd( "vippay_rate" )
                            .multiply( payJourMoney )
                            .setScale( 2, RoundingMode.HALF_UP );
                }

            } else if ( payChannel != null && StringUtils.isNotBlank( payChannel.getDiscountBill() ) ) {
                chargeGive = new BigDecimal( payChannel.getDiscountBill() )
                        .multiply( payJourMoney )
                        .setScale( 2, RoundingMode.HALF_UP );
            } else {
                chargeGive = new BigDecimal( 0 );
            }
        }

        //套利号无优惠
        if ( memberInfo.getStatus() == 4 ) {
            chargeGive = new BigDecimal( 0 );
        }

        BigDecimal firstRechargeCashBack = BigDecimal.ZERO; // 首冲赠送彩金
        if ( BooleanUtils.isTrue( updatePayJour.getFirst() )
                && configEnvCacheUtil.getConfBool( "is_first_recharge_cash_back" ) ) {
            BigDecimal rebate = cashBackFirstRechargeMapper.selectByRechargeMoney( payJourMoney );
            if ( rebate != null && rebate.compareTo( BigDecimal.ZERO ) > 0 ) {
                firstRechargeCashBack = rebate;
            }
        }
        chargeGive = chargeGive.add( firstRechargeCashBack );

        if ( chargeGive.compareTo( BigDecimal.ZERO ) > 0 ) {
            //充值彩金日志
            memberMoneyManager.addMemberMoney( memberInfo.getId(), chargeGive, EnumMoney.DEPOSIT_BONUS, BigDecimal.ONE, mark,
                    null, memberRechargeOnline.getOrderNo() );
        }

        memberMoneyManager.addMemberMoney( memberInfo.getId(), payJourMoney, EnumMoney.PAY, BigDecimal.ONE,
                mark + "-充值:" + payJourMoney, memberRechargeOnline.getOrderNo(), memberRechargeOnline.getOrderNo() );
        //新增佣金记录
        memberRecommendManager.recommendProcess( memberInfo, memberRechargeOnline.getMoney() );

        if ( Objects.equals( memberRechargeOnline.getPlatformId(), ConstantsPay.VIPPAY_PAY_PLATFORM_ID ) ) {
            //vipPay充值活动任务
            List<ActivityQuestInfo> listConfQuest = questInfoMapper.selectList( new QueryWrapper<ActivityQuestInfo>()
                    .eq( "effect", 1 )
                    .eq( "game_type_id", -2 ) );
            for ( ActivityQuestInfo confQuest : listConfQuest ) {
                memberQuestManager.memberQuestProcess( memberInfo.getId(), payJourMoney, confQuest );
            }
        }

        memberRechargeOnlineMapper.updateById( updatePayJour );

        log.warn( "会员线上充值上分成功 - orderNo:{}", memberRechargeOnline.getOrderNo() );
    }

    @Override
    public String payRedirect( String orderNo ) {
        QueryWrapper<MemberRechargeOnline> queryWrapper = new QueryWrapper<>();
        queryWrapper.select( "payment_address", "order_no" );
        queryWrapper.eq( "order_no", orderNo );
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectOne( queryWrapper );
        if ( memberRechargeOnline == null || !StringUtils.isNotBlank( memberRechargeOnline.getPaymentAddress() ) ) {
            return null;
        }
        return memberRechargeOnline.getPaymentAddress();
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void payQuery10Min() throws Exception {
        QueryWrapper<MemberRechargeOnline> queryWrapper = new QueryWrapper<MemberRechargeOnline>()
                .eq( "status", -1 )
                .le( "pay_time", LocalDateTimeUtils.format( LocalDateTime
                        .now()
                        .minusMinutes( 10 ) ) );
        List<MemberRechargeOnline> memberRechargeOnlineList = memberRechargeOnlineMapper.selectList( queryWrapper );
        for ( MemberRechargeOnline memberRechargeOnline : memberRechargeOnlineList ) {
            MemberRechargeOnline update = new MemberRechargeOnline();
            update.setStatus( 0 );
            UpdateWrapper<MemberRechargeOnline> payJourUpdateWrapper = new UpdateWrapper<>();
            payJourUpdateWrapper
                    .eq( "order_no", memberRechargeOnline.getOrderNo() )
                    .eq( "status", -1 );
            memberRechargeOnlineMapper.update( update, payJourUpdateWrapper );
        }
    }

    @Override
    public RspBase<?> payRecharge( ReqPayRecharge reqPayRecharge, PlatformUser platformUser ) throws Exception {
        BigDecimal money = reqPayRecharge.getMoney();
        if ( money == null || money.compareTo( BigDecimal.ZERO ) <= 0 ) {
            return RspBase.businessError( "请选择金额" );
        }
        // 加3秒锁
        if ( !redisUtil.lock( "payRecharge" + platformUser.getId(), 3 ) ) {
            log.warn( "请勿重复提交{}", platformUser.getId() );
            return RspBase.businessError( "请勿重复提交" );
        }
        Long payIntervalExpire = redisUtil.getExpire( "pay:interval:" + platformUser.getId() );
        if ( payIntervalExpire > 0 ) {
            log.warn( "请求订单过于频繁哦{}", platformUser.getId() );
            return RspBase.businessError( String.format( "请求订单过于频繁哦，请%s秒后再试", payIntervalExpire ) );
        }
        long payOrderNum = memberRechargeOnlineMapper.selectCount( new QueryWrapper<MemberRechargeOnline>()
                .eq( "member_id", platformUser.getId() )
                .le( "status", 0 )
                .ge( "pay_time", LocalDateTimeUtils.format( LocalDateTime
                        .now()
                        .minusMinutes( 10 ) ) ) );
        if ( payOrderNum >= configEnvCacheUtil.getConfInt( "pay_order_num_5min", 10 ) ) {
            log.warn( "您请求订单次数过多{}", platformUser.getId() );
            return RspBase.businessError( "您请求订单次数过多，请稍后重试" );
        }

        if ( reqPayRecharge.getChannelId() < 0 ) {
            //把code转化成通道id
            this.payNewLogicRecharge( reqPayRecharge, platformUser );
        }

        PayChannel payChannel = payCacheUtil.getPayChannel( reqPayRecharge.getChannelId() );
        if ( payChannel == null ) {
            return RspBase.businessError( "充值调整,请退出并重新进入充值界面" );
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( payChannel.getPlatformId() );
        PayType     payType     = payCacheUtil.getPayType( payChannel.getTypeId() );


        reqPayRecharge.setOrderNo( GenerateOrderCacheUtils.me.getOrderId( "P", 2 ) );
        reqPayRecharge.setUserId( platformUser.getId() );


        BasePay basePay     = payProcessorFactoryUtil.createPayProcessor( payPlatform.getCode() );
        String  paymentCode = basePay.orderPay( payChannel, payPlatform, reqPayRecharge );


        // 释放锁
        redisUtil.unLock( "payRecharge" + platformUser.getId() );

        if ( StringUtils.isNotBlank( paymentCode ) ) {
            reqPayRecharge.setName( payPlatform.getName() + "-" + payType.getName() );

            this.savePayRecharge( platformUser, paymentCode, reqPayRecharge, payChannel );
            redisUtil.strSet( "pay:interval:"
                    + platformUser.getId(), "0", Duration.ofSeconds( configEnvCacheUtil.getConfInt( "pay_interval_sec" ) ) );

            insertPayLog( reqPayRecharge, platformUser, payChannel, payPlatform, paymentCode );
            if ( Objects.equals( reqPayRecharge.getUrlType(), 1 ) ) {
                return RspBase.ok( "获取充值连接成功",
                        configEnvCacheUtil.getConf( "payRedirectUrl" ) + reqPayRecharge.getOrderNo() );
            } else {
                return RspBase.ok( "获取充值连接成功", paymentCode );
            }
        } else {
            if ( StringUtils.isNotBlank( reqPayRecharge.getFailReason() ) ) {
                if ( reqPayRecharge
                        .getFailReason()
                        .startsWith( "I/O error on POST request for" ) ) {
                    //超时的再下单一次
                    reqPayRecharge.setFailReason( "网络连接失败,下单超时" );
                } else if ( reqPayRecharge
                        .getFailReason()
                        .startsWith( "403 Forbidden" ) ) {
                    reqPayRecharge.setFailReason( "支付IP未加白名单,请发给三方加白" );
                } else if ( reqPayRecharge
                        .getFailReason()
                        .length() > 255 ) {
                    reqPayRecharge.setFailReason( "网络连接失败,下单报错" );
                }
            } else {
                reqPayRecharge.setFailReason( "网络连接失败,下单返回空值" );
            }
            insertPayLog( reqPayRecharge, platformUser, payChannel, payPlatform, paymentCode );
            return RspBase.businessError( "充值失败,请重试或更换金额" );
        }
    }

    private void insertPayLog( ReqPayRecharge reqPayRecharge, PlatformUser platformUser, PayChannel payChannel,
                               PayPlatform payPlatform, String paymentCode ) {
        //新增支付日志
        PayLog payLog = new PayLog();
        payLog.setSuccess( StringUtils.isNotBlank( paymentCode ) );
        payLog.setChannelId( payChannel.getId() );
        payLog.setChannelName( payChannel.getName() );
        payLog.setCreateTime( LocalDateTime.now() );
        payLog.setFailReason( reqPayRecharge.getFailReason() );
        payLog.setMemberId( platformUser.getId() );
        payLog.setMoney( reqPayRecharge.getMoney() );
        payLog.setPlatformId( payPlatform.getId() );
        payLog.setPlatformName( payPlatform.getName() );
        payLogMapper.insert( payLog );
    }

    //选择通道算法
    private void payNewLogicRecharge( ReqPayRecharge reqPayRecharge, PlatformUser platformUser ) {
        List<Map<String, Object>> resultList = memberRechargeOnlineMapper.countOrder( platformUser.getId() );
        log.warn( "会员ID ：{}，数量查询 ：{}", platformUser.getId(), JsonUtil.object2Json( resultList ) );
        // 成功数量
        int scount = 0;
        // 失败数量 = 状态失败和状态待确认的数量
        int fcount = 0;
        for ( Map<String, Object> resultMap : resultList ) {
            if ( "1".equals( resultMap
                    .getOrDefault( "status", "" )
                    .toString() ) ) {
                scount += Integer.parseInt( resultMap
                        .getOrDefault( "count", "0" )
                        .toString() );
            } else {
                fcount += Integer.parseInt( resultMap
                        .getOrDefault( "count", "0" )
                        .toString() );
            }
        }
        if ( ( scount + fcount ) == 0 ) { // min
            Integer channelId = payChannelMoneyMapper.minRateChannel( reqPayRecharge.getChannelId(), reqPayRecharge.getMoney(),
                    platformUser.getVip() );
            log.warn( "会员ID ：{}，min ：{}", platformUser.getId(), channelId );
            reqPayRecharge.setChannelId( channelId );
        } else if ( fcount == 1 && scount == 0 ) { // random
            Integer channelId = payChannelMoneyMapper.randomChannelId( reqPayRecharge.getChannelId(), reqPayRecharge.getMoney()
                    , platformUser.getVip() );
            log.warn( "会员ID ：{}，random ：{}", platformUser.getId(), channelId );
            reqPayRecharge.setChannelId( channelId );
        } else if ( fcount >= 2 && scount == 0 ) { // max
            Integer channelId = payChannelMoneyMapper.maxRateChannel( reqPayRecharge.getChannelId(), reqPayRecharge.getMoney(),
                    platformUser.getVip() );
            log.warn( "会员ID ：{}，max ：{}", platformUser.getId(), channelId );
            reqPayRecharge.setChannelId( channelId );
        } else { // random
            Integer channelId = payChannelMoneyMapper.randomChannelId( reqPayRecharge.getChannelId(), reqPayRecharge.getMoney()
                    , platformUser.getVip() );
            log.warn( "会员ID ：{}，random ：{}", platformUser.getId(), channelId );
            reqPayRecharge.setChannelId( channelId );
        }
    }

    public void savePayRecharge( PlatformUser platformUser, String paymentCode, ReqPayRecharge reqPayRecharge,
                                 PayChannel payChannel ) {
        log.info( "获取paymentCode成功，开始保存订单信息，userId:{}", platformUser.getId() );
        MemberRechargeOnline memberRechargeOnline = new MemberRechargeOnline();
        memberRechargeOnline.setOrderNo( reqPayRecharge.getOrderNo() );
        memberRechargeOnline.setMemberId( platformUser.getId() );
        memberRechargeOnline.setPlatformId( payChannel.getPlatformId() );
        memberRechargeOnline.setChannelId( payChannel.getId() );
        if ( StringUtils.isNotBlank( reqPayRecharge.getUpperOrderNo() ) ) {
            memberRechargeOnline.setUpperOrderNo( reqPayRecharge.getUpperOrderNo() );
        }
        memberRechargeOnline.setMoney( reqPayRecharge.getMoney() );
        memberRechargeOnline.setFirst( false );
        memberRechargeOnline.setPaymentAddress( paymentCode );
        memberRechargeOnline.setPayTime( LocalDateTime.now() );
        memberRechargeOnline.setStatus( -1 );
        memberRechargeOnline.setRate( payChannel.getRate() );
        memberRechargeOnline.setUpdateTime( memberRechargeOnline.getPayTime() );
        if ( StringUtils.isNotBlank( reqPayRecharge.getTicket() ) ) {
            memberRechargeOnline.setRemark( reqPayRecharge.getTicket() );
        }
        memberRechargeOnlineMapper.insert( memberRechargeOnline );
    }

}

