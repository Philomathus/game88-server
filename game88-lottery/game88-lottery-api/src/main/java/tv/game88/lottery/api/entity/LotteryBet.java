package tv.game88.lottery.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 彩票会员下注详情对象 lottery_bet
 *
 * @author mengJun
 */
@TableName( "lottery_bet" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LotteryBet {
    @TableId( type = IdType.INPUT )
    private String        id;
    @Excel( name = "会员ID" )
    private String        memberId;
    @Excel( name = "会员状态" )
    private Integer       memberStatus;
    @Excel( name = "下注彩种id" )
    private Integer       lotteryId;
    @Excel( name = "彩票名称" )
    private String        lotteryName;
    @Excel( name = "下注期数" )
    private String        issue;
    // 0= 待开奖 1= 已中奖 2=未中奖
    @Excel( name = "状态" )
    private Integer       status;
    @Excel( name = "下注选择菜单" )
    private String        methodId;
    @Excel( name = "下注选择" )
    private String        betSelect;
    @Excel( name = "下注索引" )
    private String        betIds;
    @Excel( name = "筹码" )
    private BigDecimal    chip;
    @Excel( name = "中奖金额" )
    private BigDecimal    prize;
    @Excel( name = "投资" )
    private BigDecimal    cost;
    @Excel( name = "开奖号码" )
    private String        code;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "下注时间" )
    private LocalDateTime betTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "发奖时间" )
    private LocalDateTime updateTime;

    // 直播间外-1
    @Excel( name = "主播ID" )
    private Long anchor;
}