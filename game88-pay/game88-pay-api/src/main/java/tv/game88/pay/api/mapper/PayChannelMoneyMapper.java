package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.entity.PayChannelMoney;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付通道金额Mapper接口
 *
 * @author mengJun
 */
public interface PayChannelMoneyMapper extends BaseMapper<PayChannelMoney> {
    public List<PayChannelMoney> selectPayChannelMoneyList( PayChannelMoney payChannelMoney );

    //-------------------------app--------------------------------

    List<Long> selectMoney( @Param( "typeId" ) Long typeId, @Param( "vipLevel" ) Integer vipLevel );

    Integer randomChannelId( @Param( "typeId" ) Integer typeId, @Param( "money" ) BigDecimal money,
                             @Param( "vipLevel" ) Integer vipLevel );

    Integer maxRateChannel( @Param( "typeId" ) Integer typeId, @Param( "money" ) BigDecimal money,
                            @Param( "vipLevel" ) Integer vipLevel );

    Integer minRateChannel( @Param( "typeId" ) Integer typeId, @Param( "money" ) BigDecimal money,
                            @Param( "vipLevel" ) Integer vipLevel );
}
