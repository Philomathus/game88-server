package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏注单版本对象 game_data_record
 *
 * @author MengJun
 */
@TableName( "game_record_version" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameDataRecord {
    @TableId( type = IdType.INPUT )
    private String id;
    /**
     * 平台代理
     */
    private String agent;
    /**
     * 游戏代理
     */
    private String gameAgent;
    /**
     * 游戏平台ID
     */
    private Long   platformId;
    /**
     * 游戏注单ID
     */
    private String gameId;
    /**
     * 游戏局号
     */
    private String gameRound;
    /**
     * 会员ID
     */
    private String account;
    /**
     * 游戏码
     */
    private String kindId;
    /**
     * 游戏注单ID
     */
    private String serverId;
    private String tableId;
    private String chairId;
    private String cellScore;
    private String allBet;
    private String profit;
    private String revenue;
    private String gameStartTime;
    private String gameEndTime;
}