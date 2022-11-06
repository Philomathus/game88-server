package tv.game88.core.config.cache;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class GenerateOrderCacheUtils {
    public static GenerateOrderCacheUtils me;

    private static final String CONFIG_ORDER_ID = Constants.CONFIG_PREX + "orderId:";

    @Resource
    private RedisUtils redisUtil;

    @PostConstruct
    void init() {
        me = this;
    }

    /**
     * <h2>分布式生成唯一订单号</h2>
     * <p>使用redis生成分布唯一订单号, 格式为prefix + 17位格式为 yyyyMMddHHmmssSSS 时间字符串 + randomDigits位数的随机a-z,A-Z的字符串</p>
     *
     * @param prefix       前缀标识
     * @param randomDigits 随机a-z,A-Z的字符串,长度为randomDigits
     */
    public String getOrderId( String prefix, int randomDigits ) {
        String orderNo = prefix + LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER )
                + RandomStringUtils.randomAlphabetic( randomDigits );
        if ( !redisUtil.strSetIfAbsent( CONFIG_ORDER_ID + orderNo, "", Duration.ofSeconds( 10 ) ) ) {
            return getOrderId( prefix, randomDigits );
        }
        return orderNo;
    }
}
