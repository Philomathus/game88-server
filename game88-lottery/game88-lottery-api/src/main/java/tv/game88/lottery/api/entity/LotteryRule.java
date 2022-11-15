package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 彩票规则说明对象 lottery_rule
 *
 * @author mengJun
 */
@TableName( "lottery_rule" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryRule {
    @TableId( type = IdType.AUTO )
    private Integer id;
    @Excel( name = "彩票类型名称" )
    private String  name;
    @Excel( name = "彩票类型id" )
    private Integer kind;
    @Excel( name = "开奖说明" )
    private String  des;

}