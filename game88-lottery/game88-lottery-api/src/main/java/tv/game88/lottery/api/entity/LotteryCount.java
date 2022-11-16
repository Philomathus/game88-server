package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 彩票会员下注行为对象 lottery_count
 *
 * @author mengJun
 */
@TableName( "lottery_count" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryCount {

    @TableId( type = IdType.AUTO )
    private Integer    id;
    @Excel( name = "代理号" )
    private String     agent;
    @Excel( name = "下注彩种id" )
    private Integer    lotteryId;
    @Excel( name = "会员ID" )
    private String     memberId;
    @Excel( name = "下注期数" )
    private String     issue;
    @Excel( name = "下注" )
    private String     betInfo;
    @Excel( name = "筹码" )
    private BigDecimal chip;
    @Excel( name = "ip" )
    private String     ip;
}