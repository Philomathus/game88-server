package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 彩票下注分类对象 lottery_method
 *
 * @author mengJun
 */
@TableName( "lottery_method" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryMethod {
    @TableId( type = IdType.INPUT )
    private Integer id;
    @Excel( name = "所属彩种类型" )
    private String  lotteryType;
    @Excel( name = "投注名称" )
    private String  name;
    @Excel( name = "排序" )
    private Integer sort;
}