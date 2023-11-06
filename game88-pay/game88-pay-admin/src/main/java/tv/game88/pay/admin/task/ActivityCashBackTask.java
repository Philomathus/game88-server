package tv.game88.pay.admin.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.pay.api.entity.MemberRechargeBank;
import tv.game88.pay.api.mapper.ActivityCashBackMapper;
import tv.game88.pay.api.mapper.MemberRechargeBankMapper;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        if ( !configEnvCacheUtil.getConfBool( "cash_back_switch" ) ) {
            return;
        }
        if ( !redisUtils.lock( "ActivityCashBackTask", 60 ) ) {
            return;
        }
        log.info( "开始执行充值返现活动任务" );

        //查询昨天公司入款金额
        List<MemberRechargeBank> memberRechargeBanks = memberRechargeBankMapper.yesterdaySuccessOrder();

        Set<String> all = memberRechargeBanks
                .stream()
                .map( m -> m.getMemberId() + ":" + m.getRechargeMoney() )
                .collect( Collectors.toSet() );
        log.warn( "执行充值返现活动任务 - 昨日充值会员:{}", JsonUtil.object2Json( all ) );

        for ( MemberRechargeBank memberRechargeBank : memberRechargeBanks ) {
            //要返现金额
            Integer bycash = activityCashBackMapper.selectActivityCashBackBycash( memberRechargeBank.getRechargeMoney() );
            if ( bycash != null ) {
                int count = logMoneyMapper.findExistActivityCashBack( memberRechargeBank.getMemberId(), memberRechargeBank
                        .getMemberId()
                        .substring( memberRechargeBank.getMemberId().length() - 1 ) );
                if ( count > 0 ) {
                    log.error( "执行充值返现活动任务 - 存在充值记录的会员:{}, 金额:{}", memberRechargeBank.getMemberId(),
                            memberRechargeBank.getRechargeMoney() );
                    continue;
                }
                //会员返现
                try {
                    memberMoneyManager.addMemberMoney( memberRechargeBank.getMemberId(), new BigDecimal( bycash ),
                            EnumMoney.DEPOSIT_CASHBACK, BigDecimal.ONE, "充值返现活动", null, null );
                } catch ( Exception e ) {
                    log.error( memberRechargeBank.getMemberId() + "数据插入失败" + e.getMessage(), e );
                    log.error( "执行充值返现活动任务 - 充值失败的会员:{}, 金额:{}", memberRechargeBank.getMemberId(),
                            memberRechargeBank.getRechargeMoney() );
                }
            } else {
                log.warn( "执行充值返现活动任务 - 未达到充值标准的会员:{}, 金额:{}", memberRechargeBank.getMemberId(),
                        memberRechargeBank.getRechargeMoney() );
            }
        }
    }
}
