package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 彩票即使信息对象 lottery_temp
 *
 * @author mengJun
 */
@TableName( "lottery_temp" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryTemp {
    @TableId( type = IdType.INPUT )
    @Excel( name = "彩票ID" )
    private Integer       id;
    @Excel( name = "当前期数" )
    private String        issue;
    @Excel( name = "开奖时间" )
    private LocalDateTime ktime;
    @Excel( name = "上期期号" )
    private String        issueJust;
    @Excel( name = "上期开奖" )
    private String        codeJust;
    @Excel( name = "状态" )
    private Integer       su;
}