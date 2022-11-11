package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.base.BasePayAgent;
import tv.game88.pay.api.base.PayAgentProcessorFactoryUtil;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.mapper.MemberWithdrawDetailMapper;
import tv.game88.pay.api.mapper.PayAgentChannelMapper;
import tv.game88.pay.api.mapper.PayAgentLogMapper;
import tv.game88.pay.api.mapper.PayAgentPlatformMapper;
import tv.game88.pay.api.service.PayAgentService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class PayAgentServiceImpl implements PayAgentService {
    @Resource
    private PayAgentChannelMapper        payAgentChannelMapper;
    @Resource
    private PayAgentPlatformMapper       payAgentPlatformMapper;
    @Resource
    private MemberWithdrawDetailMapper   withdrawDetailMapper;
    @Resource
    private PayAgentLogMapper            payAgentLogMapper;
    @Resource
    private PayAgentProcessorFactoryUtil payAgentProcessorFactoryUtil;
    @Resource
    private RedisUtils                   redisUtil;
    @Value( "${payAgentLimitKoLaPay:10000}" )
    private Integer                      payAgentLimitKoLaPay;

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void processOrderPay( MemberWithdrawDetail withdrawLog, PayAgentLog payAgentLog, String orderNo,
                                 PayAgentChannel payAgentChannel, boolean isSuccess ) {
        LocalDateTime        now            = LocalDateTime.now();
        MemberWithdrawDetail newWithdrawLog = new MemberWithdrawDetail();
        newWithdrawLog.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
        newWithdrawLog.setStatus( isSuccess ? 6 : 5 );
        newWithdrawLog.setUpdateTime( now );
        newWithdrawLog.setRemark( "【" + payAgentChannel.getName() + "】代付" + ( isSuccess ? "成功" : "失败" ) );
        withdrawDetailMapper.updateById( newWithdrawLog );

        PayAgentLog newPayAgentLog = new PayAgentLog();
        newPayAgentLog.setAgentOrderNo( orderNo );
        newPayAgentLog.setCallbackTime( now );
        newPayAgentLog.setCallbackStatus( isSuccess ? 1 : 2 );
        if ( payAgentLog == null ) {
            newPayAgentLog.setCreateTime( now );
            newPayAgentLog.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
            newPayAgentLog.setWithdrawMoney( withdrawLog.getWithdrawMoney() );
            newPayAgentLog.setWithdrawId( withdrawLog.getWithdrawId() );
            newPayAgentLog.setChannelId( payAgentChannel.getId() );
            newPayAgentLog.setChannelName( payAgentChannel.getName() );
            payAgentLogMapper.insert( newPayAgentLog );
        } else {
            newPayAgentLog.setWithdrawOrderNo( payAgentLog.getWithdrawOrderNo() );
            payAgentLogMapper.updateById( newPayAgentLog );
        }
    }

    @Override
    public void queryAgent4Status5Min() {
        List<PayAgentLog> payAgentLogs = payAgentLogMapper.findNoCallback();

        List<PayAgentChannel> payAgentChannels = payAgentChannelMapper.selectPayAgentChannelList( null );

        log.warn( "pal:" + payAgentLogs.size() + "pap:" + payAgentChannels.size() );

        for ( PayAgentLog payAgentLog : payAgentLogs ) {
            for ( PayAgentChannel payAgentChannel : payAgentChannels ) {
                if ( Objects.equals( payAgentLog.getChannelId(), payAgentChannel.getId() ) ) {
                    PayAgentPlatform payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

                    BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( payAgentPlatform.getCode() );
                    try {
                        log.warn( "开始批量查询代付订单 - 订单号：{}，channelId：{}", payAgentLog.getWithdrawOrderNo(),
                                payAgentLog.getChannelId() );
                        basePayAgent.queryOrderPay( payAgentLog );
                    } catch ( Exception e ) {
                        log.error( e.getMessage(), e );
                    }
                }
            }
        }
    }

    @Override
    public RspBase<?> payAgentOrder( ReqPayAgent reqPayAgent, String userName ) throws Exception {
        if ( !redisUtil.lock( "payAgent" + reqPayAgent.getWithdrawOrderNo(), 10 ) ) {
            return RspBase.businessError( "请勿重复提交代付订单:" + reqPayAgent.getWithdrawOrderNo() );
        }
        if ( StringUtils.isBlank( reqPayAgent.getWithdrawOrderNo() ) || reqPayAgent.getPayAgentChannelId() == null ) {
            return RspBase.businessError( "订单号或代付通道ID不能为空" );
        }
        MemberWithdrawDetail withdrawLog = withdrawDetailMapper.selectById( reqPayAgent.getWithdrawOrderNo() );

        PayAgentChannel payAgentChannel = payAgentChannelMapper.selectById( reqPayAgent.getPayAgentChannelId() );
        if ( withdrawLog == null || payAgentChannel == null ) {
            log.warn( "提现记录或代付通道未找到 - withdrawOrderNo:{};payAgentChannelId:{}", reqPayAgent.getWithdrawOrderNo(),
                    reqPayAgent.getPayAgentChannelId() );
            return RspBase.businessError( "提现记录或代付通道未找到" );
        }
        if ( StringUtils.isNotBlank( withdrawLog.getOpName() ) && !userName.equals( withdrawLog.getOpName() ) ) {
            return RspBase.businessError( "该订单只能由" + withdrawLog.getOpName() + "处理" );
        }
        PayAgentPlatform payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );
        if ( payAgentPlatform == null ) {
            log.warn( "代付平台未找到 - platformId:{}", payAgentChannel.getPlatformId() );
            return RspBase.businessError( "代付平台未找到" );
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( reqPayAgent.getWithdrawOrderNo() );
        if ( payAgentLog != null ) {
            return RspBase.businessError( "该订单已有代付记录" );
        }

        if ( withdrawLog.getWithdrawMoney() == null || withdrawLog.getWithdrawMoney().compareTo( BigDecimal.ZERO ) <= 0 ) {
            log.warn( "提现金额有误 - withdrawOrderNo:{};withdrawMoney:{}", reqPayAgent.getWithdrawOrderNo(),
                    withdrawLog.getWithdrawMoney() );
            return RspBase.businessError( "提现金额不得低于0元" );
        }
        if ( withdrawLog.getStatus() != 1 ) {
            return RspBase.businessError( "审核流程非法" );
        }

        if ( payAgentPlatform.getCode().contains( ConstantsPayAgent.KOLA )
                && withdrawLog.getWithdrawMoney().compareTo( new BigDecimal( payAgentLimitKoLaPay ) ) > 0 ) {
            return RspBase.businessError( "此代付暂不支持" + payAgentLimitKoLaPay + "以上出款" );
        }

        long noFailCount = payAgentLogMapper.selectCount( new QueryWrapper<PayAgentLog>()
                .eq( "withdraw_order_no", withdrawLog.getWithdrawOrderNo() ).ne( "callback_status", 2 ) );
        if ( noFailCount > 0 ) {
            return RspBase.businessError( "此订单已被代付，请在三方后台跟踪订单状态" );
        }
        long platOrderCount = payAgentLogMapper.selectCount( new QueryWrapper<PayAgentLog>()
                .eq( "withdraw_order_no", withdrawLog.getWithdrawOrderNo() )
                .eq( "channel_id", reqPayAgent.getPayAgentChannelId() ) );
        if ( platOrderCount > 0 ) {
            return RspBase.businessError( String.format( "此订单已被 %s 处理过，请更换代付商后重试", payAgentChannel.getName() ) );
        }

        reqPayAgent.setCurrentTime( LocalDateTime.now() );
        PayAgentService payAgentService = SpringUtils.getBean( PayAgentService.class );
        payAgentService.processOrder( payAgentChannel, withdrawLog, reqPayAgent.getCurrentTime(), 4, 0 );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( payAgentPlatform.getCode() );
        if ( basePayAgent.orderPay( withdrawLog, payAgentChannel, reqPayAgent ) ) {
            return RspBase.ok( "代付订单提交成功" );
        }

        redisUtil.unLock( "payAgent" + reqPayAgent.getWithdrawOrderNo() );
        return RspBase.businessError( StringUtils.isNotBlank( reqPayAgent.getFailReason() ) ? reqPayAgent.getFailReason() :
                "代付失败" );
    }

    @Override
    public RspBase<?> payAgentOrders( ReqPayAgent reqPayAgent, String userName ) {
        if ( CollectionUtils.isEmpty( reqPayAgent.getWithdrawOrderNos() ) || reqPayAgent.getWithdrawOrderNos().size() == 0 ) {
            return RspBase.businessError( "请选择需要代付的订单" );
        }
        if ( reqPayAgent.getPayAgentChannelId() == null ) {
            return RspBase.businessError( "请选择代付通道" );
        }

        List<PayAgentLog> payAgentLogList = payAgentLogMapper.selectBatchIds( reqPayAgent.getWithdrawOrderNos() );
        if ( payAgentLogList.size() > 0 ) {
            return RspBase.businessError( "被选中的订单已有代付记录" );
        }
        if ( !redisUtil.lock( "payAgent" + userName, 10 ) ) {
            return RspBase.businessError( "请勿重复提交代付订单" );
        }

        PayAgentChannel payAgentChannel = payAgentChannelMapper.selectById( reqPayAgent.getPayAgentChannelId() );
        if ( payAgentChannel == null ) {
            log.warn( "代付通道未找到 - payAgentPlatId:{}", reqPayAgent.getPayAgentChannelId() );
            return RspBase.businessError( "代付通道未找到" );
        }
        PayAgentPlatform payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );
        if ( payAgentPlatform == null ) {
            log.warn( "代付平台未找到 - platformId:{}", payAgentChannel.getPlatformId() );
            return RspBase.businessError( "代付平台未找到" );
        }

        List<MemberWithdrawDetail> withdrawLogs = withdrawDetailMapper.selectBatchIds( reqPayAgent.getWithdrawOrderNos() );
        if ( CollectionUtils.isEmpty( withdrawLogs ) ) {
            return RspBase.businessError( "未匹配到可提现订单" );
        }
        for ( MemberWithdrawDetail withdrawLog : withdrawLogs ) {
            if ( userName.equals( withdrawLog.getOpName() ) ) {
                return RspBase.businessError( String.format( "订单%s只能由%s处理", withdrawLog.getWithdrawOrderNo(),
                        withdrawLog.getOpName() ) );
            }
        }
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( payAgentPlatform.getCode() );

        Map<String, String> failReasonList  = new TreeMap<>();
        int                 sucessNum       = 0;
        PayAgentService     payAgentService = SpringUtils.getBean( PayAgentService.class );
        for ( MemberWithdrawDetail withdrawLog : withdrawLogs ) {
            long noFailCount = payAgentLogMapper.selectCount( new QueryWrapper<PayAgentLog>()
                    .eq( "withdraw_order_no", withdrawLog.getWithdrawOrderNo() ).ne( "callback_status", 2 ) );
            if ( noFailCount > 0 ) {
                failReasonList.put( withdrawLog.getWithdrawOrderNo(), "此订单已被代付，请在三方后台跟踪订单状态" );
                continue;
            }
            long platOrderCount = payAgentLogMapper.selectCount( new QueryWrapper<PayAgentLog>()
                    .eq( "withdraw_order_no", withdrawLog.getWithdrawOrderNo() )
                    .eq( "channel_id", reqPayAgent.getPayAgentChannelId() ) );
            if ( platOrderCount > 0 ) {
                failReasonList.put( withdrawLog.getWithdrawOrderNo(), String.format( "此订单已被 %s 处理过，请更换代付商后重试",
                        payAgentChannel.getName() ) );
                continue;
            }

            ReqPayAgent newReqPayAgent = new ReqPayAgent();
            newReqPayAgent.setCurrentTime( LocalDateTime.now() );
            newReqPayAgent.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
            try {
                payAgentService.processOrder( payAgentChannel, withdrawLog, newReqPayAgent.getCurrentTime(), 4, 0 );

                if ( basePayAgent.orderPay( withdrawLog, payAgentChannel, newReqPayAgent ) ) {
                    sucessNum++;
                } else {
                    failReasonList.put( withdrawLog.getWithdrawOrderNo(), newReqPayAgent.getFailReason() );
                }
            } catch ( Exception e ) {
                log.error( "代付下单失败 - 订单号：{};失败原因：{}", withdrawLog.getWithdrawOrderNo(), e.getMessage(), e );
                failReasonList.put( withdrawLog.getWithdrawOrderNo(), newReqPayAgent.getFailReason() );
            }
        }
        redisUtil.unLock( "payAgent" + userName );
        return RspBase.ok( Map.of( "fail", failReasonList, "success", sucessNum ) );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void processOrder( PayAgentChannel payAgentChannel, MemberWithdrawDetail memberWithdrawLog, LocalDateTime now,
                              int status, int orderState ) {
        MemberWithdrawDetail withdrawLog = withdrawDetailMapper.selectById( memberWithdrawLog.getWithdrawOrderNo() );
        PayAgentLog          payAgentLog = payAgentLogMapper.selectById( memberWithdrawLog.getWithdrawOrderNo() );
        if ( !( withdrawLog.getStatus() == 1 || withdrawLog.getStatus() == 4 ) ) {
            throw new BusinessException( "审核流程非法" );
        }
        // 更改withdrawLog状态
        MemberWithdrawDetail newWithdrawLog = new MemberWithdrawDetail();
        newWithdrawLog.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
        newWithdrawLog.setStatus( status );
        newWithdrawLog.setUpdateTime( now );
        String remark = switch ( status ) {
            case 4 -> "已交由【" + payAgentChannel.getName() + "】出款";
            case 5 -> "【" + payAgentChannel.getName() + "】代付失败";
            case 6 -> "【" + payAgentChannel.getName() + "】代付成功";
            default -> "";
        };
        newWithdrawLog.setRemark( remark );
        withdrawDetailMapper.updateById( newWithdrawLog );

        // 保存代付信息日志
        PayAgentLog newPayAgentLog = new PayAgentLog();
        newPayAgentLog.setWithdrawOrderNo( memberWithdrawLog.getWithdrawOrderNo() );
        switch ( status ) {
        case 4 -> newPayAgentLog.setCallbackStatus( 0 );
        case 5 -> {
            newPayAgentLog.setCallbackTime( now );
            newPayAgentLog.setCallbackStatus( 2 );
        }
        case 6 -> {
            newPayAgentLog.setCallbackTime( now );
            newPayAgentLog.setCallbackStatus( 1 );
        }
        default -> {
        }
        }
        if ( payAgentLog == null ) {
            newPayAgentLog.setCreateTime( now );
            newPayAgentLog.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
            newPayAgentLog.setWithdrawMoney( withdrawLog.getWithdrawMoney() );
            newPayAgentLog.setWithdrawId( withdrawLog.getWithdrawId() );
            newPayAgentLog.setChannelId( payAgentChannel.getId() );
            newPayAgentLog.setChannelName( payAgentChannel.getName() );
            payAgentLogMapper.insert( newPayAgentLog );
        } else {
            payAgentLogMapper.updateById( newPayAgentLog );
        }
        log.warn( JsonUtil.object2Json( newPayAgentLog ) );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void callBackOrder( MemberWithdrawDetail withdrawLog, String channelName ) {
        // 更改withdrawLog状态
        MemberWithdrawDetail newWithdrawLog = new MemberWithdrawDetail();
        newWithdrawLog.setWithdrawOrderNo( withdrawLog.getWithdrawOrderNo() );
        newWithdrawLog.setStatus( 1 );
        newWithdrawLog.setUpdateTime( LocalDateTime.now() );
        newWithdrawLog.setRemark( String.format( "请求代付[%s]不成功", channelName ) );
        int updateW = withdrawDetailMapper.updateById( newWithdrawLog );
        int deleteP = payAgentLogMapper.deleteById( withdrawLog.getWithdrawOrderNo() );
        if ( updateW <= 0 && deleteP <= 0 ) {
            log.error( "代付状态回退失败:{}", withdrawLog.getWithdrawOrderNo() );
            throw new BusinessException( "代付状态回退失败" );
        }
    }
}
