package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.entity.PayChannelMoney;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付通道金额Mapper接口
 *
 * @author 77tv
 * @date 2021-04-03
 */
public interface PayChannelMoneyMapper extends BaseMapper<PayChannelMoney> {
    public List<PayChannelMoney> selectPayChannelMoneyList( PayChannelMoney payChannelMoney );

    int deleteByChannelIds( @Param( "channelIds" ) List<Long> channelIds );

    int deleteByChannelId( Long channelId );

    //-------------------------app--------------------------------

    List<Long> selectMoney( @Param( "typeCode" ) Long typeCode, @Param( "vipLevel" ) Integer vipLevel );

    Integer randomChannelId( @Param( "typeCode" ) Integer typeCode, @Param( "money" ) BigDecimal money,
                             @Param( "vipLevel" ) Integer vipLevel );

    Integer maxRateChannel( @Param( "typeCode" ) Integer typeCode, @Param( "money" ) BigDecimal money,
                            @Param( "vipLevel" ) Integer vipLevel );

    Integer minRateChannel( @Param( "typeCode" ) Integer typeCode, @Param( "money" ) BigDecimal money,
                            @Param( "vipLevel" ) Integer vipLevel );
}
