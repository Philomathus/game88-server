package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 彩票信息对象 lottery_info
 *
 * @author mengJun
 */
@TableName( "lottery_info" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryInfo {
    @TableId( type = IdType.INPUT )
    private Integer    id;
    @Excel( name = "彩种名称" )
    private String     name;
    @Excel( name = "类型" )
    private String     type;
    @Excel( name = "激活状态" )
    private Boolean    effect;
    @Excel( name = "图标" )
    private String     icon;
    @Excel( name = "杀率" )
    private BigDecimal killRate;
    @Excel( name = "最小投注金额" )
    private BigDecimal minCost;
    @Excel( name = "周期" )
    private Integer    cycle;
}