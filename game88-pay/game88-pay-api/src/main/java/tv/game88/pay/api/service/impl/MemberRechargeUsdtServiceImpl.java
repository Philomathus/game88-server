package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.manager.MemberRecommendManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.utils.TelegramBotMessage;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.entity.MemberRechargeUsdt;
import tv.game88.pay.api.entity.PayRechargeUsdt;
import tv.game88.pay.api.mapper.ActivityCashBackFirstRechargeMapper;
import tv.game88.pay.api.mapper.MemberRechargeUsdtMapper;
import tv.game88.pay.api.mapper.PayRechargeUsdtMapper;
import tv.game88.pay.api.service.MemberRechargeUsdtService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MemberRechargeUsdtServiceImpl extends ServiceImpl<MemberRechargeUsdtMapper, MemberRechargeUsdt> implements MemberRechargeUsdtService {
    @Resource
    private MemberRecommendManager              memberRecommendManager;
    @Resource
    private MemberMoneyManager                  memberMoneyManager;
    @Resource
    private MemberInfoMapper                    memberInfoMapper;
    @Resource
    private PayRechargeUsdtMapper               payRechargeUsdtMapper;
    @Resource
    private ActivityCashBackFirstRechargeMapper cashBackFirstRechargeMapper;
    @Resource
    private RedisUtils                          redisUtils;
    @Resource
    private ConfigEnvCacheUtil                  configEnvCacheUtil;
    @Resource
    private TelegramBotMessage                  telegramBotMessage;


    @Override
    public List<MemberRechargeUsdt> selectMemberRechargeUsdtList( ReqMemberRechargeUsdt req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.selectMemberRechargeUsdtList( req );
    }

    @Override
    public RspBase<Map> listCount( ReqMemberRechargeUsdt req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        return RspBase.ok( this.baseMapper.listCount( req ) );
    }

    /**
     * 锁定USDT充值提交记录
     *
     * @param orderNo
     *
     * @return 结果
     */
    @Override
    public RspBase<?> lock( String orderNo, String userName ) {
        MemberRechargeUsdt update = new MemberRechargeUsdt();
        update.setRechargeOrderNo( orderNo );
        update.setOpName( userName );
        update.setRemark( "锁定人:" + userName );
        update.setStatus( 0 );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "锁定失败" );
    }

    /**
     * 解锁USDT充值提交记录
     *
     * @param orderNo
     *
     * @return 结果
     */
    @Override
    public RspBase<?> unLock( String orderNo, String userName, boolean contains ) {
        MemberRechargeUsdt memberRechargeUsdt = this.baseMapper.selectById( orderNo );
        if ( !contains ) {
            if ( StringUtils.hasText( memberRechargeUsdt.getOpName() ) && !userName.equals( memberRechargeUsdt.getOpName() ) ) {
                return RspBase.businessError( "该订单只能由" + memberRechargeUsdt.getOpName() + "处理" );
            }
        }
        MemberRechargeUsdt update = new MemberRechargeUsdt();
        update.setRechargeOrderNo( orderNo );
        update.setOpName( userName );
        update.setRemark( "解锁人:" + userName );
        update.setStatus( 1 );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "解锁失败" );
    }

    /**
     * 拒绝USDT充值提交记录
     *
     * @param orderNo
     *
     * @return 结果
     */
    @Override
    public RspBase<?> refused( String orderNo, String userName ) {
        MemberRechargeUsdt update = new MemberRechargeUsdt();
        update.setRechargeOrderNo( orderNo );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        update.setStatus( 2 );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "拒绝失败" );
    }

    /**
     * 通过USDT充值提交记录
     *
     * @param req USDT充值提交记录
     *
     * @return 结果
     */
    @Override
    public RspBase<?> updateMemberRechargeUsdt( MemberRechargeUsdt req, String userName ) {

        MemberRechargeUsdt memberRechargeUsdt = this.baseMapper.selectById( req.getRechargeOrderNo() );
        if ( memberRechargeUsdt == null ) {
            return RspBase.businessError( "该充值记录不存在" );
        }
        if ( memberRechargeUsdt.getStatus() != 0 ) {
            return RspBase.businessError( "该充值记录状态有误，请刷新数据后重试" );
        }
        if ( !redisUtils.lock( "RechargeUsdt" + req.getRechargeOrderNo(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        MemberRechargeUsdt update = new MemberRechargeUsdt();
        update.setRechargeOrderNo( req.getRechargeOrderNo() );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        update.setRemark( req.getRemark() );
        update.setStatus( 3 );

        try {
            MemberInfo                memberInfo = memberInfoMapper.selectById( memberRechargeUsdt.getMemberId() );
            MemberRechargeUsdtService service    = SpringUtils.getBean( MemberRechargeUsdtService.class );
            service.updateMemberRechargeUsdtLogic( memberInfo, update, memberRechargeUsdt.getRechargeMoney(),
                    memberRechargeUsdt.getDiscountBill() );
            return RspBase.ok( "审核成功" );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            return RspBase.businessError( "审核异常" );
        } finally {
            redisUtils.unLock( "RechargeUsdt" + req.getRechargeOrderNo() );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    public void updateMemberRechargeUsdtLogic( MemberInfo memberInfo, MemberRechargeUsdt update, BigDecimal rechargeMoney,
                                               BigDecimal discountBill ) {
        if ( discountBill == null ) {
            discountBill = BigDecimal.ZERO;
        }

        BigDecimal chargeGive = discountBill.multiply( rechargeMoney ).setScale( 2, RoundingMode.HALF_UP ); // 充值彩金

        update.setFirst( memberInfo.getAccountCharge().compareTo( BigDecimal.ZERO ) == 0 );

        BigDecimal firstRechargeCashBack = BigDecimal.ZERO; // 首冲赠送彩金
        if ( update.getFirst() && configEnvCacheUtil.getConfBool( "is_first_recharge_cash_back" ) ) {
            BigDecimal rebate = cashBackFirstRechargeMapper.selectByRechargeMoney( rechargeMoney );
            if ( rebate != null && rebate.compareTo( BigDecimal.ZERO ) > 0 ) {
                firstRechargeCashBack = rebate;
            }
        }

        chargeGive = chargeGive.add( firstRechargeCashBack );

        //套利号无优惠
        if ( memberInfo.getStatus() == 4 ) {
            chargeGive = BigDecimal.ZERO;
        }
        if ( chargeGive.compareTo( BigDecimal.ZERO ) > 0 ) {
            //充值彩金日志
            memberMoneyManager.addMemberMoney( memberInfo.getId(), chargeGive, EnumMoney.DEPOSIT_BONUS, BigDecimal.ONE,
                    update.getRemark(), null, update.getRechargeOrderNo() );
        }
        //usdt充值日志
        memberMoneyManager.addMemberMoney( memberInfo.getId(), rechargeMoney, EnumMoney.USDT, BigDecimal.ONE,
                update.getRemark(), update.getRechargeOrderNo(), update.getRechargeOrderNo() );
        //新增佣金记录
        memberRecommendManager.recommendProcess( memberInfo, rechargeMoney );
        //更新usdt充值记录表状态
        int i = this.baseMapper.updateById( update );
        if ( i < 0 ) {
            throw new BusinessException( "更新状态失败" );
        }
    }

    @Override
    public RspBase<?> usdtRecharge( PlatformUser platformUser, ReqMemberRechargeUsdt req ) {
        if ( platformUser.getStatus() == 0 ) {
            return RspBase.businessError( "账号异常，请联系客服" );
        }
        if ( req.getRechargeNumber() <= 0 ) {
            return RspBase.businessError( "您输入的充值USDT数量不正确" );
        }
        PayRechargeUsdt payRechargeUsdt = payRechargeUsdtMapper.selectById( req.getId() );
        if ( payRechargeUsdt == null ) {
            return RspBase.businessError( "该USDT充值渠道不存在" );
        }
        if ( !payRechargeUsdt.getEffect() ) {
            return RspBase.businessError( "该USDT渠道已停用,如有疑问请联系客服" );
        }
        if ( !redisUtils.lock( "usdtRecharge" + platformUser.getId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        long count1 = this.baseMapper.selectCount( new QueryWrapper<MemberRechargeUsdt>().eq( "transaction_id",
                req.getTransactionId() ) );
        if ( count1 > 0 ) {
            return RspBase.businessError( "该交易ID已存在,如不是本人提交请联系客服" );
        }
        Long count2 = this.baseMapper.selectCount( new QueryWrapper<MemberRechargeUsdt>().eq( "member_id", platformUser.getId() )
                                                                                         .ne( "status", 3 )
                                                                                         .between( "create_time",
                                                                                                 LocalDateTimeUtils.getStartOfToday(), LocalDateTimeUtils.getEndOfToday() ) );
        if ( count2 >= 10 ) {
            return RspBase.businessError( "您的当日已提交未处理订单已有10单,请联系客服处理后再提交" );
        }
        MemberRechargeUsdt memberRechargeUsdt = new MemberRechargeUsdt();
        memberRechargeUsdt.setRechargeOrderNo( GenerateOrderCacheUtils.me.getOrderId( "CZU", 3 ) );
        memberRechargeUsdt.setMemberId( platformUser.getId() );
        memberRechargeUsdt.setTransactionId( req.getTransactionId() );
        memberRechargeUsdt.setRechargeNumber( req.getRechargeNumber() );
        memberRechargeUsdt.setRechargeMoney( new BigDecimal( req.getRechargeNumber() ).multiply( payRechargeUsdt.getExchangeRate() ) );
        memberRechargeUsdt.setStatus( 1 );
        memberRechargeUsdt.setDiscountBill( payRechargeUsdt.getDiscountBill() );
        memberRechargeUsdt.setChainName( payRechargeUsdt.getChainName() );
        memberRechargeUsdt.setRechargeAddress( payRechargeUsdt.getRechargeAddress() );
        memberRechargeUsdt.setChannelName( payRechargeUsdt.getChannelName() );
        memberRechargeUsdt.setCreateTime( LocalDateTime.now() );
        memberRechargeUsdt.setUpdateTime( memberRechargeUsdt.getUpdateTime() );
        BigDecimal accountCharge = memberInfoMapper.getUserCharge( platformUser.getId() );
        memberRechargeUsdt.setFirst( accountCharge.compareTo( BigDecimal.ZERO ) == 0 );
        int i = this.baseMapper.insert( memberRechargeUsdt );
        if ( i > 0 ) {
            // TODO send message to telegram ; ID: recharge_log_telegram ; message: 您有新的USDT充值订单,金额:{},请及时处理!
            telegramBotMessage.sendByChatId( String.format( "您有新的USDT充值订单,金额:%s,请及时处理!",
                    memberRechargeUsdt.getRechargeMoney() ), "recharge_log_telegram" );
            return RspBase.ok( "USDT充值订单提交成功" );
        }
        return RspBase.businessError( "USDT充值订单提交失败" );
    }
}

