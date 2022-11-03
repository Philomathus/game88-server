package tv.game88.pay.api.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.mapper.PayChannelMapper;
import tv.game88.pay.api.mapper.PayPlatformMapper;
import tv.game88.pay.api.mapper.PayTypeMapper;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * @author mengJun
 */
@Component
@Log4j2
public class PayCacheUtil {
    public static final String TYPE_LIST               = "pay:typeList";
    public static final String TYPE                    = "pay:type:";
    public static final String PLATFORM                = "pay:platform:";
    public static final String CHANNEL                 = "pay:channel:";
    public static final String CHANNELSUCCESSRATE      = "pay:channelSuccessRate:";
    public static final String CHANNELSUCCESSRATE_LOCK = "pay:channelSuccessRateLock:";
    public static final String ORDER_ID                = "pay:orderId:";

    @Resource
    private RedisUtils        redisUtil;
    @Resource
    private PayTypeMapper     payTypeMapper;
    @Resource
    private PayPlatformMapper payPlatformMapper;
    @Resource
    private PayChannelMapper  payChannelMapper;

    public void existsPayTypeList() {
        if ( !redisUtil.exists( TYPE_LIST ) ) {
            List<PayType> payTypes = payTypeMapper.selectCachePayTypeList();
            if ( payTypes.isEmpty() ) {
                return;
            }
            this.setPayTypeList( payTypes );
        }
    }

    //获取支付类型list集合
    public List<PayType> getPayTypeList() {
        this.existsPayTypeList();
        String value = redisUtil.strGet( TYPE_LIST );
        return StringUtils.isNotBlank( value ) ? JsonUtil.json2Array( value, new TypeReference<>() {} ) : null;
    }

    /**
     * 获取支付类型列表缓存
     *
     * @param payTypes 支付类型列表
     */
    public void setPayTypeList( List<PayType> payTypes ) {
        redisUtil.unlink( TYPE_LIST );
        redisUtil.strSet( TYPE_LIST, JsonUtil.object2Json( payTypes ), Duration.ofHours( 6 ) );
    }


    public void clearPayTypeList() {
        redisUtil.unlink( TYPE_LIST );
    }

    public void existsPayType( Long payTypeId ) {
        if ( !redisUtil.exists( TYPE + payTypeId ) ) {
            PayType payType = payTypeMapper.selectById( payTypeId );
            if ( payType != null ) {
                this.setPayType( payType );
            }
        }
    }

    public void setPayType( PayType payType ) {
        redisUtil.unlink( TYPE + payType.getId() );
        redisUtil.strSet( TYPE + payType.getId(), JsonUtil.object2Json( payType ), Duration.ofHours( 6 ) );
    }

    /**
     * 获取支付类型缓存
     *
     * @param payTypeId 支付类型ID
     *
     * @return 支付类型
     */
    public PayType getPayType( Long payTypeId ) {
        this.existsPayType( payTypeId );
        String value = redisUtil.strGet( TYPE + payTypeId );
        return StringUtils.isNotBlank( value ) ? JsonUtil.json2Object( value, PayType.class ) : null;
    }

    public void clearPayType( String... payTypeIds ) {
        for ( String payTypeId : payTypeIds ) {
            redisUtil.unlink( TYPE + payTypeId );
        }
    }

    public void existsPayPlatform( Serializable payPlatformId ) {
        if ( !redisUtil.exists( PLATFORM + payPlatformId ) ) {
            PayPlatform payPlatform = payPlatformMapper.selectById( payPlatformId );
            if ( payPlatform != null ) {
                this.setPayPlatform( payPlatform );
            }
        }
    }

    public void setPayPlatform( PayPlatform payPlatform ) {
        redisUtil.unlink( PLATFORM + payPlatform.getId() );
        redisUtil.strSet( PLATFORM + payPlatform.getId(), JsonUtil.object2Json( payPlatform ), Duration.ofHours( 6 ) );
    }

    /**
     * 获取支付平台缓存
     *
     * @param payPlatformId 支付平台ID
     *
     * @return 支付平台
     */
    public PayPlatform getPayPlatform( Serializable payPlatformId ) {
        this.existsPayPlatform( payPlatformId );
        String value = redisUtil.strGet( PLATFORM + payPlatformId );
        return StringUtils.isNotBlank( value ) ? JsonUtil.json2Object( value, PayPlatform.class ) : null;
    }

    public void clearPayPlatform( Serializable... payPlatformIds ) {
        for ( Serializable payPlatformId : payPlatformIds ) {
            redisUtil.unlink( PLATFORM + payPlatformId );
        }
    }

    public void existsPayChannel( Serializable payChannelId ) {
        if ( !redisUtil.exists( CHANNEL + payChannelId ) ) {
            PayChannel payChannel = payChannelMapper.selectById( payChannelId );
            if ( payChannel != null ) {
                this.setPayChannel( payChannel );
            }
        }
    }

    public void setPayChannel( PayChannel payChannel ) {
        redisUtil.unlink( CHANNEL + payChannel.getId() );
        redisUtil.strSet( CHANNEL + payChannel.getId(), JsonUtil.object2Json( payChannel ), Duration.ofHours( 6 ) );
    }

    /**
     * 获取支付渠道缓存
     *
     * @param payChannelId 支付渠道ID
     *
     * @return 支付渠道
     */
    public PayChannel getPayChannel( Serializable payChannelId ) {
        this.existsPayChannel( payChannelId );
        String value = redisUtil.strGet( CHANNEL + payChannelId );
        return StringUtils.isNotBlank( value ) ? JsonUtil.json2Object( value, PayChannel.class ) : null;
    }

    public void clearPayChannel( Serializable... payChannelIds ) {
        for ( Serializable payChannelId : payChannelIds ) {
            redisUtil.unlink( CHANNEL + payChannelId );
        }
    }

    //---------------------------以下是admin--------------------------


    public void clearPayChannel( Long... payChannelIds ) {
        for ( Long payChannelId : payChannelIds ) {
            redisUtil.unlink( CHANNEL + payChannelId );
        }
    }

    public boolean setPayChannelSuccessRateLock( Long id ) {
        return redisUtil.strSetIfAbsent( CHANNELSUCCESSRATE_LOCK + id, "0", Duration.ofMinutes( 10 ) );
    }

    public boolean delPayChannelSuccessRateLock( Long id ) {
        return redisUtil.unlink( CHANNELSUCCESSRATE_LOCK + id );
    }

    public void setPayChannelSuccessRate( Long id, String successRate ) {
        redisUtil.strSet( CHANNELSUCCESSRATE + id, successRate, Duration.ofMinutes( 10 ) );
    }

    public String getPayChannelSuccessRate( Long id ) {
        return redisUtil.strGet( CHANNELSUCCESSRATE + id );
    }

}
