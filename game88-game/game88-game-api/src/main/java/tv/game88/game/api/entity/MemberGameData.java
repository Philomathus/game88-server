package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员游戏注单数据对象 member_game_data
 *
 * @author mengJun
 */
@TableName( "member_game_data" )
@Data
@EqualsAndHashCode( callSuper = false )
public class MemberGameData {
    @TableId( type = IdType.INPUT )
    private String  id;
    @Excel( name = "代理编号" )
    private String  agent;
    @Excel( name = "账号" )
    private String  account;
    @Excel( name = "本地平台id" )
    private Integer platformId;
    @Excel( name = "游戏局号" )
    private String  gameId;
    @Excel( name = "游戏id" )
    private String  kindId;
    @Excel( name = "房间号" )
    private String  serverId;
    @Excel( name = "有效下注" )
    private String  cellScore;
    @Excel( name = "总下注" )
    private String  allBet;
    @Excel( name = "盈利" )
    private String  profit;
    @Excel( name = "抽水" )
    private String  revenue;
    @Excel( name = "0:未洗码1已经洗码" )
    private Integer status;
    @Excel( name = "游戏开始时间" )
    private String  gameStartTime;
    @Excel( name = "游戏结束时间" )
    private String  gameEndTime;
    @Excel( name = "游戏局号" )
    private String  gameRound;

    @TableField( exist = false )
    @Excel( name = "平台名称" )
    private String     platformName;
    @TableField( exist = false )
    @Excel( name = "子平台名称" )
    private String     sonPlatformName;
    @TableField( exist = false )
    private BigDecimal totalSuccessBet;
    @TableField( exist = false )
    private BigDecimal totalBet;
    @TableField( exist = false )
    private BigDecimal totalIncome;

}