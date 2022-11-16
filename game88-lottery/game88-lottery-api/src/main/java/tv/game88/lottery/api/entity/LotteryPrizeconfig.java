package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.vo.BaseEntity;

import java.math.BigDecimal;

/**
 * 彩票杀率配置对象 lottery_prizeconfig
 *
 * @author mengJun
 */
@TableName( "lottery_prizeconfig" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryPrizeconfig extends BaseEntity {
    @TableId( type = IdType.INPUT )
    private Integer    lotteryId;
    @Excel( name = "彩种名称" )
    private String     lotteryName;
    @Excel( name = "杀率阀值" )
    private BigDecimal lotteryKillrate;
    @Excel( name = "杀率禁用时间点" )
    private String     lotteryNokillratehour;
    @Excel( name = "随机开启不杀概率" )
    private Integer    lotteryRandom;
}