package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 彩票奖池汇总对象 lottery_prizepool
 *
 * @author mengJun
 */
@TableName( "lottery_prizepool" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryPrizepool {
    @TableId( type = IdType.INPUT )
    private String id;
    @Excel( name = "彩种编号" )
    private Integer lotteryId;
    @Excel( name = "奖池日期" )
    private String lotteryDate;
    @Excel( name = "奖池日期小时" )
    private Integer lotteryHour;
    @Excel( name = "奖池投注日累积" )
    private BigDecimal betTotal;
    @Excel( name = "奖池派奖日累积" )
    private BigDecimal awardTotal;
    @Excel( name = "奖池剩余金额日累积" )
    private BigDecimal overTotal;
    @Excel( name = "累积杀率" )
    private BigDecimal killRate;
    @Excel( name = "游戏奖池使用金额" )
    private BigDecimal poolUseMoney;

}