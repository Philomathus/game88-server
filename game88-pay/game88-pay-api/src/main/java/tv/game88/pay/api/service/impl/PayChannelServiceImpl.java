package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayChannelMoney;
import tv.game88.pay.api.mapper.PayChannelMapper;
import tv.game88.pay.api.mapper.PayChannelMoneyMapper;
import tv.game88.pay.api.service.PayChannelService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@Service
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel> implements PayChannelService {
    @Resource
    private PayChannelMoneyMapper payChannelMoneyMapper;
    @Resource
    private PayCacheUtil          payCacheUtil;

    @Override
    public List<PayChannel> selectPayChannelList( PayChannel payChannel ) {
        List<PayChannel> payChannels = this.baseMapper.selectPayChannelList( payChannel );
        for ( PayChannel me : payChannels ) {
            if ( me.getEffect() ) {
                String successRate = payCacheUtil.getPayChannelSuccessRate( me.getId() );
                if ( successRate == null ) {
                    Executors.newVirtualThreadPerTaskExecutor().execute( () -> {
                        if ( payCacheUtil.setPayChannelSuccessRateLock( me.getId() ) ) {
                            payCacheUtil.setPayChannelSuccessRate( me.getId(), this.baseMapper.successRate( me.getId() ) );
                            payCacheUtil.delPayChannelSuccessRateLock( me.getId() );
                        }
                    } );
                    me.setSuccessRate( "计算中..." );
                } else {
                    me.setSuccessRate( successRate );
                }
            } else {
                me.setSuccessRate( "已停用" );
            }
        }
        return payChannels;
    }

    @Override
    public RspBase<?> insertPayChannel( PayChannel payChannel ) {
        RspBase<?> businessError = saveCheck( payChannel );
        if ( businessError != null ) {
            return businessError;
        }
        payChannel.setEffect( false );
        payChannel.setCanCallback( true );
        payChannel.setDelFlag( false );
        int i = this.baseMapper.insert( payChannel );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "支付通道插入失败" );
    }

    private RspBase<?> saveCheck( PayChannel payChannel ) {
        if ( payChannel.getRate() == null || StringUtils.isBlank( payChannel.getQuickAmount() ) ) {
            return RspBase.businessError( "通道费率或快捷金额不得为空,请补全" );
        }
        if ( payChannel.getRate().compareTo( new BigDecimal( "0.4" ) ) > 0
                || payChannel.getRate().compareTo( new BigDecimal( "0.01" ) ) < 0 ) {
            return RspBase.businessError( "通道费率不得大于0.4或小于0.01" );
        }
        if ( !StringUtils.isNotBlank( payChannel.getDiscountBill() ) || payChannel.getDiscountBill() == null ) {
            payChannel.setDiscountBill( "0" );
        }
        if ( new BigDecimal( payChannel.getDiscountBill() ).compareTo( new BigDecimal( "1" ) ) > 0 ) {
            return RspBase.businessError( "优惠比例请填写小数形式,不可大于1" );
        }
        payChannel.setQuickAmount( payChannel.getQuickAmount().trim().replaceAll( " ", "" ).replaceAll( "，", "," ) );
        return null;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> updatePayChannel( PayChannel payChannel ) {
        RspBase<?> businessError = saveCheck( payChannel );
        if ( businessError != null ) {
            return businessError;
        }
        payChannel.setEffect( null );
        payChannel.setCanCallback( null );
        payChannel.setDelFlag( null );
        int i = this.baseMapper.updateById( payChannel );
        if ( i > 0 ) {
            PayChannel channelNew = this.baseMapper.selectById( payChannel.getId() );
            if ( channelNew.getEffect() ) {
                payChannelMoneyMapper.deleteByChannelIds( Collections.singletonList( payChannel.getId() ) );
                String[] moneys = channelNew.getQuickAmount().split( "," );
                for ( String money : moneys ) {
                    PayChannelMoney payChannelMoney = new PayChannelMoney();
                    payChannelMoney.setMoney( Long.parseLong( money.trim() ) );
                    payChannelMoney.setChannelId( channelNew.getId() );
                    payChannelMoney.setChannelPayRate( channelNew.getRate() );
                    payChannelMoney.setTypeId( channelNew.getTypeId() );
                    payChannelMoney.setOpenLevelMin( channelNew.getOpenLevelMin() == null ? 1 : channelNew.getOpenLevelMin() );
                    payChannelMoney.setOpenLevelMax( channelNew.getOpenLevelMax() == null ? 50 : channelNew.getOpenLevelMax() );
                    payChannelMoneyMapper.insert( payChannelMoney );
                }
            }
            return RspBase.ok();
        }
        return RspBase.businessError( "更新失败" );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> updateEffect( Long id, Boolean effect ) {
        if ( effect ) {
            PayChannel channelNew    = this.baseMapper.selectById( id );
            RspBase<?> businessError = saveCheck( channelNew );
            if ( businessError != null ) {
                return businessError;
            }
            payChannelMoneyMapper.deleteByChannelIds( Collections.singletonList( id ) );
            String[] moneys = channelNew.getQuickAmount().split( "," );
            for ( String money : moneys ) {
                PayChannelMoney payChannelMoney = new PayChannelMoney();
                payChannelMoney.setMoney( Long.parseLong( money.trim() ) );
                payChannelMoney.setChannelId( channelNew.getId() );
                payChannelMoney.setChannelPayRate( channelNew.getRate() );
                payChannelMoney.setTypeId( channelNew.getTypeId() );
                payChannelMoney.setOpenLevelMin( channelNew.getOpenLevelMin() == null ? 1 : channelNew.getOpenLevelMin() );
                payChannelMoney.setOpenLevelMax( channelNew.getOpenLevelMax() == null ? 50 : channelNew.getOpenLevelMax() );
                payChannelMoneyMapper.insert( payChannelMoney );
            }
        } else {
            payChannelMoneyMapper.deleteByChannelIds( Collections.singletonList( id ) );
        }
        PayChannel update = new PayChannel();
        update.setId( id );
        update.setEffect( effect );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "更新失败" );
    }
}

