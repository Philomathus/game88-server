package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.service.RecommendService;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.entity.MemberRechargeUsdt;
import tv.game88.pay.api.mapper.ActivityCashBackFirstRechargeMapper;
import tv.game88.pay.api.mapper.MemberRechargeUsdtMapper;
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
    private RecommendService                    recommendService;
    @Resource
    private MemberMoneyManager                  memberMoneyManager;
    @Resource
    private MemberInfoMapper                    memberInfoMapper;
    @Resource
    private ActivityCashBackFirstRechargeMapper cashBackFirstRechargeMapper;
    @Resource
    private RedisUtils                          redisUtils;
    @Resource
    private ConfigEnvCacheUtil                  configEnvCacheUtil;


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
        //充值彩金日志
        memberMoneyManager.addMemberMoney( memberInfo.getId(), chargeGive, EnumMoney.ACTIVITY, 1, update.getRemark() );
        //usdt充值日志
        memberMoneyManager.addMemberMoney( memberInfo.getId(), rechargeMoney, EnumMoney.USDT, 1, update.getRemark() );
        //新增佣金记录
        recommendService.recommendProcess( memberInfo, rechargeMoney );
        //更新usdt充值记录表状态
        int i = this.baseMapper.updateById( update );
        if ( i < 0 ) {
            throw new BusinessException( "更新状态失败" );
        }
    }

    @Override
    public RspBase<?> usdtRecharge( String memberId, ReqMemberRechargeUsdt req ) {
        if ( req.getRechargeNumber() <= 0 ) {
            return RspBase.businessError( "您输入的充值USDT数量不正确" );
        }
        return null;
    }
}

