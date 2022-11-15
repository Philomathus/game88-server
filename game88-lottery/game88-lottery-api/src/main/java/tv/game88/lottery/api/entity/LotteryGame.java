package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 彩票下注配置对象 lottery_game
 *
 * @author mengJun
 */
@TableName( "lottery_game" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryGame {
    @TableId( type = IdType.AUTO )
    private Integer    id;
    @Excel( name = "菜单id" )
    private String     methodId;
    @Excel( name = "类型" )
    private String     type;
    @Excel( name = "简介" )
    private String     info;
    @Excel( name = "赔率" )
    private BigDecimal odds;
    @Excel( name = "排序" )
    private Integer    index;
}