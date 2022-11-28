package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 游戏信息对象 game_info
 *
 * @author mengJun
 */
@TableName( "game_info" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameInfo {

    /**
     * 系统编号
     */
    @TableId( type = IdType.AUTO )
    private Long id;

    /**
     * 游戏名称
     */
    @Excel( name = "游戏名称" )
    private String name;

    /**
     * 游戏平台ID
     */
    @Excel( name = "游戏平台ID" )
    private Long platformId;

    /**
     * 游戏类型ID
     */
    @Excel( name = "游戏类型ID" )
    private Long typeId;

    /**
     * 游戏码
     */
    @Excel( name = "游戏码" )
    private String kindId;

    /**
     * 图标
     */
    @Excel( name = "图标" )
    private String icon;

    /**
     * 激活状态(1启用0停用)
     */
    @Excel( name = "激活状态(1启用0停用)" )
    private Boolean effect;

    /**
     * 是否维护(1是0否)
     */
    @Excel( name = "是否维护(1是0否)" )
    private Boolean maintain;

    /**
     * 是否推荐(1是0否)
     */
    @Excel( name = "是否推荐(1是0否)" )
    private Boolean recommend;

    /**
     * 是否大图标(1是0否)
     */
    @Excel( name = "是否大图标(1是0否)" )
    private Boolean largeIcon;

    /**
     * 创建时间
     */
    @Excel( name = "创建时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    @Excel( name = "排序" )
    private Integer sort;
}