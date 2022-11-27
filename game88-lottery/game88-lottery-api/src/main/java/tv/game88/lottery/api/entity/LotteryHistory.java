package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 彩票开奖历史对象 lottery_history
 *
 * @author mengJun
 */
@TableName( "lottery_history" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryHistory {
    @TableId( type = IdType.INPUT )
    private String        id;
    @Excel( name = "期数" )
    private String        issue;
    @Excel( name = "所属彩种" )
    private Integer       lotteryId;
    @Excel( name = "开奖号码" )
    private String        code;
    @Excel( name = "开奖时间" )
    private LocalDateTime ktime;
    @Excel( name = "开奖状态" )
    private Integer       status;
    @Excel( name = "彩票名称" )
    private String        name;
    @Excel( name = "自开实际杀率" )
    private BigDecimal    killRate;
    @Excel( name = "总投注" )
    private Long          totalBet;
    @Excel( name = "预计派奖总额" )
    private BigDecimal    totalPrize;
    @Excel( name = "控杀" )
    private Integer       ctl;
    @Excel( name = "开奖分析" )
    private String        analyse;

    /**
     * 请求参数
     */
    @JsonIgnore
    @TableField( exist = false )
    private Map<String, Object> params;
}