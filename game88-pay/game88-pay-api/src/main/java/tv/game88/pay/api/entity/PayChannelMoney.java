package tv.game88.pay.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付通道金额对象 pay_channel_money
 *
 * @author mengJun
 */
@Data
public class PayChannelMoney {
    @TableId( type = IdType.AUTO )
    private Long       id;
    /**
     * 通道金额
     */
    private Long       money;
    /**
     * 通道ID
     */
    private Long       channelId;
    /**
     * 通道费率
     */
    private BigDecimal channelPayRate;
    /**
     * 支付类型ID
     */
    private Long       typeId;
    /**
     * 开放层级
     */
    private Integer    openLevelMin;
    private Integer    openLevelMax;

}