package tv.game88.pay.admin.task;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.pay.api.dto.MemberSumRecharge;
import tv.game88.pay.api.entity.ActivityCashBack;
import tv.game88.pay.api.mapper.ActivityCashBackMapper;
import tv.game88.pay.api.mapper.MemberRechargeBankMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 充值返现活动
 */
@Log4j2
@Component
public class ActivityCashBackTask {
    @Resource
    private MemberRechargeBankMapper memberRechargeBankMapper;
    @Resource
    private ActivityCashBackMapper   activityCashBackMapper;
    @Resource
    private LogMoneyMapper           logMoneyMapper;
    @Resource
    private MemberMoneyManager       memberMoneyManager;
    @Resource
    private ConfigEnvCacheUtil       configEnvCacheUtil;
    @Resource
    private RedisUtils               redisUtils;

    @Scheduled( cron = "0 58 15 * * ?" )// 每天15:58点执行一次
    @Scheduled( cron = "0 58 16 * * ?" )// 每天16:58点执行一次
    public void cashBackTask() {
        int cashBackSwitch = configEnvCacheUtil.getConfInt( "cash_back_switch" );
        if ( cashBackSwitch <= 0 ) {
            return;
        }
        if ( !redisUtils.lock( "ActivityCashBackTask", 60 ) ) {
            return;
        }
        log.info( "开始执行充值返现活动任务" );

        //查询昨天公司入款金额
        List<MemberSumRecharge> memberRechargeLogs;
        if ( cashBackSwitch == 1 ) {
            memberRechargeLogs = memberRechargeBankMapper.bankRechargeSum();
        } else {
            memberRechargeLogs = memberRechargeBankMapper.allRechargeSum();
        }
        log.warn( "执行充值返现活动任务 - 昨日充值会员:{}", JsonUtil.object2Json( memberRechargeLogs ) );

        ActivityCashBack query = new ActivityCashBack();
        query.setStatus( "1" );
        List<ActivityCashBack> activityCashBackList = activityCashBackMapper.selectActivityCashBackList( query );

        for ( MemberSumRecharge sumRecharge : memberRechargeLogs ) {
            //要返现金额
            Long bycash = null;
            for ( ActivityCashBack activityCashBack : activityCashBackList ) {
                if ( new BigDecimal( activityCashBack.getDepositTotalMin() ).compareTo( sumRecharge.getMoney() ) <= 0
                        && new BigDecimal( activityCashBack.getDepositTotalMax() ).compareTo( sumRecharge.getMoney() ) > 0 ) {
                    bycash = activityCashBack.getRebate();
                }
            }
            if ( bycash == null ) {
                log.warn( "执行充值返现活动任务 - 未达到充值标准的会员:{}, 金额:{}", sumRecharge.getMemberId(), sumRecharge.getMoney() );
                continue;
            }
            String orderId = "CZFX" + LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDD_FORMATTER )
                    + sumRecharge.getMemberId();
            String dbNodes = sumRecharge.getMemberId().substring( sumRecharge.getMemberId().length() - 1 );
            if ( logMoneyMapper.findExist( dbNodes, orderId ) != null ) {
                log.error( "执行充值返现活动任务 - 存在充值记录的会员:{}, 金额:{}", sumRecharge.getMemberId(), sumRecharge.getMoney() );
                continue;
            }
            //会员返现
            try {
                memberMoneyManager.addMemberMoney( sumRecharge.getMemberId(), new BigDecimal( bycash ),
                        EnumMoney.DEPOSIT_CASHBACK, BigDecimal.ONE, "充值返现活动", orderId, null );
            } catch ( Exception e ) {
                log.error( sumRecharge.getMemberId() + "数据插入失败" + e.getMessage(), e );
                log.error( "执行充值返现活动任务 - 充值失败的会员:{}, 金额:{}", sumRecharge.getMemberId(), sumRecharge.getMoney() );
            }
        }
    }
}
